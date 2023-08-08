// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import java.util.List;

public class LogsIngestionRequest {

    private final List<Object> logs;
    private final byte[] requestBody;

    public LogsIngestionRequest(List<Object> logs, byte[] requestBody) {
        this.logs = logs;
        this.requestBody = requestBody;
    }

    public List<Object> getLogs() {
        return logs;
    }

    public byte[] getRequestBody() {
        return requestBody;
    }
}
