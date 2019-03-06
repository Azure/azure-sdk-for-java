// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class RevisionFilter {
    private String key;
    private String label;
    private String fields;
    private String range;
    private String acceptDatetime;

    public RevisionFilter withKey(String key) {
        this.key = key;
        return this;
    }

    public String key() {
        return key;
    }

    public RevisionFilter withLabel(String label) {
        this.label = label;
        return this;
    }

    public String label() {
        return label;
    }

    public RevisionFilter withFields(String fields) {
        this.fields = fields;
        return this;
    }

    public String fields() {
        return fields;
    }

    public RevisionFilter withRange(String range) {
        this.range = range;
        return this;
    }

    public String range() {
        return range;
    }

    public RevisionFilter withAcceptDatetime(OffsetDateTime datetime) {
        this.acceptDatetime = DateTimeFormatter.RFC_1123_DATE_TIME.toFormat().format(datetime);
        return this;
    }

    public String acceptDatetime() {
        return acceptDatetime;
    }
}
