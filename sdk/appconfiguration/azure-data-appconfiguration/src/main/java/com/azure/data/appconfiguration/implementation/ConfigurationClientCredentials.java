// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.appconfiguration.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Credentials that authorizes requests to Azure App Configuration. It uses content within the HTTP request to
 * generate the correct "Authorization" header value. {@link ConfigurationCredentialsPolicy} ensures that the content
 * exists in the HTTP request so that a valid authorization value is generated.
 *
 * @see ConfigurationCredentialsPolicy
 * @see ConfigurationClientBuilder
 */
public class ConfigurationClientCredentials {
    private final ClientLogger logger = new ClientLogger(ConfigurationClientCredentials.class);

    private static final String HOST_HEADER = "Host";
    private static final String DATE_HEADER = "Date";
    private static final String CONTENT_HASH_HEADER = "x-ms-content-sha256";
    private static final String[] SIGNED_HEADERS = new String[]{HOST_HEADER, DATE_HEADER, CONTENT_HASH_HEADER };
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final CredentialInformation credentials;
    private final AuthorizationHeaderProvider headerProvider;

    /**
     * Creates an instance that is able to authorize requests to Azure App Configuration service.
     *
     * @param connectionString Connection string in the format "endpoint={endpoint_value};id={id_value};
     *     secret={secret_value}"
     * @throws NoSuchAlgorithmException When the HMAC-SHA256 MAC algorithm cannot be instantiated.
     * @throws InvalidKeyException When the {@code connectionString} secret is invalid and cannot instantiate the
     *     HMAC-SHA256 algorithm.
     */
    public ConfigurationClientCredentials(String connectionString)
        throws InvalidKeyException, NoSuchAlgorithmException {
        credentials = new CredentialInformation(connectionString);
        headerProvider = new AuthorizationHeaderProvider(credentials);
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
     * Gets a list of headers to add to a request to authenticate it to the Azure APp Configuration service.
     * @param url the request url
     * @param httpMethod the request HTTP method
     * @param contents the body content of the request
     * @return a flux of headers to add for authorization
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm doesn't exist.
     */
    Mono<Map<String, String>> getAuthorizationHeadersAsync(URL url, String httpMethod,
                                                                  Flux<ByteBuffer> contents) {
        return contents
            .collect(() -> {
                try {
                    return MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    throw logger.logExceptionAsError(Exceptions.propagate(e));
                }
            }, (messageDigest, byteBuffer) -> {
                    if (messageDigest != null) {
                        messageDigest.update(byteBuffer);
                    }
                })
            .flatMap(messageDigest -> Mono.just(headerProvider.getAuthenticationHeaders(
                url,
                httpMethod,
                messageDigest)));
    }

    private static class AuthorizationHeaderProvider {
        private final String signedHeadersValue = String.join(";", SIGNED_HEADERS);
        private static final String HMAC_SHA256 = "HMAC-SHA256 Credential=%s&SignedHeaders=%s&Signature=%s";
        private final CredentialInformation credentials;
        private final Mac sha256HMAC;

        AuthorizationHeaderProvider(CredentialInformation credentials)
            throws NoSuchAlgorithmException, InvalidKeyException {
            this.credentials = credentials;

            sha256HMAC = Mac.getInstance("HmacSHA256");
            sha256HMAC.init(new SecretKeySpec(credentials.secret(), "HmacSHA256"));
        }

        private Map<String, String> getAuthenticationHeaders(final URL url, final String httpMethod,
                                                             final MessageDigest messageDigest) {
            final Map<String, String> headers = new HashMap<>();
            final String contentHash = Base64.getEncoder().encodeToString(messageDigest.digest());

            // All three of these headers are used by ConfigurationClientCredentials to generate the
            // Authentication header value. So, we need to ensure that they exist.
            headers.put(HOST_HEADER, url.getHost());
            headers.put(CONTENT_HASH_HEADER, contentHash);

            if (headers.get(DATE_HEADER) == null) {
                String utcNow = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
                headers.put(DATE_HEADER, utcNow);
            }

            addSignatureHeader(url, httpMethod, headers);
            return headers;
        }

        private void addSignatureHeader(final URL url, final String httpMethod, final Map<String, String> httpHeaders) {
            String pathAndQuery = url.getPath();
            if (url.getQuery() != null) {
                pathAndQuery += '?' + url.getQuery();
            }

            final String signed = Arrays.stream(SIGNED_HEADERS)
                .map(httpHeaders::get)
                .collect(Collectors.joining(";"));

            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            // The line separator has to be \n. Using %n with String.format will result in a 401 from the service.
            String stringToSign = httpMethod.toUpperCase(Locale.US) + "\n" + pathAndQuery + "\n" + signed;

            final String signature =
                Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            httpHeaders.put(AUTHORIZATION_HEADER,
                String.format(HMAC_SHA256, credentials.id(), signedHeadersValue, signature));
        }
    }

    private static class CredentialInformation {
        private static final String ENDPOINT = "endpoint=";
        private static final String ID = "id=";
        private static final String SECRET = "secret=";

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
                throw new IllegalArgumentException(connectionString);
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
                String lowerCase = segment.toLowerCase(Locale.US);

                if (lowerCase.startsWith(ENDPOINT)) {
                    try {
                        baseUri = new URL(segment.substring(ENDPOINT.length()));
                    } catch (MalformedURLException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                } else if (lowerCase.startsWith(ID)) {
                    id = segment.substring(ID.length());
                } else if (lowerCase.startsWith(SECRET)) {
                    String secretBase64 = segment.substring(SECRET.length());
                    secret = Base64.getDecoder().decode(secretBase64);
                }
            }

            this.baseUri = baseUri;
            this.id = id;
            this.secret = secret;

            if (this.baseUri == null || this.id == null || this.secret == null) {
                throw new IllegalArgumentException("Could not parse 'connectionString'."
                    + " Expected format: 'endpoint={endpoint};id={id};secret={secret}'. Actual:" + connectionString);
            }
        }
    }
}
