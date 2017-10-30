/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.v2.AzureEnvironment;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.v2.serializer.AzureJacksonAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Managed Service Identity token based credentials for use with a REST Service Client.
 */
@Beta
public class MSICredentials extends AzureTokenCredentials {
    private final String resource;
    private final int msiPort;
    private final AzureJacksonAdapter adapter;
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
        this.adapter = new AzureJacksonAdapter();
    }

    @Override
    public String getToken(String resource) throws IOException {
        URL url = new URL(String.format("http://localhost:%d/oauth2/token", this.msiPort));
        String postData = String.format("resource=%s", this.resource);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            connection.setRequestProperty("Metadata", "true");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            connection.setDoOutput(true);

            connection.connect();

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(postData);
            wr.flush();

            InputStream stream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"), 100);
            String result = reader.readLine();

            MSIToken msiToken = adapter.deserialize(result, MSIToken.class);
            return msiToken.accessToken;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
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
