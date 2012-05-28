/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.opengamma.DataNotFoundException;

/**
 *
 */
public class AnalyticsViewManager {

  private final Map<String, AnalyticsView> _views = new ConcurrentHashMap<String, AnalyticsView>();
  private final AtomicLong _nextViewId = new AtomicLong(0);

  public String createView(ViewRequest request) {
    long viewId = _nextViewId.getAndIncrement();
    String viewIdStr = Long.toString(viewId);
    _views.put(viewIdStr, new SimpleAnalyticsView(request));
    return viewIdStr;
  }

  public void deleteView(String viewId) {
    AnalyticsView view = _views.remove(viewId);
    if (view == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    view.close();
  }

  public AnalyticsView getView(String viewId) {
    AnalyticsView view = _views.get(viewId);
    if (view == null) {
      throw new DataNotFoundException("No view found with ID " + viewId);
    }
    return view;
  }
}
