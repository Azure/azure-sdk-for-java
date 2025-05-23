// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.client;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Set;

/**
 * HttpClient implementation using {@link HttpURLConnection} to send requests and receive responses.
 */
public final class JdkHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpClient.class);

    private static final String ERROR_MESSAGE = "It is recommended that libraries be deployed on the latest LTS "
        + "version of Java, however the Java client will support down to Java 8. In the case where the client is to "
        + "operate on Java versions below Java 11, it is required to include additional dependencies. Usage of "
        + "DefaultHttpClient is only available when using Java 12 or higher. For support with Java 11 or lower, "
        + "include a dependency on io.clientcore:http-okhttp3.";

    /**
     * Creates an instance of DefaultHttpClient.
     *
     * @param httpClient The wrapped http client.
     * @param restrictedHeaders The set of headers that are restricted from being set by the user.
     * @param writeTimeout The write timeout.
     * @param responseTimeout The response timeout.
     * @param readTimeout The read timeout.
     * @throws UnsupportedOperationException if the client is not running on Java 12 or higher.
     */
    public JdkHttpClient(Object httpClient, Set<String> restrictedHeaders, Duration writeTimeout,
        Duration responseTimeout, Duration readTimeout) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }

    @Override
    public Response<BinaryData> send(HttpRequest request) {
        throw LOGGER.throwableAtError().log(ERROR_MESSAGE, UnsupportedOperationException::new);
    }
}
