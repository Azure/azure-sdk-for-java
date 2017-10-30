/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.google.common.hash.Hashing;
import com.microsoft.azure.v2.serializer.AzureJacksonAdapter;
import com.microsoft.rest.v2.RestClient;
import com.microsoft.rest.v2.ServiceClient;
import com.microsoft.rest.v2.credentials.ServiceClientCredentials;

import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class AzureServiceClient extends ServiceClient {
    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     * @param credentials the credentials
     */
    protected AzureServiceClient(String baseUrl, ServiceClientCredentials credentials) {
        this(new RestClient.Builder()
                .withBaseUrl(baseUrl)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .build());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param restClient the REST client
     */
    protected AzureServiceClient(RestClient restClient) {
        super(restClient);
    }

    /**
     * The default User-Agent header. Override this method to override the user agent.
     *
     * @return the user agent string.
     */
    public String userAgent() {
        return String.format("Azure-SDK-For-Java/%s OS:%s MacAddressHash:%s Java:%s",
                getClass().getPackage().getImplementationVersion(),
                OS,
                MAC_ADDRESS_HASH,
                JAVA_VERSION);
    }

    private static final String MAC_ADDRESS_HASH;
    private static final String OS;
    private static final String JAVA_VERSION;

    static {
        OS = System.getProperty("os.name") + "/" + System.getProperty("os.version");
        String macAddress = "Unknown";
        try {
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while (networks.hasMoreElements()) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();

                if (mac != null) {
                    macAddress = Hashing.sha256().hashBytes(mac).toString();
                    break;
                }
            }
        } catch (Throwable t) {
            // It's okay ignore mac address hash telemetry
        }
        MAC_ADDRESS_HASH = macAddress;
        String version = System.getProperty("java.version");
        JAVA_VERSION = version != null ? version : "Unknown";
    }
}
