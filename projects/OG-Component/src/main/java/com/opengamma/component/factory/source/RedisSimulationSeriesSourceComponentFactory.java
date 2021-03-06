/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import redis.clients.jedis.JedisPool;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceResource;
import com.opengamma.core.historicaltimeseries.impl.RedisSimulationSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DataHistoricalTimeSeriesResolverResource;
import com.opengamma.master.historicaltimeseries.impl.RedisSimulationSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesResolver;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.redis.RedisConnector;

/**
 * A component factory to build {@code RedisSimulationSeriesSource} instances.
 */
@BeanDefinition
public class RedisSimulationSeriesSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * Connector to the Redis server.
   */
  @PropertyDefinition
  private RedisConnector _redisConnector;
  /**
   * prefix to append to redis keys when stored
   */
  @PropertyDefinition
  private String _redisPrefix = "";
  /**
   * The redis database to connect to
   */
  @PropertyDefinition
  private Integer _database;
  /**
   * The initial value to use for the simulation date - this may be modified later during runtime.
   */
  @PropertyDefinition
  private LocalDate _simulationDate;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;

  //-------------------------------------------------------------------------
  @Override
  public void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception {
    final JedisPool jedisPool = getRedisConnector().getJedisPool();
    final RedisSimulationSeriesSource  instance = new RedisSimulationSeriesSource(jedisPool, getRedisPrefix());
    if (_simulationDate != null) {
      instance.setCurrentSimulationExecutionDate(_simulationDate);
    }
    HistoricalTimeSeriesResolver resolver = new RedisSimulationSeriesResolver(instance);

    ComponentInfo infoResolver = new ComponentInfo(HistoricalTimeSeriesResolver.class, getClassifier());
    infoResolver.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    infoResolver.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteHistoricalTimeSeriesResolver.class);
    repo.registerComponent(infoResolver, resolver);
    ComponentInfo infoSource = new ComponentInfo(HistoricalTimeSeriesSource.class, getClassifier());
    infoSource.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    infoSource.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteHistoricalTimeSeriesSource.class);
    repo.registerComponent(infoSource, instance);
    ComponentInfo infoRedis = new ComponentInfo(RedisSimulationSeriesSource.class, getClassifier());
    infoRedis.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    repo.registerComponent(infoRedis, instance);

    // Is caching needed? assume no for now
    if (_publishRest) {
      repo.getRestComponents().publish(infoResolver, new DataHistoricalTimeSeriesResolverResource(resolver, OpenGammaFudgeContext.getInstance()));
      repo.getRestComponents().publish(infoSource, new DataHistoricalTimeSeriesSourceResource(instance));
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RedisSimulationSeriesSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static RedisSimulationSeriesSourceComponentFactory.Meta meta() {
    return RedisSimulationSeriesSourceComponentFactory.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(RedisSimulationSeriesSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public RedisSimulationSeriesSourceComponentFactory.Meta metaBean() {
    return RedisSimulationSeriesSourceComponentFactory.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        return getClassifier();
      case -745461486:  // redisConnector
        return getRedisConnector();
      case -2024915987:  // redisPrefix
        return getRedisPrefix();
      case 1789464955:  // database
        return getDatabase();
      case 1652633173:  // simulationDate
        return getSimulationDate();
      case -614707837:  // publishRest
        return isPublishRest();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case -281470431:  // classifier
        setClassifier((String) newValue);
        return;
      case -745461486:  // redisConnector
        setRedisConnector((RedisConnector) newValue);
        return;
      case -2024915987:  // redisPrefix
        setRedisPrefix((String) newValue);
        return;
      case 1789464955:  // database
        setDatabase((Integer) newValue);
        return;
      case 1652633173:  // simulationDate
        setSimulationDate((LocalDate) newValue);
        return;
      case -614707837:  // publishRest
        setPublishRest((Boolean) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_classifier, "classifier");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RedisSimulationSeriesSourceComponentFactory other = (RedisSimulationSeriesSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getRedisConnector(), other.getRedisConnector()) &&
          JodaBeanUtils.equal(getRedisPrefix(), other.getRedisPrefix()) &&
          JodaBeanUtils.equal(getDatabase(), other.getDatabase()) &&
          JodaBeanUtils.equal(getSimulationDate(), other.getSimulationDate()) &&
          JodaBeanUtils.equal(isPublishRest(), other.isPublishRest()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRedisConnector());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRedisPrefix());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDatabase());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSimulationDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets connector to the Redis server.
   * @return the value of the property
   */
  public RedisConnector getRedisConnector() {
    return _redisConnector;
  }

  /**
   * Sets connector to the Redis server.
   * @param redisConnector  the new value of the property
   */
  public void setRedisConnector(RedisConnector redisConnector) {
    this._redisConnector = redisConnector;
  }

  /**
   * Gets the the {@code redisConnector} property.
   * @return the property, not null
   */
  public final Property<RedisConnector> redisConnector() {
    return metaBean().redisConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets prefix to append to redis keys when stored
   * @return the value of the property
   */
  public String getRedisPrefix() {
    return _redisPrefix;
  }

  /**
   * Sets prefix to append to redis keys when stored
   * @param redisPrefix  the new value of the property
   */
  public void setRedisPrefix(String redisPrefix) {
    this._redisPrefix = redisPrefix;
  }

  /**
   * Gets the the {@code redisPrefix} property.
   * @return the property, not null
   */
  public final Property<String> redisPrefix() {
    return metaBean().redisPrefix().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the redis database to connect to
   * @return the value of the property
   */
  public Integer getDatabase() {
    return _database;
  }

  /**
   * Sets the redis database to connect to
   * @param database  the new value of the property
   */
  public void setDatabase(Integer database) {
    this._database = database;
  }

  /**
   * Gets the the {@code database} property.
   * @return the property, not null
   */
  public final Property<Integer> database() {
    return metaBean().database().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the initial value to use for the simulation date - this may be modified later during runtime.
   * @return the value of the property
   */
  public LocalDate getSimulationDate() {
    return _simulationDate;
  }

  /**
   * Sets the initial value to use for the simulation date - this may be modified later during runtime.
   * @param simulationDate  the new value of the property
   */
  public void setSimulationDate(LocalDate simulationDate) {
    this._simulationDate = simulationDate;
  }

  /**
   * Gets the the {@code simulationDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> simulationDate() {
    return metaBean().simulationDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RedisSimulationSeriesSourceComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", RedisSimulationSeriesSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code redisConnector} property.
     */
    private final MetaProperty<RedisConnector> _redisConnector = DirectMetaProperty.ofReadWrite(
        this, "redisConnector", RedisSimulationSeriesSourceComponentFactory.class, RedisConnector.class);
    /**
     * The meta-property for the {@code redisPrefix} property.
     */
    private final MetaProperty<String> _redisPrefix = DirectMetaProperty.ofReadWrite(
        this, "redisPrefix", RedisSimulationSeriesSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code database} property.
     */
    private final MetaProperty<Integer> _database = DirectMetaProperty.ofReadWrite(
        this, "database", RedisSimulationSeriesSourceComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code simulationDate} property.
     */
    private final MetaProperty<LocalDate> _simulationDate = DirectMetaProperty.ofReadWrite(
        this, "simulationDate", RedisSimulationSeriesSourceComponentFactory.class, LocalDate.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", RedisSimulationSeriesSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "redisConnector",
        "redisPrefix",
        "database",
        "simulationDate",
        "publishRest");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case -745461486:  // redisConnector
          return _redisConnector;
        case -2024915987:  // redisPrefix
          return _redisPrefix;
        case 1789464955:  // database
          return _database;
        case 1652633173:  // simulationDate
          return _simulationDate;
        case -614707837:  // publishRest
          return _publishRest;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RedisSimulationSeriesSourceComponentFactory> builder() {
      return new DirectBeanBuilder<RedisSimulationSeriesSourceComponentFactory>(new RedisSimulationSeriesSourceComponentFactory());
    }

    @Override
    public Class<? extends RedisSimulationSeriesSourceComponentFactory> beanType() {
      return RedisSimulationSeriesSourceComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code redisConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<RedisConnector> redisConnector() {
      return _redisConnector;
    }

    /**
     * The meta-property for the {@code redisPrefix} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> redisPrefix() {
      return _redisPrefix;
    }

    /**
     * The meta-property for the {@code database} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> database() {
      return _database;
    }

    /**
     * The meta-property for the {@code simulationDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> simulationDate() {
      return _simulationDate;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
