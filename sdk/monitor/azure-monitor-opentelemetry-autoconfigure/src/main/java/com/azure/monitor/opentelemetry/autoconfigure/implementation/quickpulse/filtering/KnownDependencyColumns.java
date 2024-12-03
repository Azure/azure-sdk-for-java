// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class KnownDependencyColumns {
    public static final String TARGET = "Target";
    public static final String DURATION = "Duration";
    public static final String RESULT_CODE = "ResultCode";
    public static final String SUCCESS = "Success";
    public static final String TYPE = "Type";
    public static final String DATA = "Data";
    public static final String NAME = "Name";

    public static final Set<String> ALL_COLUMNS
        = new HashSet<>(asList(TARGET, DURATION, RESULT_CODE, SUCCESS, TYPE, DATA, NAME));
}
