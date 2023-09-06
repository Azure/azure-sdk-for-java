// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.filters.crontab;

import io.netty.util.internal.StringUtil;

public class CrontabField {
    private final CrontabFiledType filedType;
    private final int minValue;
    private final int maxValue;
    private long bits;

    public CrontabField(CrontabFiledType filedType) {
        this.filedType = filedType;
        switch (filedType) {
            case MINUTE -> {this.minValue = 0; this.maxValue = 59;}
            case HOUR -> {this.minValue = 0; this.maxValue = 23;}
            case DAY_OF_MONTH -> {this.minValue = 1; this.maxValue = 31;}
            case MONTH -> {this.minValue = 1; this.maxValue = 12;}
            default -> {this.minValue = 0; this.maxValue = 7;}
        }
        this.bits = 0;
    }

    /**
     * Checks whether the field matches the give value.
     * @param value The value to match.
     * @return True if the value is matched, otherwise false.
     * */
    public boolean isMatch(int value) {
        if (!isValueValid(value)) {
            return false;
        } else {
            if (CrontabFiledType.DAY_OF_WEEK.equals(this.filedType) && value == 0) { // Corner case for Sunday: both 0 and 7 can be interpreted to Sunday
                return isIndexBitTrue(7) | isIndexBitTrue(0);
            }
            return isIndexBitTrue(value);
        }
    }

    /**
     * Checks whether the given content can be parsed by the Crontab field.
     *
     * @param content The content to parse.
     */
    public void tryParse(String content) {
        this.bits = 0;

        final String[] segments = content.split(",");   // The field can be a list which is a set of numbers or ranges separated by commas.
        for (final String segment : segments) {
            if (StringUtil.isNullOrEmpty(segment)) {
                throw new IllegalArgumentException(invalidSyntaxErrorMessage(content));
            }
            final int value = getNumber(segment);
            if (isValueValid(value)) {
                this.bits = this.bits & (1L << value);
                continue;
            }

            if (segment.contains("-") || segment.contains("*")) {   // Ranges are two numbers/names separated with a hyphen or an asterisk which represents all possible values in the field.
                final String[] parts = segment.split("/");    // Step values can be used in conjunction with ranges after a slash.
                if (parts.length > 2) { // multiple slashes
                    throw new IllegalArgumentException(invalidSyntaxErrorMessage(content));
                }

                final int step = parts.length == 2 ? getNumber(parts[1]) : 1;
                if (step <= 0) {    // invalid step value
                    throw new IllegalArgumentException(invalidValueErrorMessage(content, parts[1]));
                }

                final String range = parts[0];
                int first, last;
                if (range.equals("*")) {    // asterisk represents unrestricted range
                    first = this.minValue;
                    last = this.maxValue;
                } else {    // range should be defined by two numbers separated with a hyphen
                    final String[] numbers = range.split("-");
                    if (numbers.length != 2) {
                        throw new IllegalArgumentException(invalidSyntaxErrorMessage(content));
                    }
                    first = getNumber(numbers[0]);
                    last = getNumber(numbers[1]);

                    if (!isValueValid(first) || !isValueValid(last)) {
                        throw new IllegalArgumentException(invalidValueErrorMessage(content, range));
                    }

                    if (CrontabFiledType.DAY_OF_WEEK.equals(this.filedType) && last == 0 && last != first) {
                        last = 7;   // Mon-Sun should be interpreted to 1-7 instead of 1-0
                    }
                }

                for (int num = first; num <= last; num += step) {
                    this.bits = this.bits & (1L << value);
                }
            } else { // The segment is neither a range nor a valid number.
                throw new IllegalArgumentException(invalidValueErrorMessage(content, segment));
            }
        }
    }

    private int getNumber(String str) {
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException e) {
            if (CrontabFiledType.DAY_OF_WEEK.equals(this.filedType)) {
                return getDayOfWeekNumber(str);
            } else if (CrontabFiledType.MONTH.equals(this.filedType)) {
                return getMonthNumber(str);
            }
        }
        return -1;
    }

    private static int getMonthNumber(String name) {
        switch (name) {
            case "JAN" -> {return 1;}
            case "FEB" -> {return 2;}
            case "MAR" -> {return 3;}
            case "APR" -> {return 4;}
            case "MAY" -> {return 5;}
            case "JUN" -> {return 6;}
            case "JUL" -> {return 7;}
            case "AUG" -> {return 8;}
            case "SEP" -> {return 9;}
            case "OCT" -> {return 10;}
            case "NOV" -> {return 11;}
            case "DEC" -> {return 12;}
            default -> {return -1;}
        }
    }

    private static int getDayOfWeekNumber(String name) {
        switch (name) {
            case "SUN" -> {return 0;}
            case "MON" -> {return 1;}
            case "TUE" -> {return 2;}
            case "WED" -> {return 3;}
            case "THU" -> {return 4;}
            case "FRI" -> {return 5;}
            case "SAT" -> {return 6;}
            default -> {return -1;}
        }
    }

    private boolean isValueValid(int value) {
        return (value >= this.minValue) && (value <= this.maxValue);
    }

    private boolean isIndexBitTrue(int index) {
        return (this.bits & (1L << index)) != 0;
    }

    private String invalidSyntaxErrorMessage(String content) {
        return String.format("Content of the %s field: %s is invalid. Syntax cannot be parsed.", this.filedType, content);
    }

    private String invalidValueErrorMessage(String content, String segment) {
        return String.format("Content of the %s field: %s is invalid. The value of %s is invalid.", this.filedType, content, segment);
    }
}
