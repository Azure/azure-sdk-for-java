// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;

/**
 * Logs HTTP responses handled by {@link HttpInstrumentationPolicy}.
 */
@FunctionalInterface
public interface HttpResponseLogger {
    /**
     * Logs an HTTP response.
     *
     * @param logger The client logger associated with the response.
     * @param context Information available while logging the response.
    * @return The original response or a non-null response wrapper to pass to the caller.
     */
    Response<BinaryData> logResponse(ClientLogger logger, HttpResponseLoggingContext context);
}
