// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.credentials;

import com.azure.applicationconfig.ConfigurationAsyncClientBuilder;
import com.azure.applicationconfig.policy.ConfigurationCredentialsPolicy;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;

/**
 * Credentials that authorizes requests to Azure Application Configuration. It uses content within the HTTP request to
 * generate the correct "Authorization" header value. {@link ConfigurationCredentialsPolicy} ensures that the content
 * exists in the HTTP request so that a valid authorization value is generated.
 *
 * @see ConfigurationCredentialsPolicy
 * @see ConfigurationAsyncClientBuilder
 */
public class ConfigurationClientCredentials {
    private static final String ENDPOINT = "endpoint=";
    private static final String ID = "id=";
    private static final String SECRET = "secret=";

    private URL baseUri;
    private String id;
    private byte[] secret;

    /**
     * Creates an instance that is able to authorize requests to Azure Application Configuration service.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};secret={secret_value}"
     */
    public ConfigurationClientCredentials(String connectionString) {
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalArgumentException(connectionString);
        }

        String[] args = connectionString.split(";");
        if (args.length < 3) {
            throw new IllegalArgumentException("invalid connection string segment count");
        }

        for (String arg : args) {
            String segment = arg.trim();
            String lowerCase = segment.toLowerCase(Locale.US);

            if (lowerCase.startsWith(ENDPOINT)) {
                try {
                    this.baseUri = new URL(segment.substring(ENDPOINT.length()));
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException(ex);
                }
            } else if (lowerCase.startsWith(ID)) {
                this.id = segment.substring(ID.length());
            } else if (lowerCase.startsWith(SECRET)) {
                String secretBase64 = segment.substring(SECRET.length());
                this.secret = Base64.getDecoder().decode(secretBase64);
            }
        }

        if (this.baseUri == null || this.id == null || this.secret == null) {
            throw new IllegalArgumentException("Could not parse 'connectionString'."
                + " Expected format: 'endpoint={endpoint};id={id};secret={secret}'. Actual:" + connectionString);
        }
    }

    /**
     * Gets the base URI of the Azure App Configuration instance based on the provided connection string.
     *
     * @return The base URI of the configuration service extracted from connection string provided.
     */
    public URL baseUri() {
        return baseUri;
    }

    public String id() {
        return id;
    }

    public byte[] secret() {
        return secret;
    }
}
