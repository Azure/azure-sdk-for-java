/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.google.common.hash.Hashing;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * ServiceClient is the abstraction for accessing REST operations and their payload data types.
 */
public abstract class AzureServiceClient extends ServiceClient {
    protected AzureServiceClient(String baseUrl, ServiceClientCredentials credentials) {
        this(baseUrl, credentials, new OkHttpClient.Builder(), new Retrofit.Builder());
    }

    /**
     * Initializes a new instance of the ServiceClient class.
     *
     * @param baseUrl the service base uri
     * @param clientBuilder the http client builder
     * @param restBuilder the retrofit rest client builder
     */
    protected AzureServiceClient(String baseUrl, ServiceClientCredentials credentials, OkHttpClient.Builder clientBuilder, Retrofit.Builder restBuilder) {
        this(new RestClient.Builder(clientBuilder, restBuilder)
                .withBaseUrl(baseUrl)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
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
        return String.format("Azure-SDK-For-Java/%s OS:%s MacAddressHash:%s",
                getClass().getPackage().getImplementationVersion(),
                OS,
                MAC_ADDRESS_HASH);
    }

    private static final String MAC_ADDRESS_HASH;
    private static final String OS;

    static {
        OS = System.getProperty("os.name") + "/" + System.getProperty("os.version");
        String macAddress;
        try {
            macAddress = Hashing.sha256().hashBytes(NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress()).toString();
        } catch (Exception e) {
            macAddress = "Unknown";
        }
        MAC_ADDRESS_HASH = macAddress;
    }
}
