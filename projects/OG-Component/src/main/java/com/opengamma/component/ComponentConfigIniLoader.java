/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ResourceUtils;

/**
 * Loads configuration from the INI format file.
 * <p>
 * The format is line-based as follows:<br>
 *  <code>#</code> or <code>;</code> for comment lines (at the start of the line)<br>
 *  <code>${key}</code> is replaced by an earlier replacement declaration<br>
 *  <code>[group]</code> defines the start of a named group of configs<br>
 *  <code>key = value</code> defines a single config element within a group<br>
 *  <code>MANAGER.INCLUDE = resource</code> declares a resource to be included immediately<br>
 *  the "global" group is used to add keys to the set of properties used for replacement<br>
 *  Everything is trimmed as necessary.
 *  <p>
 *  The specified properties are used in two ways.
 *  Firstly, they are used to substitute sections that have defined the '<code>${key}</code>' notation.
 *  Secondly, they can directly override properties in an INI group, if the property has a
 *  key of the format '{@code [group].key}'.
 */
public class ComponentConfigIniLoader extends AbstractComponentConfigLoader {

  /**
   * The pattern to match [group].key
   */
  private static final Pattern GROUP_OVERRIDE = Pattern.compile("\\[" + "([^\\]]+)" + "\\]" + "[.]" + "(.+)");

  /**
   * Creates an instance.
   * 
   * @param logger  the logger, not null
   * @param properties  the properties in use, not null
   */
  public ComponentConfigIniLoader(ComponentLogger logger, ConcurrentMap<String, String> properties) {
    super(logger, properties);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the INI file.
   * <p>
   * Loads the configuration defining components from the specified resource.
   * 
   * @param resource  the config resource to load, not null
   * @param depth  the depth of the properties file, used for logging
   * @return the config, not null
   */
  public ComponentConfig load(final Resource resource, final int depth) {
    try {
      ComponentConfig config = doLoad(resource, depth, new ComponentConfig());
      return overrideProperties(config);
      
    } catch (RuntimeException ex) {
      throw new OpenGammaRuntimeException("Unable to load INI file: " + resource, ex);
    }
  }

  /**
   * Loads the INI file.
   * <p>
   * Loads the configuration defining components from the specified resource.
   * 
   * @param resource  the config resource to load, not null
   * @param depth  the depth of the properties file, used for logging
   * @param config  the config being loaded, not null
   * @return the config, not null
   */
  private ComponentConfig doLoad(final Resource resource, final int depth, ComponentConfig config) {
    List<String> lines = readLines(resource);
    String group = null;
    int lineNum = 0;
    for (String line : lines) {
      lineNum++;
      line = line.trim();
      if (line.length() == 0 || line.startsWith("#") || line.startsWith(";")) {
        continue;
      }
      if (line.startsWith("[") && line.endsWith("]")) {
        group = line.substring(1, line.length() - 1);
        
      } else if (group == null) {
        throw new OpenGammaRuntimeException("Invalid format, properties must be specified within a [group], line " + lineNum);
        
      } else {
        int equalsPosition = line.indexOf('=');
        if (equalsPosition < 0) {
          throw new OpenGammaRuntimeException("Invalid format, line " + lineNum);
        }
        String key = line.substring(0, equalsPosition).trim();
        String value = line.substring(equalsPosition + 1).trim();
        if (key.length() == 0) {
          throw new IllegalArgumentException("Invalid empty key, line " + lineNum);
        }
        if (config.contains(group, key)) {
          throw new IllegalArgumentException("Invalid file, key '" + key + "' specified twice, line " + lineNum);
        }
        
        // resolve ${} references
        value = resolveProperty(value, lineNum);
        
        // handle includes
        if (key.equals(ComponentManager.MANAGER_INCLUDE)) {
          handleInclude(resource, value, depth, config);
        } else {
          // store property
          config.put(group, key, value);
          if (group.equals("global")) {
            getProperties().put(key, value);
          }
        }
      }
    }
    return config;
  }

  /**
   * Handle the inclusion of another file.
   * 
   * @param baseResource  the base resource, not null
   * @param includeFile  the resource to include, not null
   * @param depth  the depth of the properties file, used for logging
   * @param config  the config being loaded, not null
   */
  private void handleInclude(final Resource baseResource, String includeFile, final int depth, ComponentConfig config) {
    // find resource
    Resource include;
    try {
      include = ResourceUtils.createResource(includeFile);
    } catch (Exception ex) {
      try {
        include = baseResource.createRelative(includeFile);
      } catch (Exception ex2) {
        throw new OpenGammaRuntimeException(ex2.getMessage(), ex2);
      }
    }
    
    // load and merge
    getLogger().logInfo(StringUtils.repeat(" ", depth) + "   Including file: " + include);
    try {
      doLoad(include, depth + 1, config);
    } catch (RuntimeException ex) {
      throw new OpenGammaRuntimeException("Unable to load INI file: " + include, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Override config using the properties.
   * <p>
   * Any property that has a key of the form '[group].key' will replace the
   * specified key within the group.
   * 
   * @param config  the config to update, not null
   * @return the updated config, not null
   */
  private ComponentConfig overrideProperties(ComponentConfig config) {
    List<String[]> iniProperties = extractIniOverrideProperties();
    for (String[] array : iniProperties) {
      config.getGroup(array[0]);  // validate group (but returns a copy of the inner map)
      config.put(array[0], array[1], array[2]);
      getLogger().logDebug("  Replacing group property: [" + array[0] + "]." + array[1] + "=" + array[2]);
    }
    return config;
  }

  /**
   * Extracts any properties that match the group name style "[group].key".
   * <p>
   * These directly override any INI file settings.
   * 
   * @return the extracted set of INI properties, not null
   */
  private List<String[]> extractIniOverrideProperties() {
    List<String[]> extracted = new ArrayList<String[]>();
    for (String key : getProperties().keySet()) {
      Matcher matcher = GROUP_OVERRIDE.matcher(key);
      if (matcher.matches()) {
        String group = matcher.group(1);
        String propertyKey = matcher.group(2);
        String[] array = {group, propertyKey, getProperties().get(key)};
        extracted.add(array);
      }
    }
    return extracted;
  }

}
