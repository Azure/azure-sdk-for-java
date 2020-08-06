/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import java.util.HashMap;

public class ConnectionStringResolver {

    private static final String ENDPOINT = "Endpoint";
    private static final String HOST = "host";
    private static final String SAS_KEY_NAME = "SharedAccessKeyName";
    private static final String SAS_KEY = "SharedAccessKey";

    public static ServiceBusKey getServiceBusKey(String connectionString) {
        String[] segments = connectionString.split(";");
        HashMap<String, String> hashMap = new HashMap<>();

        for (String segment : segments) {
            int indexOfEqualSign = segment.indexOf("=");
            String key = segment.substring(0, indexOfEqualSign);
            String value = segment.substring(indexOfEqualSign + 1);
            hashMap.put(key, value);
        }

        String endpoint = hashMap.get(ENDPOINT);
        String[] segmentsOfEndpoint = endpoint.split("/");
        String host = segmentsOfEndpoint[segmentsOfEndpoint.length - 1];
        hashMap.put(HOST, host);

        return new ServiceBusKey(hashMap.get(HOST),
                hashMap.get(SAS_KEY_NAME), hashMap.get(SAS_KEY));
    }

}
