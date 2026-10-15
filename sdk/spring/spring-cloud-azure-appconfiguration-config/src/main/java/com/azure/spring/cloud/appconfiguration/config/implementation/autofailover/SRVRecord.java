// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.autofailover;

class SRVRecord implements Comparable<SRVRecord> {

    private final int priority;

    private final int weight;

    private final int port;

    private final String target;
    
    private static final String PROTOCOL = "https://";

    SRVRecord(String[] record) {
        this.priority = Integer.parseInt(record[0]);
        this.weight = Integer.parseInt(record[1]);
        this.port = Integer.parseInt(record[2]);
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
        return PROTOCOL + target;
    }

    @Override
    public int compareTo(SRVRecord record) {
        if (priority != record.getPriority()) {
            return Integer.compare(priority, record.getPriority());
        }
        // Higher weight should be preferred (sorted first)
        return Integer.compare(record.getWeight(), weight);
    }
}
