// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@SuppressWarnings("ALL")
public class Entry {

    @JsonProperty
    Date date;

    public Date date() {
        return (date == null) ? null : (Date) date.clone();
    }

    public void date(Date date) {
        this.date = (date == null) ? null : (Date) date.clone();
    }
}
