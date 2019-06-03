/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.rx.internal.routing;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;

public class LocationHelper {
    /**
     * For example, for https://contoso.documents.azure.com:443/ and "West US", this will return https://contoso-westus.documents.azure.com:443/
     * NOTE: This ONLY called by client first boot when the input endpoint is not available.
     *
     * @param serviceEndpoint
     * @param location
     * @return
     */
    public static URL getLocationEndpoint(URL serviceEndpoint, String location) {

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
            return new URL(serviceEndpoint.getProtocol(), host, serviceEndpoint.getPort(), serviceEndpoint.getFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String dataCenterToUriPostfix(String dataCenter) {
        return dataCenter.replace(" ", "");
    }
}

