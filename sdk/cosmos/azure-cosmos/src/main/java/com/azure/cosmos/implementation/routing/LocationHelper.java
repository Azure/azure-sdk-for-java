// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava27.Strings;

import java.net.URI;

public class LocationHelper {
    /**
     * For example, for https://contoso.documents.azure.com:443/ and "West US", this will return https://contoso-westus.documents.azure.com:443/
     * NOTE: This ONLY called by client first boot when the input endpoint is not available.
     *
     * @param serviceEndpoint
     * @param location
     * @return
     */
    public static URI getLocationEndpoint(URI serviceEndpoint, String location) {

        // Split the host into 2 parts seperated by '.'
        // For example, "contoso.documents.azure.com" is separated into "contoso" and "documents.azure.com"
        // If the host doesn't contains '.', this will return the host as is, as the only element
        String[] hostParts = StringUtils.split(serviceEndpoint.getHost(), ".", 2);

        String host;
        if (hostParts.length != 0) {
            // hostParts[0] will be the global account name
            hostParts[0] = hostParts[0] + "-" + dataCenterToUriPostfix(location);

            // if hostParts has only one element, '.' is not included in the returned string
            host = String.join(".", hostParts);
        } else {
            host = serviceEndpoint.getHost();
        }

        try {
            return new URI(serviceEndpoint.getScheme(),
                    serviceEndpoint.getUserInfo(), host, serviceEndpoint.getPort(),
                    serviceEndpoint.getPath(), serviceEndpoint.getQuery(),
                    serviceEndpoint.getFragment());
        } catch (Exception e) {
            throw new RuntimeException(Strings.lenientFormat("invalid location [%s] or serviceEndpoint [%s]", location, serviceEndpoint.toString()), e);
        }
    }

    private static String dataCenterToUriPostfix(String dataCenter) {
        return dataCenter.replace(" ", "");
    }
}

