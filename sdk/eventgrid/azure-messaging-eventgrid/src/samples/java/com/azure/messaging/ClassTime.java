package com.azure.messaging;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;

public class ClassTime {

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

    public ClassTime(Department department, int courseNumber, OffsetDateTime time) {
        this.department = department;
        this.courseNumber = courseNumber;
        this.startTime = time;
    }

    public static ClassTime getRandom(Random random) {
        Department department = Department.values()[random.nextInt(Department.values().length)];
        int courseNumber = random.nextInt(500) + 100;
        OffsetDateTime time = OffsetDateTime.of(LocalDate.now(),
            LocalTime.MIDNIGHT.plusHours(7).plusMinutes(random.nextInt(44) * 15), ZoneOffset.UTC);
        return new ClassTime(department, courseNumber, time);
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public String getDepartment() {
        return department.toString();
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    @Override
    public String toString() {
        return getDepartment() + " " + getCourseNumber() + " starts at: " + getStartTime().toLocalTime().toString();
    }
}
