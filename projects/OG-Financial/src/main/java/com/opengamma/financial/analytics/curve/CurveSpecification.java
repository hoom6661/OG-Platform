/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CurveSpecification implements Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  private final LocalDate _curveDate;
  private final String _name;
  private final UniqueIdentifiable _id;
  private final SortedSet<CurveNodeWithIdentifier> _strips;

  public CurveSpecification(final LocalDate curveDate, final String name, final UniqueIdentifiable id, final Collection<CurveNodeWithIdentifier> strips) {
    ArgumentChecker.notNull(curveDate, "curve date");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(id, "id");
    ArgumentChecker.notNull(strips, "strips");
    _curveDate = curveDate;
    _name = name;
    _id = id;
    _strips = new TreeSet<>(strips);
  }

  public void addStrip(final CurveNodeWithIdentifier strip) {
    ArgumentChecker.notNull(strip, "strip");
    _strips.add(strip);
  }

  public LocalDate getCurveDate() {
    return _curveDate;
  }

  public String getName() {
    return _name;
  }

  public UniqueIdentifiable getIdentifier() {
    return _id;
  }

  public Set<CurveNodeWithIdentifier> getStrips() {
    return Collections.unmodifiableSet(_strips);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveDate.hashCode();
    result = prime * result + _id.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + _strips.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CurveSpecification)) {
      return false;
    }
    final CurveSpecification other = (CurveSpecification) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveDate, other._curveDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_id, other._id)) {
      return false;
    }
    if (!ObjectUtils.equals(_strips, other._strips)) {
      return false;
    }
    return true;
  }

}
