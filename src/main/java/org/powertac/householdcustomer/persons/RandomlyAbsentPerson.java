/*
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.powertac.householdcustomer.persons;

import java.util.ListIterator;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import org.powertac.common.configurations.HouseholdConstants;
import org.powertac.common.enumerations.Status;

/**
 * This is the instance of the person type that works in shifts that may vary form week to week or
 * from month to month. The consequence is that he has little time for leisure activities.
 * @author Antonios Chrysopoulos
 * @version 1, 13/02/2011
 **/
public class RandomlyAbsentPerson extends WorkingPerson
{

  /**
   * This is the initialization function. It uses the variable values for the configuration file to
   * create the person as it should for this type.
   * @param AgentName
   * @param conf
   * @param publicVacationVector
   * @param gen
   * @return
   */
  public void initialize (String AgentName, Properties conf, Vector<Integer> publicVacationVector, Random gen)
  {
    // Variables Taken from the configuration file
    double sicknessMean = Double.parseDouble(conf.getProperty("SicknessMean"));
    double sicknessDev = Double.parseDouble(conf.getProperty("SicknessDev"));
    double leisureDurationMean = Double.parseDouble(conf.getProperty("LeisureDurationMean"));
    double leisureDurationDev = Double.parseDouble(conf.getProperty("LeisureDurationDev"));
    double RALeisure = Double.parseDouble(conf.getProperty("RALeisure"));
    double workingDurationMean = Double.parseDouble(conf.getProperty("WorkingDurationMean"));
    double workingDurationDev = Double.parseDouble(conf.getProperty("WorkingDurationDev"));
    double vacationDurationMean = Double.parseDouble(conf.getProperty("VacationDurationMean"));
    double vacationDurationDev = Double.parseDouble(conf.getProperty("VacationDurationDev"));

    // Filling the main variables
    name = AgentName;
    status = Status.Normal;

    // Filling the sickness and public Vacation Vectors
    sicknessVector = createSicknessVector(sicknessMean, sicknessDev, gen);
    this.publicVacationVector = publicVacationVector;
    // Filling the leisure variables
    int x = (int) (gen.nextGaussian() + RALeisure);
    leisureVector = createLeisureVector(x, gen);
    leisureDuration = (int) (leisureDurationDev * gen.nextGaussian() + leisureDurationMean);
    // Filling Working variables
    int work = workingDaysRandomizer(conf, gen);
    workingDays = createWorkingDaysVector(work, gen);
    workingStartHour = createWorkingStartHour(gen);
    workingDuration = (int) (workingDurationDev * gen.nextGaussian() + workingDurationMean);
    // Filling Vacation Variables
    vacationDuration = (int) (vacationDurationDev * gen.nextGaussian() + vacationDurationMean);
    vacationVector = createVacationVector(vacationDuration, gen);
  }

  /**
   * This function selects the shift of the worker. There three different shifts: 00:00 - 08:00
   * 08:00 - 16:00 and 16:00 - 24:00.
   * @param gen
   * @return
   */
  int createWorkingStartHour (Random gen)
  {
    int x = gen.nextInt(HouseholdConstants.NUMBER_OF_SHIFTS);
    return (x * HouseholdConstants.HOURS_OF_SHIFT_WORK * HouseholdConstants.QUARTERS_OF_HOUR);
  }

  /**
   * This function fills out the leisure activities in the daily schedule of the person in question.
   * @param weekday
   * @param gen
   * @return
   */
  void addLeisureWorking (int weekday, Random gen)
  {
    // Create auxiliary variables
    ListIterator<Integer> iter = leisureVector.listIterator();
    Status st;
    while (iter.hasNext()) {
      if (iter.next() == weekday) {
        int start = workingStartHour + workingDuration;
        if (workingStartHour == HouseholdConstants.SHIFT_START_1) {
          int startq = gen.nextInt((HouseholdConstants.LEISURE_WINDOW + 1) - start) + (start + HouseholdConstants.SHIFT_START_2);
          for (int i = startq; i < startq + leisureDuration; i++) {
            st = Status.Leisure;
            dailyRoutine.set(i, st);
            if (i == HouseholdConstants.QUARTERS_OF_DAY - 1)
              break;
          }
        } else {
          if (workingStartHour == HouseholdConstants.SHIFT_START_2) {
            int startq = start + gen.nextInt(HouseholdConstants.LEISURE_WINDOW_SHIFT - start);
            for (int i = startq; i < startq + leisureDuration; i++) {
              st = Status.Leisure;
              dailyRoutine.set(i, st);
              if (i == HouseholdConstants.QUARTERS_OF_DAY - 1)
                break;
            }
          } else {
            int startq = HouseholdConstants.SHIFT_START_2 + gen.nextInt(HouseholdConstants.SHIFT_START_3 - (HouseholdConstants.LEISURE_WINDOW - 1));
            for (int i = startq; i < startq + leisureDuration; i++) {
              st = Status.Leisure;
              dailyRoutine.set(i, st);
              if (i == HouseholdConstants.QUARTERS_OF_DAY - 1)
                break;
            }
          }
        }
      }
    }
  }

