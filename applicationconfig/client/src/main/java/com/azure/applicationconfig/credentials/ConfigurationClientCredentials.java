// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.applicationconfig.credentials;

import com.azure.applicationconfig.ConfigurationAsyncClientBuilder;
import com.azure.applicationconfig.policy.ConfigurationCredentialsPolicy;
import com.azure.common.credentials.AsyncServiceClientCredentials;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpRequest;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Credentials that authorizes requests to Azure Application Configuration. It uses content within the HTTP request to
 * generate the correct "Authorization" header value. {@link ConfigurationCredentialsPolicy} ensures that the content
 * exists in the HTTP request so that a valid authorization value is generated.
 *
 * @see ConfigurationCredentialsPolicy
 * @see ConfigurationAsyncClientBuilder
 */
public class ConfigurationClientCredentials implements AsyncServiceClientCredentials {
    // "Host", "Date", and "x-ms-content-sha256" are required to generate "Authorization" value.
    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "Date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String[] SIGNED_HEADERS = new String[]{HOST_HEADER, DATE_HEADER, CONTENT_HASH_HEADER };

    private final CredentialInformation credentials;
    private final AuthorizationHeaderProvider headerProvider;

    /**
     * Creates an instance that is able to authorize requests to Azure Application Configuration service.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};secret={secret_value}"
     * @throws NoSuchAlgorithmException When the HMAC-SHA256 MAC algorithm cannot be instantiated.
     * @throws InvalidKeyException When the {@code connectionString} secret is invalid and cannot instantiate the HMAC-SHA256 algorithm.
     */
    public ConfigurationClientCredentials(String connectionString) throws InvalidKeyException, NoSuchAlgorithmException {
        credentials = new CredentialInformation(connectionString);
        headerProvider = new AuthorizationHeaderProvider(credentials);
    }

    /**
     * Gets the base URI of the Azure App Configuration instance based on the provided connection string.
     *
     * @return The base URI of the configuration service extracted from connection string provided.
     */
    public URL baseUri() {
        return this.credentials.baseUri();
    }

    /**
     * Gets the "Authentication" header value used authenticate the {@code request} with the configuration service.
     *
     * @param request HTTP request to send to the configuration service.
     * @return The "Authentication" header value.
     */
    @Override
    public Mono<String> authorizationHeaderValueAsync(HttpRequest request) {
        return Mono.just(headerProvider.getAuthenticationHeaderValue(request));
    }

    private static class AuthorizationHeaderProvider {
        private final String signedHeadersValue = String.join(";", SIGNED_HEADERS);
        private final CredentialInformation credentials;
        private final Mac sha256HMAC;

        AuthorizationHeaderProvider(CredentialInformation credentials) throws NoSuchAlgorithmException, InvalidKeyException {
            this.credentials = credentials;

            sha256HMAC = Mac.getInstance("HmacSHA256");
            sha256HMAC.init(new SecretKeySpec(credentials.secret(), "HmacSHA256"));
        }

        private String getAuthenticationHeaderValue(final HttpRequest request) {
            final String stringToSign = getStringToSign(request);

            final String signature = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            return String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s",
                    credentials.id(),
                    signedHeadersValue,
                    signature);
        }

        private String getStringToSign(final HttpRequest request) {
            String pathAndQuery = request.url().getPath();
            if (request.url().getQuery() != null) {
                pathAndQuery += '?' + request.url().getQuery();
            }

            final HttpHeaders httpHeaders = request.headers();
            final String signed = Arrays.stream(SIGNED_HEADERS)
                    .map(httpHeaders::value)
                    .collect(Collectors.joining(";"));

            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            // The line separator has to be \n. Using %n with String.format will result in a 401 from the service.
            return request.httpMethod().toString().toUpperCase(Locale.US) + "\n" + pathAndQuery + "\n" + signed;
        }
    }

    private static class CredentialInformation {
        private static final String ENDPOINT = "endpoint=";
        private static final String ID = "id=";
        private static final String SECRET = "secret=";

        private URL baseUri;
        private String id;
        private byte[] secret;

        URL baseUri() {
            return baseUri;
        }

        String id() {
            return id;
        }

        byte[] secret() {
            return secret;
        }

        CredentialInformation(String connectionString) {
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
    }
}
