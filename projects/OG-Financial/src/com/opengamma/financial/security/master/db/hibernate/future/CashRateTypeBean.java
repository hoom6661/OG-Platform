/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db.hibernate.future;

import com.opengamma.financial.security.master.db.hibernate.EnumBean;

public class CashRateTypeBean extends EnumBean {
  
  protected CashRateTypeBean() {
  }

  public CashRateTypeBean(String cashRateType) {
    super(cashRateType);
  }
  
}
