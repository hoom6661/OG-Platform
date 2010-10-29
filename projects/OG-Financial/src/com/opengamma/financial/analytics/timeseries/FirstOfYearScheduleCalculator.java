/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.DateAdjusters;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class FirstOfYearScheduleCalculator extends Schedule {

  @Override
  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public LocalDate[] getSchedule(final LocalDate startDate, final LocalDate endDate) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == 1 && startDate.getMonthOfYear() == MonthOfYear.JANUARY) {
        return new LocalDate[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the first day of the year");
    }
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    LocalDate date = startDate.with(DateAdjusters.firstDayOfYear());
    if (date.isBefore(startDate)) {
      date = date.plusYears(1);
    }
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusYears(1);
    }
    return dates.toArray(EMPTY_LOCAL_DATE_ARRAY);
  }

  @Override
  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate, final boolean fromEnd, final boolean generateRecursive) {
    return getSchedule(startDate, endDate);
  }

  public ZonedDateTime[] getSchedule(final ZonedDateTime startDate, final ZonedDateTime endDate) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(endDate, "end date");
    Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate));
    if (startDate.equals(endDate)) {
      if (startDate.getDayOfMonth() == 1 && startDate.getMonthOfYear() == MonthOfYear.JANUARY) {
        return new ZonedDateTime[] {startDate};
      }
      throw new IllegalArgumentException("Start date and end date were the same but neither was the first day of the year");
    }
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = startDate.with(DateAdjusters.firstDayOfYear());
    if (date.isBefore(startDate)) {
      date = date.plusYears(1);
    }
    while (!date.isAfter(endDate)) {
      dates.add(date);
      date = date.plusYears(1);
    }
    return dates.toArray(EMPTY_ZONED_DATE_TIME_ARRAY);
  }
}
