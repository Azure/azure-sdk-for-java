// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;

/**
 * This class stores some basic information about college classes and the time they start.
 */
public class ClassTime {

    /**
     * All the possible departments/subjects for classes.
     */
    public enum Department {
        MATH,
        PHYS,
        BIOL,
        ENGL,
        CSE
    }

    private final Department department;

    private final int courseNumber;

    private final OffsetDateTime startTime;

    /**
     * Creates a new instance containing information about the department, course number, and start time of the class.
     * @param department the department of the class, e.g. MATH.
     * @param courseNumber the course number of the class, e.g. 225 or 101.
     * @param time the time the class starts today, e.g. 3:15 pm.
     */
    public ClassTime(Department department, int courseNumber, OffsetDateTime time) {
        this.department = department;
        this.courseNumber = courseNumber;
        this.startTime = time;
    }

    /**
     * Creates a random instance of a class, containing a random department, random realistic course number, and a start
     * time that is randomly selected from any 15 minute interval from 7:00 am to 6:00 pm today.
     * @param random the random element to use to create the class and time.
     * @return a random instance of this class and time.
     */
    public static ClassTime getRandom(Random random) {
        Department department = Department.values()[random.nextInt(Department.values().length)];
        int courseNumber = random.nextInt(500) + 100;
        OffsetDateTime time = OffsetDateTime.of(LocalDate.now(),
            LocalTime.MIDNIGHT.plusHours(7).plusMinutes(random.nextInt(44) * 15), ZoneOffset.UTC);
        return new ClassTime(department, courseNumber, time);
    }

    /**
     * Get the starting time of this class.
     * @return the start time.
     */
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    /**
     * Get the department/subject of the class.
     * @return the department.
     */
    public String getDepartment() {
        return department.toString();
    }

    /**
     * Get the course number of this class.
     * @return the class course number.
     */
    public int getCourseNumber() {
        return courseNumber;
    }

    @Override
    public String toString() {
        return getDepartment() + " " + getCourseNumber() + " starts at: " + getStartTime().toLocalTime().toString();
    }
}
