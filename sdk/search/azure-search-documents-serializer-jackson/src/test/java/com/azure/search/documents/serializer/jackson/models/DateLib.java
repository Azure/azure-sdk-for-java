// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateLib {
    @JsonProperty(value = "id")
    private String id;
    @JsonProperty(value = "date")
    private Date date;

    public String getId() {
        return id;
    }

    public DateLib setId(String id) {
        this.id = id;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public DateLib setDate(Date date) {
        this.date = date;
        return this;
    }

    public String toString() {
        return String.format("{\"id\":\"%s\",\"date\":\"%s\"}",
            id, date.toInstant().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
