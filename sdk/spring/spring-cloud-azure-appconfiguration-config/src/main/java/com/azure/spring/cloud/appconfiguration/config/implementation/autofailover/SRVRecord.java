// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

public class SRVRecord {

    private final int priority;

    private final int weight;

    private final int port;

    private final String target;
    
    private static final String PROTOCAL = "https://";

    public SRVRecord(String[] record) {
        this.priority = Integer.valueOf(record[0]);
        this.weight = Integer.valueOf(record[1]);
        this.port = Integer.valueOf(record[2]);
        this.target = record[3].substring(0, record[3].length() - 1);
    }

    public int getPriority() {
        return priority;
    }

    public int getWeight() {
        return weight;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public String getEndpoint() {
        return PROTOCAL + target;
    }

    public int compareTo(SRVRecord record) {
        if (priority > record.getPriority()) {
            return 1;
        }
        if (record.getPriority() > priority) {
            return -1;
        }

        if (weight > record.getWeight()) {
            return 1;
        }
        if (record.getWeight() > weight) {
            return -1;
        }

        return 0;
    }
}
