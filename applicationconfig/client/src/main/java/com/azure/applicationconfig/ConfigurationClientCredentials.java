package com.azure.applicationconfig;

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

import static com.azure.applicationconfig.ConfigurationCredentialsPolicy.CONTENT_HASH_HEADER;
import static com.azure.applicationconfig.ConfigurationCredentialsPolicy.DATE_HEADER;
import static com.azure.applicationconfig.ConfigurationCredentialsPolicy.HOST_HEADER;

public class ConfigurationClientCredentials implements AsyncServiceClientCredentials {
    private final CredentialInformation credentials;
    private final AuthorizationHeaderProvider headerProvider;

    /**
     * Creates an instance that is able to authorize requests to Azure Application Configuration service.
     *
     * @param connectionString connection string in the format "endpoint=_endpoint_;id=_id_;secret=_secret_"
     */
    public ConfigurationClientCredentials(String connectionString) {
        credentials = new CredentialInformation(connectionString);
        headerProvider = new AuthorizationHeaderProvider(credentials);
    }

    URL baseUri() { return this.credentials.baseUri; }

    @Override
    public Mono<String> authorizationHeaderValueAsync(HttpRequest request) {
        try {
            String authorizationValue = headerProvider.getAuthenticationHeaderValue(request);
            return Mono.just(authorizationValue);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    private static class AuthorizationHeaderProvider {
        private final String[] signedHeaders = new String[]{HOST_HEADER, DATE_HEADER, CONTENT_HASH_HEADER};
        private final String signedHeadersValue = String.join(";", signedHeaders);
        private final CredentialInformation credentials;

        AuthorizationHeaderProvider(CredentialInformation credentials) {
            this.credentials = credentials;
        }

        private String getAuthenticationHeaderValue(final HttpRequest request) throws NoSuchAlgorithmException, InvalidKeyException {
            final String stringToSign = getStringToSign(request);
            final Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secretKey = new SecretKeySpec(credentials.secret(), "HmacSHA256");

            sha256HMAC.init(secretKey);

            final String signature = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8)));
            return String.format("HMAC-SHA256 Credential=%s, SignedHeaders=%s, Signature=%s",
                credentials.credential(),
                signedHeadersValue,
                signature);
        }

        private String getStringToSign(final HttpRequest request) {
            String pathAndQuery = request.url().getPath();
            if (request.url().getQuery() != null) {
                pathAndQuery += '?' + request.url().getQuery();
            }

            final HttpHeaders httpHeaders = request.headers();
            final String signed = Arrays.stream(signedHeaders)
                .map(httpHeaders::value)
                .collect(Collectors.joining(";"));

            // String-To-Sign=HTTP_METHOD + '\n' + path_and_query + '\n' + signed_headers_values
            // Signed headers: "host;x-ms-date;x-ms-content-sha256"
            return String.format("%s\n%s\n%s", request.httpMethod().toString().toUpperCase(Locale.US), pathAndQuery, signed);
        }
    }

    private class CredentialInformation {
        private URL baseUri;
        private String credential;
        private byte[] secret;

        URL baseUri() { return baseUri; }

        String credential() { return credential; }

        byte[] secret() { return secret; }

        CredentialInformation(String connectionString) {
            if (connectionString == null || connectionString.isEmpty()) {
                throw new IllegalArgumentException(connectionString);
            }

            // Parse connection string
            String[] args = connectionString.split(";");
            if (args.length < 3) {
                throw new IllegalArgumentException("invalid connection string segment count");
            }

            String endpointString = "endpoint=";
            String idString = "id=";
            String secretString = "secret=";

            for (String arg : args) {
                String segment = arg.trim();
                try {
                    if (segment.toLowerCase().startsWith(endpointString)) {
                        this.baseUri = new URL(segment.substring(segment.indexOf('=') + 1));
                    } else if (segment.toLowerCase().startsWith(idString)) {
                        this.credential = segment.substring(segment.indexOf('=') + 1);
                    } else if (segment.toLowerCase().startsWith(secretString)) {
                        String secretBase64 = segment.substring(segment.indexOf('=') + 1);
                        this.secret = Base64.getDecoder().decode(secretBase64);
                    }
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }

            if (this.baseUri == null || this.credential == null || this.secret == null) {
                throw new IllegalArgumentException("Could not parse 'connectionString' value: " + connectionString);
            }
        }
    }
}
