// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

/**
 * Service interface for testing duration encoding in HTTP requests.
 * <p>
 * Covers the following encoding scenarios from the http-specs encode/duration spec:
 * <ul>
 *     <li>ISO-8601 (default and explicit)</li>
 *     <li>int32 seconds</li>
 *     <li>int32 seconds with a larger-unit duration (minutes)</li>
 *     <li>float seconds</li>
 *     <li>float seconds with a larger-unit duration (minutes)</li>
 *     <li>int32 milliseconds</li>
 *     <li>int32 milliseconds with a larger-unit duration (minutes)</li>
 * </ul>
 */
@ServiceInterface(name = "EncodeDurationService", host = "{endpoint}")
public interface EncodeDurationService {
    /**
     * Creates a new instance of {@link EncodeDurationService}.
     *
     * @param pipeline The HTTP pipeline to use.
     * @return A new instance of EncodeDurationService.
     */
    static EncodeDurationService getNewInstance(HttpPipeline pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null");
        }
        try {
            Class<?> clazz = Class.forName("io.clientcore.annotation.processor.test.EncodeDurationServiceImpl");
            return (EncodeDurationService) clazz.getMethod("getNewInstance", HttpPipeline.class).invoke(null, pipeline);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
            | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // =========== Query parameter methods ===========

    /**
     * Tests the default (ISO-8601) encoding for a duration query parameter.
     * Expected query parameter: {@code input=P40D}
     *
     * @param endpoint The service endpoint.
     * @param input The duration to encode.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/default",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryDefault(@HostParam("endpoint") String endpoint, @QueryParam("input") Duration input,
        RequestContext requestContext);

    /**
     * Tests int32 seconds encoding for a duration query parameter.
     * Expected query parameter: {@code input=36}
     *
     * @param endpoint The service endpoint.
     * @param input The duration in seconds (as long).
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/int32-seconds",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryInt32Seconds(@HostParam("endpoint") String endpoint, @QueryParam("input") long input,
        RequestContext requestContext);

    /**
     * Tests int32 seconds encoding for a duration query parameter where the duration is several minutes.
     * Expected query parameter: {@code input=120}
     *
     * @param endpoint The service endpoint.
     * @param input The duration in seconds (as long), e.g. {@code Duration.ofMinutes(2).getSeconds()}.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/int32-seconds-larger-unit",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryInt32SecondsLargerUnit(@HostParam("endpoint") String endpoint, @QueryParam("input") long input,
        RequestContext requestContext);

    /**
     * Tests float seconds encoding for a duration query parameter.
     * Expected query parameter: {@code input=35.625}
     *
     * @param endpoint The service endpoint.
     * @param input The duration in seconds (as double).
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/float-seconds",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryFloatSeconds(@HostParam("endpoint") String endpoint, @QueryParam("input") double input,
        RequestContext requestContext);

    /**
     * Tests float seconds encoding for a duration query parameter where the duration is several minutes.
     * Expected query parameter: {@code input=150.0}
     *
     * @param endpoint The service endpoint.
     * @param input The duration in seconds (as double), e.g. {@code Duration.ofMinutes(2).plusSeconds(30).getSeconds()}.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/float-seconds-larger-unit",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryFloatSecondsLargerUnit(@HostParam("endpoint") String endpoint,
        @QueryParam("input") double input, RequestContext requestContext);

    /**
     * Tests int32 milliseconds encoding for a duration query parameter.
     * Expected query parameter: {@code input=36000}
     *
     * @param endpoint The service endpoint.
     * @param input The duration in milliseconds (as long).
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/int32-milliseconds",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryInt32Milliseconds(@HostParam("endpoint") String endpoint, @QueryParam("input") long input,
        RequestContext requestContext);

    /**
     * Tests int32 milliseconds encoding for a duration query parameter where the duration is several minutes.
     * Expected query parameter: {@code input=180000}
     *
     * @param endpoint The service endpoint.
     * @param input The duration in milliseconds (as long), e.g. {@code Duration.ofMinutes(3).toMillis()}.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/query/int32-milliseconds-larger-unit",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> queryInt32MillisecondsLargerUnit(@HostParam("endpoint") String endpoint,
        @QueryParam("input") long input, RequestContext requestContext);

    // =========== Header parameter methods ===========

    /**
     * Tests the default (ISO-8601) encoding for a duration header.
     * Expected header: {@code duration: P40D}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration to encode.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/default",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerDefault(@HostParam("endpoint") String endpoint, @HeaderParam("duration") Duration duration,
        RequestContext requestContext);

    /**
     * Tests int32 seconds encoding for a duration header.
     * Expected header: {@code duration: 36}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration in seconds (as long).
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/int32-seconds",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerInt32Seconds(@HostParam("endpoint") String endpoint, @HeaderParam("duration") long duration,
        RequestContext requestContext);

    /**
     * Tests int32 seconds encoding for a duration header where the duration is several minutes.
     * Expected header: {@code duration: 120}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration in seconds (as long), e.g. {@code Duration.ofMinutes(2).getSeconds()}.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/int32-seconds-larger-unit",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerInt32SecondsLargerUnit(@HostParam("endpoint") String endpoint,
        @HeaderParam("duration") long duration, RequestContext requestContext);

    /**
     * Tests float seconds encoding for a duration header.
     * Expected header: {@code duration: 35.625}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration in seconds (as double).
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/float-seconds",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerFloatSeconds(@HostParam("endpoint") String endpoint, @HeaderParam("duration") double duration,
        RequestContext requestContext);

    /**
     * Tests float seconds encoding for a duration header where the duration is several minutes.
     * Expected header: {@code duration: 150.0}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration in seconds (as double), e.g. {@code (double) Duration.ofMinutes(2).plusSeconds(30).getSeconds()}.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/float-seconds-larger-unit",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerFloatSecondsLargerUnit(@HostParam("endpoint") String endpoint,
        @HeaderParam("duration") double duration, RequestContext requestContext);

    /**
     * Tests int32 milliseconds encoding for a duration header.
     * Expected header: {@code duration: 36000}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration in milliseconds (as long).
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/int32-milliseconds",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerInt32Milliseconds(@HostParam("endpoint") String endpoint,
        @HeaderParam("duration") long duration, RequestContext requestContext);

    /**
     * Tests int32 milliseconds encoding for a duration header where the duration is several minutes.
     * Expected header: {@code duration: 180000}
     *
     * @param endpoint The service endpoint.
     * @param duration The duration in milliseconds (as long), e.g. {@code Duration.ofMinutes(3).toMillis()}.
     * @param requestContext The request context.
     * @return The response.
     */
    @HttpRequestInformation(
        method = HttpMethod.GET,
        path = "/encode/duration/header/int32-milliseconds-larger-unit",
        expectedStatusCodes = { 204 })
    @UnexpectedResponseExceptionDetail
    Response<Void> headerInt32MillisecondsLargerUnit(@HostParam("endpoint") String endpoint,
        @HeaderParam("duration") long duration, RequestContext requestContext);
}
