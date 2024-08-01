// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import java.util.List;

import static java.util.Collections.singletonList;

public final class Exceptions {

    public static List<ExceptionDetailBuilder> minimalParse(String str) {
        ExceptionDetailBuilder builder = new ExceptionDetailBuilder();
        builder.setStack(str);
        return singletonList(builder);
    }

    private Exceptions() {
    }
}
