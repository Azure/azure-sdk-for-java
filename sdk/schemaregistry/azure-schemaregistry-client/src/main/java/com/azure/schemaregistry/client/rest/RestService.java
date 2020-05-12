/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client.rest;

import com.azure.schemaregistry.client.rest.entities.ErrorMessage;
import com.azure.schemaregistry.client.rest.entities.responses.RegisterSchemaResponse;
import com.azure.schemaregistry.client.rest.entities.responses.SchemaObjectResponse;
import com.azure.schemaregistry.client.rest.exceptions.RestClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class RestService {
    private static final Logger log = LoggerFactory.getLogger(RestService.class);
    private static final TypeReference<RegisterSchemaResponse> REGISTER_RESPONSE_TYPE =
            new TypeReference<RegisterSchemaResponse>() {
            };
    private static final TypeReference<SchemaObjectResponse> GET_SCHEMA_BY_GUID_RESPONSE_TYPE =
            new TypeReference<SchemaObjectResponse>() {
            };
    private static final TypeReference<SchemaObjectResponse> GET_LATEST_SCHEMA_RESPONSE_TYPE =
            new TypeReference<SchemaObjectResponse>() {
            };
    private static final TypeReference<SchemaObjectResponse> GET_SCHEMA_BY_VERSION_RESPONSE_TYPE =
            new TypeReference<SchemaObjectResponse>() {
            };

    private static final int HTTP_CONNECT_TIMEOUT_MS = 60000;
    private static final int HTTP_READ_TIMEOUT_MS = 60000;

    private static final int JSON_PARSE_ERROR_CODE = 50005;
    private static ObjectMapper jsonDeserializer = new ObjectMapper();

    public static Charset SERVICE_CHARSET = StandardCharsets.UTF_8;

    private static final String AUTHORIZATION_HEADER = "Authorization";

    public static Map<String,String> getDefaultRequestProperties(Map<String, String> contentTypeEntryMap) {
        String contentTypeValue = "application/json";
        if (contentTypeEntryMap != null) {
            for (Map.Entry<String, String> p : contentTypeEntryMap.entrySet()) {
                contentTypeValue += ";" + p.getKey() + "=" + p.getValue();
            }
        }
        return Collections.singletonMap("Content-Type", contentTypeValue);
    }

    private String registryUrl;
    private String credentials;
    private SSLSocketFactory sslSocketFactory;
    private Map<String, String> httpHeaders;
    private Proxy proxy;

    public RestService(String registryUrl, String credentials) {
        if (!validateRegistryNamespace(registryUrl)) {
            throw new IllegalArgumentException(String.format("Improper registry namespace: %s", registryUrl));
        }
        this.registryUrl = registryUrl;
        this.credentials = credentials;
    }

    private static boolean isNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    private static boolean isValidProxyConfig(String proxyHost, Integer proxyPort) {
        return isNonEmpty(proxyHost) && proxyPort != null && proxyPort > 0;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    /**
     * @param requestUrl        HTTP connection will be established with this url.
     * @param method            HTTP method ("GET", "POST", "PUT", etc.)
     * @param requestBodyData   Bytes to be sent in the request body.
     * @param requestProperties HTTP header properties.
     * @param responseFormat    Expected format of the response to the HTTP request.
     * @param <T>               The type of the deserialized response to the HTTP request.
     * @return The deserialized response to the HTTP request, or null if no data is expected.
     */
    @SuppressWarnings("unchecked")
    private <T> T sendHttpRequest(String requestUrl, String method, byte[] requestBodyData,
                                  Map<String, String> requestProperties,
                                  TypeReference<T> responseFormat)
            throws IOException, RestClientException {
        String requestData = requestBodyData == null
                ? "null"
                : new String(requestBodyData, StandardCharsets.UTF_8);

        HttpURLConnection connection = null;
        try {
            URL url = new URL(requestUrl);

            connection = buildConnection(url, method, requestProperties);

            log.debug(String.format("Sending %s with input %s to %s. Connection hc: %d.",
                method, requestData, requestUrl, connection.hashCode()));

            if (requestBodyData != null) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestBodyData);
                    os.flush();
                } catch (IOException e) {
                    log.error("Failed to send HTTP request to endpoint: " + url, e);
                    throw e;
                }
            }

            int responseCode = connection.getResponseCode();
            log.debug(String.format("Response code received: HTTP%d. Connection hc: %d.",
                    responseCode, connection.hashCode()));
            if (responseCode == HttpURLConnection.HTTP_OK) {
                T result;
                if (responseFormat.getType().equals(SchemaObjectResponse.class)) {
                    result = (T)new SchemaObjectResponse(connection);
                }
                else {
                    InputStream is = connection.getInputStream();
                    result = jsonDeserializer.readValue(is, responseFormat);
                    is.close();
                }
                return result;
            } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                return null;
            } else {
                ErrorMessage errorMessage;
                try (InputStream es = connection.getErrorStream()) {
                    java.io.ByteArrayOutputStream streamContent = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = es.read(buffer)) != -1) {
                        streamContent.write(buffer, 0, length);
                    }
                    log.debug(new String(streamContent.toByteArray()));
                    errorMessage = jsonDeserializer.readValue(es, ErrorMessage.class);
                } catch (JsonProcessingException e) {
                    errorMessage = new ErrorMessage(JSON_PARSE_ERROR_CODE, e.getMessage());
                }
                throw new RestClientException(errorMessage.getMessage(), responseCode,
                        errorMessage.getErrorCode());
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection buildConnection(URL url, String method, Map<String,
            String> requestProperties)
            throws IOException {
        HttpURLConnection connection = null;
        if (proxy == null) {
            connection = (HttpURLConnection) url.openConnection();
        } else {
            connection = (HttpURLConnection) url.openConnection(proxy);
        }

        connection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(HTTP_READ_TIMEOUT_MS);

        setupSsl(connection);
        connection.setRequestMethod(method);
        setAuthRequestHeaders(connection);
        setCustomHeaders(connection);
        // connection.getResponseCode() implicitly calls getInputStream, so always set to true.
        // On the other hand, leaving this out breaks nothing.
        connection.setDoInput(true);

        for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        connection.setUseCaches(false);

        return connection;
    }

    private void setupSsl(HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection && sslSocketFactory != null) {
            ((HttpsURLConnection)connection).setSSLSocketFactory(sslSocketFactory);
        }
    }

    private <T> T httpRequest(String schemaGroup,
                              String schemaName,
                              String schemaVersion,
                              String method,
                              byte[] requestBodyData,
                              Map<String, String> requestProperties,
                              Map<String, String> queryMap,
                              TypeReference<T> responseFormat)
            throws IOException, RestClientException {
        int retryCount = 3;
        while (true) {
            String requestUrl = buildRequestUrl(this.registryUrl, schemaGroup, schemaName, schemaVersion, queryMap);
            try {
                return sendHttpRequest(requestUrl,
                        method,
                        requestBodyData,
                        requestProperties,
                        responseFormat);
            } catch (IOException e) {
                retryCount--;
                if (retryCount == 0) {
                    throw e;
                }
            }
        }
    }


    /**
     * @param registryUrl registry namespace
     * @param schemaGroup group, may be null
     * @param schemaName schema name, may be null
     * @param schemaVersion schema version, may be null
     * @param queryMap
     * @return
     */
    static String buildRequestUrl(String registryUrl, String schemaGroup, String schemaName, String schemaVersion, Map<String,String> queryMap) {
        StringBuilder builder = new StringBuilder();

        builder.append("https://")
                .append(registryUrl)
                .append("/$schemagroups");

        if (schemaGroup != null) {
            builder.append("/")
                    .append(schemaGroup);
        }

        if (schemaName != null) {
            builder.append("/schemas/")
                    .append(schemaName);
        }

        if (schemaVersion != null) {
            builder.append("/versions/")
                    .append(schemaVersion);
        }

        builder.append("?api-version=2017-04");

        if (queryMap != null) {
            for (String key : queryMap.keySet()) {
                builder.append("&").append(key.trim()).append("=").append(queryMap.get(key).trim());
            }
        }

        return builder.toString();
    }

    public String registerSchema(String schemaGroup, String schemaName, String schemaString, String serializationType) throws IOException, RestClientException {
        RegisterSchemaResponse response = httpRequest(
                schemaGroup,
                schemaName,
                null
                , "PUT",
                schemaString.getBytes(SERVICE_CHARSET),
                getDefaultRequestProperties(Collections.singletonMap("serialization", serializationType)),
                null,
                REGISTER_RESPONSE_TYPE);

        return response.getId();
    }

    public String getGuid(String schemaGroup, String schemaName, String schemaString, String serializationType) throws IOException, RestClientException {
        RegisterSchemaResponse response = httpRequest(
                schemaGroup, schemaName, null,
                "PUT",
                schemaString.getBytes(SERVICE_CHARSET),
                getDefaultRequestProperties(Collections.singletonMap("serialization", serializationType)),
                null,
                REGISTER_RESPONSE_TYPE);

        return response.getId();
    }

    public SchemaObjectResponse getSchemaByGuid(String schemaGuid) throws IOException, RestClientException {
        Map<String, String> queryMap = Collections.singletonMap("schema-id", schemaGuid);
        return httpRequest(
            null,null,null,
            "GET",
            null,
            getDefaultRequestProperties(null),
            queryMap,
            GET_SCHEMA_BY_GUID_RESPONSE_TYPE);
    }

    public SchemaObjectResponse getSchema(String schemaGroup, String schemaName, int version) throws IOException, RestClientException {
        SchemaObjectResponse response;

        // latest version
        if (version == -1) {
            String path = String.format("/%s", schemaName);
            response = httpRequest(
                schemaGroup,
                schemaName,
                null,
                "GET",
                null,
                getDefaultRequestProperties(null),
                null,
                GET_LATEST_SCHEMA_RESPONSE_TYPE);
        }
        else {
            String path = String.format("/%s/$versions/%d", schemaName, version);
            response = httpRequest(
                schemaGroup,
                schemaName,
                String.valueOf(version),
                "GET",
                null,
                getDefaultRequestProperties(null),
                null,
                GET_SCHEMA_BY_VERSION_RESPONSE_TYPE);
        }

        return response;
    }

    private void setAuthRequestHeaders(HttpURLConnection connection) {
//        if (bearerAuthCredentialProvider != null) {
//            String bearerToken = bearerAuthCredentialProvider.getBearerToken(connection.getURL());
//            if (bearerToken != null) {
//                connection.setRequestProperty(AUTHORIZATION_HEADER, "Bearer " + bearerToken);
//            }
//        }
    }

    private void setCustomHeaders(HttpURLConnection connection) {
        if (httpHeaders != null) {
            httpHeaders.forEach((k, v) -> connection.setRequestProperty(k, v));
        }
    }
//
//    public void setBearerAuthCredentialProvider(
//            BearerAuthCredentialProvider bearerAuthCredentialProvider) {
//        this.bearerAuthCredentialProvider = bearerAuthCredentialProvider;
//    }

    public void setHttpHeaders(Map<String, String> httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public void setProxy(String proxyHost, int proxyPort) {
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    }

    private boolean validateRegistryNamespace(String registryNamespace) {
        return registryNamespace.contains(".net") && !registryNamespace.contains("//");
    }
}
