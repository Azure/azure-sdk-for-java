// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import java.util.HashMap;

/**
 * Resolve Service Bus host, shared access key name, shared access key from Service Bus connection string.
 */
public class ConnectionStringResolver {

    private static final String ENDPOINT = "Endpoint";
    private static final String HOST = "host";
    private static final String SAS_KEY_NAME = "SharedAccessKeyName";
    private static final String SAS_KEY = "SharedAccessKey";

    public static ServiceBusKey getServiceBusKey(String connectionString) {
        final String[] segments = connectionString.split(";");
        final HashMap<String, String> hashMap = new HashMap<>();

        for (final String segment : segments) {
            final int indexOfEqualSign = segment.indexOf("=");
            final String key = segment.substring(0, indexOfEqualSign);
            final String value = segment.substring(indexOfEqualSign + 1);
            hashMap.put(key, value);
        }

        final String endpoint = hashMap.get(ENDPOINT);
        final String[] segmentsOfEndpoint = endpoint.split("/");
        final String host = segmentsOfEndpoint[segmentsOfEndpoint.length - 1];
        hashMap.put(HOST, host);

        return new ServiceBusKey(hashMap.get(HOST), hashMap.get(SAS_KEY_NAME), hashMap.get(SAS_KEY));
    }

}
