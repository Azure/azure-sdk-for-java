// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventgrid.perf;

import java.time.OffsetDateTime;

class CustomEvent {
    private final String id;
    private final OffsetDateTime time;
    private final String subject;
    private final String foo;
    private final String type;
    private final String data;
    private final String dataVersion;

    CustomEvent(String id, OffsetDateTime time, String subject, String foo,
        String type, String data, String dataVersion) {
        this.id = id;
        this.time = time;
        this.subject = subject;
        this.foo = foo;
        this.type = type;
        this.data = data;
        this.dataVersion = dataVersion;
    }

    public String getData() {
        return data;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public String getDataVersion() {
        return dataVersion;
    }

    public String getFoo() {
        return foo;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getType() {
        return type;
    }
}
