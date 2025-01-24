// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.logging.ClientLogger;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import reactor.core.Exceptions;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Locale;

import static com.azure.data.appconfiguration.implementation.FakeCredentialConstants.SECRET_PLACEHOLDER;

/**
 * Credentials that authorizes requests to Azure App Configuration. It uses content within the HTTP request to generate
 * the correct "Authorization" header value. {@link ConfigurationCredentialsPolicy} ensures that the content exists in
 * the HTTP request so that a valid authorization value is generated.
 *
 * @see ConfigurationCredentialsPolicy
 * @see ConfigurationClientBuilder
 */
public class ConfigurationClientCredentials {
    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationClientCredentials.class);
    private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
    static final HttpHeaderName X_MS_CONTENT_SHA256 = HttpHeaderName.fromString("x-ms-content-sha256");

    private final CredentialInformation credentials;

    /**
     * Creates an instance that is able to authorize requests to Azure App Configuration service.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};
     * secret={secret_value}"
     */
    public ConfigurationClientCredentials(String connectionString) {
        credentials = new CredentialInformation(connectionString);
    }

    /**
     * Gets the base URI of the Azure App Configuration instance based on the provided connection string.
     *
     * @return The base url of the configuration service extracted from connection string provided.
     */
    public String getBaseUri() {
        return this.credentials.baseUri().toString();
    }

    /**
     * Sets the {@code Authorization} header on the request.
     *
     * @param httpRequest The request being authenticated.
     */
    void setAuthorizationHeaders(HttpRequest httpRequest) {
        BinaryData binaryData = httpRequest.getBodyAsBinaryData();
        final ByteBuffer byteBuffer = binaryData == null ? EMPTY_BYTE_BUFFER : binaryData.toByteBuffer();

        try {
            // Initialize everything that may throw first.
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            sha256HMAC.init(new SecretKeySpec(credentials.secret(), "HmacSHA256"));

            messageDigest.update(byteBuffer.duplicate());

            String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());

            URL url = httpRequest.getUrl();
            String pathAndQuery = url.getPath();
            if (url.getQuery() != null) {
                pathAndQuery += '?' + url.getQuery();
            }

            HttpHeaders headers = httpRequest.getHeaders();
            String date = headers.getValue(HttpHeaderName.DATE);
            if (date == null) {
                date = DateTimeRfc1123.toRfc1123String(OffsetDateTime.now(ZoneOffset.UTC));
                headers.set(HttpHeaderName.DATE, date);
            }

            String signed = url.getHost() + ";" + date + ";" + contentHash;

            headers.set(HttpHeaderName.HOST, url.getHost()).set(X_MS_CONTENT_SHA256, contentHash);

            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            // The line separator has to be \n. Using %n with String.format will result in a 401 from the service.
            String stringToSign
                = httpRequest.getHttpMethod().toString().toUpperCase(Locale.US) + "\n" + pathAndQuery + "\n" + signed;

            String signature
                = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            headers.set(HttpHeaderName.AUTHORIZATION, "HMAC-SHA256 Credential=" + credentials.id()
                + "&SignedHeaders=Host;Date;x-ms-content-sha256&Signature=" + signature);
        } catch (GeneralSecurityException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
    }

    private static class CredentialInformation {
        private static final String ENDPOINT = "endpoint=";
        private static final String ID = "id=";
        private static final String SECRET = SECRET_PLACEHOLDER;

        private final URL baseUri;
        private final String id;
        private final byte[] secret;

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
            if (CoreUtils.isNullOrEmpty(connectionString)) {
                throw new IllegalArgumentException("'connectionString' cannot be null or empty.");
            }

            String[] args = connectionString.split(";");
            if (args.length < 3) {
                throw new IllegalArgumentException("invalid connection string segment count");
            }

            URL baseUri = null;
            String id = null;
            byte[] secret = null;

            for (String arg : args) {
                String segment = arg.trim();

                if (ENDPOINT.regionMatches(true, 0, segment, 0, ENDPOINT.length())) {
                    try {
                        baseUri = new URL(segment.substring(ENDPOINT.length()));
                    } catch (MalformedURLException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                } else if (ID.regionMatches(true, 0, segment, 0, ID.length())) {
                    id = segment.substring(ID.length());
                } else if (SECRET.regionMatches(true, 0, segment, 0, SECRET.length())) {
                    String secretBase64 = segment.substring(SECRET.length());
                    secret = Base64.getDecoder().decode(secretBase64);
                }
            }

            this.baseUri = baseUri;
            this.id = id;
            this.secret = secret;

            if (this.baseUri == null
                || CoreUtils.isNullOrEmpty(this.id)
                || this.secret == null
                || this.secret.length == 0) {
                throw new IllegalArgumentException("Could not parse 'connectionString'."
                    + " Expected format: 'endpoint={endpoint};id={id};secret={secret}'. Actual:" + connectionString);
            }
        }
    }
}
