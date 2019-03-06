// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class KeyLabelFilter {
    private String name;
    private String fields;
    private String range;
    private String acceptDatetime;

    public KeyLabelFilter withName(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public KeyLabelFilter withFields(String fields) {
        this.fields = fields;
        return this;
    }

    public String fields() {
        return fields;
    }

    public KeyLabelFilter withRange(String range) {
        this.range = range;
        return this;
    }

    public String range() {
        return range;
    }

    public KeyLabelFilter withAcceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return this;
    }

    public String acceptDatetime() {
        return acceptDatetime;
    }
}

