/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.Annuity;
import com.opengamma.financial.interestrate.annuity.definition.ConstantCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.VariableAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.swap.definition.BasisSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public final class ParRateCurveSensitivityCalculator implements InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> {

  private final PresentValueCalculator _pvCalculator = PresentValueCalculator.getInstance();
  private final PresentValueSensitivityCalculator _pvSenseCalculator = PresentValueSensitivityCalculator.getInstance();

  private static ParRateCurveSensitivityCalculator s_instance;

  public static ParRateCurveSensitivityCalculator getInstance() {
    if (s_instance == null) {
      s_instance = new ParRateCurveSensitivityCalculator();
    }
    return s_instance;
  }

  private ParRateCurveSensitivityCalculator() {
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> getValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.notNull(instrument);
    Validate.notNull(curves);
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitCash(Cash cash, YieldCurveBundle curves) {
    String curveName = cash.getYieldCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    double ta = cash.getTradeTime();
    double tb = cash.getPaymentTime();
    double yearFrac = cash.getYearFraction();
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    if (yearFrac == 0.0) {
      if (ta != tb) {
        throw new IllegalArgumentException("year fraction is zero, but payment time not equal the trade time");
      }
      temp.add(new DoublesPair(ta, 1.0));
    } else {
      double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / yearFrac;
      temp.add(new DoublesPair(ta, -ta * ratio));
      temp.add(new DoublesPair(tb, tb * ratio));
    }
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitForwardRateAgreement(ForwardRateAgreement fra, YieldCurveBundle curves) {
    String curveName = fra.getLiborCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = fra.getFixingDate();
    final double tb = fra.getMaturity();
    final double delta = fra.getForwardYearFraction();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(s1);
    temp.add(s2);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitInterestRateFuture(InterestRateFuture future, YieldCurveBundle curves) {
    String curveName = future.getCurveName();
    YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = future.getSettlementDate();
    final double delta = future.getYearFraction();
    final double tb = ta + delta;

    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
    temp.add(s1);
    temp.add(s2);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitSwap(Swap swap, YieldCurveBundle curves) {
    Annuity payLeg = swap.getPayLeg().withZeroSpread();
    Annuity receiveLeg = swap.getReceiveLeg().withZeroSpread();
    Annuity spreadLeg = swap.getReceiveLeg().withUnitCoupons();

    double a = _pvCalculator.getValue(receiveLeg, curves);
    double b = _pvCalculator.getValue(payLeg, curves);
    double c = _pvCalculator.getValue(spreadLeg, curves);

    Map<String, List<Pair<Double, Double>>> senseA = _pvSenseCalculator.getValue(receiveLeg, curves);
    Map<String, List<Pair<Double, Double>>> senseB = _pvSenseCalculator.getValue(payLeg, curves);
    Map<String, List<Pair<Double, Double>>> senseC = _pvSenseCalculator.getValue(spreadLeg, curves);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();

    double factor = -(b - a) / c / c;

    for (String name : curves.getAllNames()) {
      boolean flag = false;
      List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
      if (senseA.containsKey(name)) {
        flag = true;
        for (Pair<Double, Double> pair : senseA.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), -pair.getSecond() / c));
        }
      }
      if (senseB.containsKey(name)) {
        flag = true;
        for (Pair<Double, Double> pair : senseB.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), pair.getSecond() / c));
        }
      }
      if (senseC.containsKey(name)) {
        flag = true;
        for (Pair<Double, Double> pair : senseC.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), factor * pair.getSecond()));
        }
      }
      if (flag) {
        result.put(name, temp);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitFixedFloatSwap(FixedFloatSwap swap, YieldCurveBundle curves) {
    FixedAnnuity tempAnnuity = swap.getFixedLeg().withUnitCoupons();
    double a = _pvCalculator.getValue(tempAnnuity, curves);
    double b = _pvCalculator.getValue(swap.getFloatingLeg(), curves);
    double bOveraSq = b / a / a;
    Map<String, List<Pair<Double, Double>>> senseA = _pvSenseCalculator.getValue(tempAnnuity, curves);
    Map<String, List<Pair<Double, Double>>> senseB = _pvSenseCalculator.getValue(swap.getFloatingLeg(), curves);

    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();
    for (String name : curves.getAllNames()) {
      boolean flag = false;
      List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
      if (senseA.containsKey(name)) {
        flag = true;
        for (Pair<Double, Double> pair : senseA.get(name)) {
          double t = pair.getFirst();
          DoublesPair newPair = new DoublesPair(t, -bOveraSq * pair.getSecond());
          temp.add(newPair);
        }
      }
      if (senseB.containsKey(name)) {
        flag = true;
        for (Pair<Double, Double> pair : senseB.get(name)) {
          double t = pair.getFirst();
          DoublesPair newPair = new DoublesPair(t, pair.getSecond() / a);
          temp.add(newPair);
        }
      }
      if (flag) {
        result.put(name, temp);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitBasisSwap(BasisSwap swap, YieldCurveBundle curves) {
    return visitSwap(swap, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitFloatingRateNote(FloatingRateNote frn, YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitBond(Bond bond, YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(bond.getCurveName());
    final FixedAnnuity ann = bond.getFixedAnnuity().withUnitCoupons();
    final double a = _pvCalculator.getValue(ann, curves);
    Map<String, List<Pair<Double, Double>>> senseA = _pvSenseCalculator.getValue(ann, curves);
    Map<String, List<Pair<Double, Double>>> result = new HashMap<String, List<Pair<Double, Double>>>();

    final double maturity = bond.getMaturity();
    final double df = curve.getDiscountFactor(maturity);
    final double factor = -(1 - df) / a / a;

    for (String name : curves.getAllNames()) {
      if (senseA.containsKey(name)) {
        List<Pair<Double, Double>> temp = new ArrayList<Pair<Double, Double>>();
        List<Pair<Double, Double>> list = senseA.get(name);
        int n = list.size();
        for (int i = 0; i < (n - 1); i++) {
          Pair<Double, Double> pair = list.get(i);
          temp.add(new DoublesPair(pair.getFirst(), factor * pair.getSecond()));
        }
        Pair<Double, Double> pair = list.get(n - 1);
        temp.add(new DoublesPair(pair.getFirst(), maturity * df / a + factor * pair.getSecond()));
        result.put(name, temp);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitFixedAnnuity(FixedAnnuity annuity, YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitVariableAnnuity(VariableAnnuity annuity, YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

  @Override
  public Map<String, List<Pair<Double, Double>>> visitConstantCouponAnnuity(ConstantCouponAnnuity annuity, YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

}
