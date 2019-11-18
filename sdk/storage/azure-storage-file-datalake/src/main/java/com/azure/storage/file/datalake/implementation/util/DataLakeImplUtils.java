// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

public class DataLakeImplUtils {
    public static String endpointToDesiredEndpoint(String endpoint, String desiredEndpoint, String currentEndpoint) {
        // Add the . on either side to prevent overwriting an endpoint if a user provides a
        String desiredStringToMatch = "." + desiredEndpoint + ".";
        String currentStringToMatch = "." + currentEndpoint + ".";
        if (endpoint.contains(desiredStringToMatch)) {
            return endpoint;
        } else {
            return endpoint.replaceFirst(currentStringToMatch, desiredStringToMatch);
        }
    }
}
