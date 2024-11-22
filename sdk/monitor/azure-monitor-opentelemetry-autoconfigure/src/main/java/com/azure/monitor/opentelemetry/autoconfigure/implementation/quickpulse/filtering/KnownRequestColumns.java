// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class KnownRequestColumns {
    public static final String URL = "Url";
    public static final String DURATION = "Duration";
    public static final String RESPONSE_CODE = "ResponseCode";
    public static final String SUCCESS = "Success";
    public static final String NAME = "Name";

    public static final Set<String> allColumns = new HashSet<>(asList(
        URL,
        DURATION,
        RESPONSE_CODE,
        SUCCESS,
        NAME
    ));

}
