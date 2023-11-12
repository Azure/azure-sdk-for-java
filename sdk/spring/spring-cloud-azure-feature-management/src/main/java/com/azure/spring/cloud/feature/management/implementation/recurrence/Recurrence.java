package com.azure.spring.cloud.feature.management.implementation.recurrence;

/**
 * A recurrence definition describing how time window recurs
 * */
public class Recurrence {
    /**
     * The recurrence pattern specifying how often the time window repeats
     * */
    private RecurrencePattern pattern;
    /**
     * The recurrence range specifying how long the recurrence pattern repeats
     * */
    private RecurrenceRange range;

    /**
     * @return the recurrence pattern specifying how often the time window repeats
     * */
    public RecurrencePattern getPattern() {
        return pattern;
    }

    /**
     * @param pattern the recurrence pattern to be set
     * */
    public void setPattern(RecurrencePattern pattern) {
        this.pattern = pattern;
    }

    /**
     * @return the recurrence range specifying how long the recurrence pattern repeats
     * */
    public RecurrenceRange getRange() {
        return range;
    }

    /**
     * @param range the recurrence range to be set
     * */
    public void setRange(RecurrenceRange range) {
        this.range = range;
    }
}
