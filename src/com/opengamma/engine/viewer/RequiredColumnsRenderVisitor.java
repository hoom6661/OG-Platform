/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 *
 * @author jim
 */
public class RequiredColumnsRenderVisitor implements RenderVisitor<List<String>> {

  @Override
  public List<String> visitDiscountCurve(DiscountCurve discountCurve) {
    Set<Double> keySet = discountCurve.getData().keySet();
    List<String> results = new ArrayList<String>();
    for (Double d : keySet) {
      results.add(d.toString());
    }
    return null;
  }

  @Override
  public List<String> visitDoubleTimeSeries(DoubleTimeSeries doubleTimeSeries) {
    return null;
  }

  @Override
  public List<String> visitGreekResultCollection(
      GreekResultCollection greekResultCollection) {
    List<String> results = new ArrayList<String>();
    for (Greek greek : greekResultCollection.keySet()) {
      results.add(greek.name());
    }
    return results;
  }

  @Override
  public List<String> visitVolatilitySurface(VolatilitySurface volatilitySurface) {
    return null;
  }

}
