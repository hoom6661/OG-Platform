/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

/**
 * 
 */
public class InstrumentTypeProperties {
  /**
   * Name of the surface type property. This allows surfaces to be distinguished by instrument type (e.g. an FX volatility
   * surface, swaption ATM volatility surface).
   */
  public static final String PROPERTY_SURFACE_INSTRUMENT_TYPE = "InstrumentType";
  /** Property representing a surface for FX vanilla options */
  public static final String FOREX = "FX_VANILLA_OPTION";
  /** Property representing a surface for IR future options */
  public static final String IR_FUTURE_OPTION = "IR_FUTURE_OPTION";
  /** Property representing a curve for IR futures */
  public static final String IR_FUTURE_PRICE = "IR_FUTURE_PRICE";
  /** Property representing ATM surfaces for swaptions */
  public static final String SWAPTION_ATM = "SWAPTION_ATM";
  /** Property representing a surface for equity options */
  public static final String EQUITY_OPTION = "EQUITY_OPTION";
  /** Property representing a surface for bond future options */
  public static final String BOND_FUTURE_OPTION = "BOND_FUTURE_OPTION";

  /**
   * Name of the cube type property. This allows cubes to be distinguished by instrument type.
   */
  public static final String PROPERTY_CUBE_INSTRUMENT_TYPE = "CubeInstrumentType";
  /** Property representing a swaption volatility cube */
  public static final String SWAPTION_CUBE = "SWAPTION_CUBE";
}
