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
 * This is the instance of the person type that spents most of its time inside the house. Such types
 * are children or elderly people. These persons don't work at all, so they have more time for
 * leisure activities.
 * @author Antonios Chrysopoulos
 * @version 1, 13/02/2011
 */
public class MostlyPresentPerson extends Person
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
    double MPLeisure = Double.parseDouble(conf.getProperty("MPLeisure"));

    // Filling the main variables
    name = AgentName;
    status = Status.Normal;
    sicknessVector = createSicknessVector(sicknessMean, sicknessDev, gen);
    this.publicVacationVector = publicVacationVector;
    int x = (int) (gen.nextGaussian() + MPLeisure);
    leisureVector = createLeisureVector(x, gen);
    leisureDuration = (int) (leisureDurationDev * gen.nextGaussian() + leisureDurationMean);
  }

  @Override
  public void showInfo ()
  {
    // Printing base variables
    log.info("Name = " + name);
    log.info("Member Of = " + memberOf.toString());

    // Printing Sickness variables
    log.info("Sickness Days = ");
    ListIterator<Integer> iter = sicknessVector.listIterator();
    while (iter.hasNext())
      log.info(iter.next());

    // Printing Leisure variables
    log.info("Leisure Days of Week = ");
    iter = leisureVector.listIterator();
    while (iter.hasNext())
      log.info(iter.next());
    log.info("Leisure Duration = " + leisureDuration);

    // Printing Public Vacation Variables
    log.info("Public Vacation of Year = ");
    iter = publicVacationVector.listIterator();
    while (iter.hasNext())
      log.info(iter.next());

    // Printing Weekly Schedule
    log.info("Weekly Routine Length : " + weeklyRoutine.size());
    log.info("Weekly Routine : ");

    for (int i = 0; i < HouseholdConstants.DAYS_OF_WEEK; i++) {
      log.info("Day " + (i));
      ListIterator<Status> iter2 = weeklyRoutine.get(i).listIterator();
      for (int j = 0; j < HouseholdConstants.QUARTERS_OF_DAY; j++)
        log.info("Quarter : " + (j + 1) + " Status : " + iter2.next());
    }
  }

  @Override
  public void refresh (Properties conf, Random gen)
  {

    // Renew Variables
    double leisureDurationMean = Double.parseDouble(conf.getProperty("LeisureDurationMean"));
    double leisureDurationDev = Double.parseDouble(conf.getProperty("LeisureDurationDev"));
    double MPLeisure = Double.parseDouble(conf.getProperty("MPLeisure"));
    double vacationAbsence = Double.parseDouble(conf.getProperty("VacationAbsence"));

    int x = (int) (gen.nextGaussian() + MPLeisure);
    leisureDuration = (int) (leisureDurationDev * gen.nextGaussian() + leisureDurationMean);
    leisureVector = createLeisureVector(x, gen);
    for (int i = 0; i < HouseholdConstants.DAYS_OF_WEEK; i++) {
      fillDailyRoutine(i, vacationAbsence, gen);
      weeklyRoutine.add(dailyRoutine);
    }
  }
}
