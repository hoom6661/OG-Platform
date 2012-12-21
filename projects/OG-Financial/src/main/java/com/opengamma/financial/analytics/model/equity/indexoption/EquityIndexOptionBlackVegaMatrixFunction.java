/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.equity.DerivativeSensitivityCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionPresentValueCalculator;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix2D;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.VegaMatrixHelper;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;

/**
 * Calculates the bucketed vega of an equity index option using the Black formula.
 */
public class EquityIndexOptionBlackVegaMatrixFunction  extends EquityIndexOptionFunction {
  /** The Black present value calculator */
  private static final EquityIndexOptionPresentValueCalculator PVC = EquityIndexOptionPresentValueCalculator.getInstance();
  /** Calculates derivative sensitivities */
  private static final DerivativeSensitivityCalculator CALCULATOR = new DerivativeSensitivityCalculator(PVC);
  /** The format for the output */
  private static final DecimalFormat FORMATTER = new DecimalFormat("#.##");
  /** The shift to use in bumping */
  private static final double SHIFT = 0.0001; // FIXME This really should be configurable by the user!

  /**
   * Default constructor
   */
  public EquityIndexOptionBlackVegaMatrixFunction() {
    super(ValueRequirementNames.VEGA_QUOTE_MATRIX, FXOptionBlackFunction.BLACK_METHOD);
  }

  @Override
  protected Set<ComputedValue> computeValues(final EquityIndexOption derivative, final StaticReplicationDataBundle market, final FunctionInputs inputs,
      final Set<ValueRequirement> desiredValues, final ValueSpecification resultSpec) {
    final NodalDoublesSurface vegaSurface = CALCULATOR.calcBlackVegaForEntireSurface(derivative, market, SHIFT);
    final Double[] xValues;
    final Double[] yValues;
    if (market.getVolatilitySurface() instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      final BlackVolatilitySurfaceMoneynessFcnBackedByGrid volDataBundle = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) market.getVolatilitySurface();
      xValues = ArrayUtils.toObject(volDataBundle.getGridData().getExpiries());
      final double[][] strikes2d = volDataBundle.getGridData().getStrikes();
      final Set<Double> strikeSet = new HashSet<Double>();
      for (final double[] element : strikes2d) {
        strikeSet.addAll(Arrays.asList(ArrayUtils.toObject(element)));
      }
      yValues = strikeSet.toArray(new Double[] {});
    } else {
      xValues = vegaSurface.getXData();
      yValues = vegaSurface.getYData();
    }

    final Set<Double> xSet = new HashSet<Double>(Arrays.asList(xValues));
    final Set<Double> ySet = new HashSet<Double>(Arrays.asList(yValues));
    final Double[] uniqueX = xSet.toArray(new Double[0]);
    final String[] expLabels = new String[uniqueX.length];
    // Format the expiries for display
    for (int i = 0; i < uniqueX.length; i++) {
      uniqueX[i] = roundTwoDecimals(uniqueX[i]);
      expLabels[i] = VegaMatrixHelper.getFXVolatilityFormattedExpiry(uniqueX[i]);
    }
    final Double[] uniqueY = ySet.toArray(new Double[0]);
    final double[][] values = new double[ySet.size()][xSet.size()];
    int i = 0;
    for (final Double x : xSet) {
      int j = 0;
      for (final Double y : ySet) {
        double vega;
        try {
          vega = vegaSurface.getZValue(x, y);
        } catch (final IllegalArgumentException e) {
          vega = 0;
        }
        values[j++][i] = vega;
      }
      i++;
    }
    final DoubleLabelledMatrix2D matrix = new DoubleLabelledMatrix2D(uniqueX, expLabels, uniqueY, uniqueY, values);
    return Collections.singleton(new ComputedValue(resultSpec, matrix));
  }

  private double roundTwoDecimals(final double d) {
    return Double.valueOf(FORMATTER.format(d));
  }

  @Override
  /* The VegaMatrixFunction advertises the particular underlying Bloomberg ticker that it applies to. The target must share this underlying. */
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final String bbgTicker = getBloombergTicker(OpenGammaCompilationContext.getHistoricalTimeSeriesSource(context), ((EquityIndexOptionSecurity) target.getSecurity()).getUnderlyingId());
    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), createValueProperties(target, bbgTicker).get()));
  }

  /* We specify one additional property, the UnderlyingTicker, to allow a View to contain a VegaQuoteMatrix for each VolMatrix */
  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return super.createValueProperties(target)
      .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
      .with(ValuePropertyNames.UNDERLYING_TICKER); //TODO do we need this given that the target is the security?
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target, final ValueRequirement desiredValue) {
    throw new UnsupportedOperationException();
  }

  /* We specify one additional property, the UnderlyingTicker, to allow a View to contain a VegaQuoteMatrix for each VolMatrix */
  private ValueProperties.Builder createValueProperties(final ComputationTarget target, final String bbgTicker) {
    return super.createValueProperties(target)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.EQUITY_OPTION)
        .with(ValuePropertyNames.UNDERLYING_TICKER, bbgTicker);
  }

}
