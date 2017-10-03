/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;
import rx.Observable;

import java.io.IOException;

/**
 * Managed Service Identity token based credentials for use with a REST Service Client.
 */
@Beta
public class MSICredentials extends AzureTokenCredentials {
    private final String resource;
    private final int msiPort;

    /**
     * Initializes a new instance of the MSICredentials.
     *
     * @param environment the Azure environment to use
     */
    public MSICredentials(AzureEnvironment environment) {
        this(environment, 50342);
    }

    /**
     * Initializes a new instance of the MSICredentials.
     *
     * @param environment the Azure environment to use
     * @param msiPort the local port to retrieve token from
     */
    public MSICredentials(AzureEnvironment environment, int msiPort) {
        super(environment, null /** retrieving MSI token does not require tenant **/);
        this.resource = environment.resourceManagerEndpoint();
        this.msiPort = msiPort;
    }

    @Override
    public String getToken(String resource) throws IOException {
        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(String.format("http://localhost:%d", this.msiPort))
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withSerializerAdapter(new AzureJacksonAdapter())
                .build();

        MSITokenService msiTokenService = restClient.retrofit().create(MSITokenService.class);
        Observable<Response<MSIToken>> msiResponse = msiTokenService.getTokenAsync("/oauth2/token",
                "true",
                this.resource);
        MSIToken msiToken = msiResponse.toBlocking().last().body();
        return msiToken.accessToken;
    }

    /**
     * The Retrofit service used for retrieving the MSI token.
     */
    private interface MSITokenService {
        @FormUrlEncoded
        @POST
        Observable<Response<MSIToken>> getTokenAsync(@Url String url, @Header("Metadata") String metadata, @Field("resource") String resource);
    }

    /**
     * Type representing response from the local MSI token provider.
     */
    private static class MSIToken {
        /**
         * Token type "Bearer".
         */
        @JsonProperty(value = "token_type")
        private String tokenType;

        /**
         * Access token.
         */
        @JsonProperty(value = "access_token")
        private String accessToken;
    }
}
