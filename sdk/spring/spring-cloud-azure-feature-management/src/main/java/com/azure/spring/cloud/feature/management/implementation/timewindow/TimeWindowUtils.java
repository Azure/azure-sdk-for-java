// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.feature.management.implementation.timewindow;

import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeWindowUtils {
    public static ZonedDateTime convertStringToDate(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        try {
            return ZonedDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (final DateTimeParseException e) {
            return ZonedDateTime.parse(timeStr, DateTimeFormatter.RFC_1123_DATE_TIME);
        }
    }
}