  @Override
  void fillWork ()
  {
    // Create auxiliary variables
    Status st;
    if (workingStartHour == HouseholdConstants.SHIFT_START_1) {
      for (int i = HouseholdConstants.SHIFT_START_1; i < workingDuration; i++) {
        st = Status.Working;
        dailyRoutine.set(i, st);
      }
      for (int i = workingDuration; i < workingDuration + HouseholdConstants.SHIFT_START_2; i++) {
        st = Status.Sleeping;
        dailyRoutine.set(i, st);
      }
      for (int i = workingDuration + HouseholdConstants.SHIFT_START_2; i < HouseholdConstants.QUARTERS_OF_DAY; i++) {
        st = Status.Normal;
        dailyRoutine.set(i, st);
      }
    } else {
      if (workingStartHour == HouseholdConstants.SHIFT_START_2) {
        for (int i = HouseholdConstants.START_OF_SLEEPING_1; i < HouseholdConstants.END_OF_SLEEPING_1; i++) {
          st = Status.Sleeping;
          dailyRoutine.set(i, st);
        }
        for (int i = HouseholdConstants.END_OF_SLEEPING_1; i < HouseholdConstants.SHIFT_START_2; i++) {
          st = Status.Normal;
          dailyRoutine.set(i, st);
        }
        for (int i = HouseholdConstants.SHIFT_START_2; i < workingDuration + HouseholdConstants.SHIFT_START_2; i++) {
          st = Status.Working;
          dailyRoutine.set(i, st);
        }
        for (int i = workingDuration + HouseholdConstants.SHIFT_START_2; i < HouseholdConstants.START_OF_SLEEPING_1; i++) {
          st = Status.Normal;
          dailyRoutine.set(i, st);
        }
        for (int i = HouseholdConstants.START_OF_SLEEPING_1; i < HouseholdConstants.QUARTERS_OF_DAY; i++) {
          st = Status.Sleeping;
          dailyRoutine.set(i, st);
        }
      } else {
        for (int i = HouseholdConstants.START_OF_SLEEPING_1; i < HouseholdConstants.END_OF_SLEEPING_1; i++) {
          st = Status.Sleeping;
          dailyRoutine.set(i, st);
        }
        for (int i = HouseholdConstants.END_OF_SLEEPING_1; i < HouseholdConstants.SHIFT_START_3; i++) {
          st = Status.Normal;
          dailyRoutine.set(i, st);
        }
        if (workingDuration > HouseholdConstants.HOURS_OF_SHIFT_WORK * HouseholdConstants.QUARTERS_OF_HOUR) {
          for (int i = HouseholdConstants.SHIFT_START_3; i < HouseholdConstants.QUARTERS_OF_DAY; i++) {
            st = Status.Working;
            dailyRoutine.set(i, st);
          }
        } else {
          for (int i = HouseholdConstants.SHIFT_START_3; i < HouseholdConstants.SHIFT_START_3 + workingDuration; i++) {
            if (i >= HouseholdConstants.QUARTERS_OF_DAY)
              break;
            st = Status.Working;
            dailyRoutine.set(i, st);
          }
          for (int i = HouseholdConstants.SHIFT_START_3 + workingDuration; i < HouseholdConstants.QUARTERS_OF_DAY; i++) {
            st = Status.Sleeping;
            dailyRoutine.set(i, st);
          }
        }
      }
    }
  }

  @Override
  public void refresh (Properties conf, Random gen)
  {
    // Renew Variables
    double leisureDurationMean = Double.parseDouble(conf.getProperty("LeisureDurationMean"));
    double leisureDurationDev = Double.parseDouble(conf.getProperty("LeisureDurationDev"));
    double RALeisure = Double.parseDouble(conf.getProperty("RALeisure"));
    double vacationAbsence = Double.parseDouble(conf.getProperty("VacationAbsence"));

    int work = workingDaysRandomizer(conf, gen);
    workingDays = createWorkingDaysVector(work, gen);
    workingStartHour = createWorkingStartHour(gen);

    int x = (int) (gen.nextGaussian() + RALeisure);
    leisureDuration = (int) (leisureDurationDev * gen.nextGaussian() + leisureDurationMean);
    leisureVector = createLeisureVector(x, gen);

    for (int i = 0; i < HouseholdConstants.DAYS_OF_WEEK; i++) {
      fillDailyRoutine(i, vacationAbsence, gen);
      weeklyRoutine.add(dailyRoutine);
    }
  }

}
