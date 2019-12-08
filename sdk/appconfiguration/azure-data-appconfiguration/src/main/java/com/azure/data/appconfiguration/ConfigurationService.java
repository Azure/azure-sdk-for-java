// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.implementation.ConfigurationSettingPage;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.core.annotation.BodyParam;
import com.azure.core.annotation.Delete;
import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ReturnValueWireType;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.annotation.UnexpectedResponseExceptionType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.ContentType;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link ConfigurationAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
@ServiceInterface(name = "AppConfig")
interface ConfigurationService {
    @Get("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<ConfigurationSetting>> getKeyValue(
        @HostParam("url") String url,
        @PathParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @QueryParam("$select") String fields,
        @HeaderParam("Accept-Datetime") String acceptDatetime,
        @HeaderParam("If-Match") String ifMatch,
        @HeaderParam("If-None-Match") String ifNoneMatch,
        Context context);

    @Put("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<ConfigurationSetting>> setKey(
        @HostParam("url") String url,
        @PathParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @BodyParam(ContentType.APPLICATION_JSON) ConfigurationSetting keyValueParameters,
        @HeaderParam("If-Match") String ifMatch,
        @HeaderParam("If-None-Match") String ifNoneMatch,
        Context context);

    @Delete("kv/{key}")
    @ExpectedResponses({200, 204})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<ConfigurationSetting>> delete(
        @HostParam("url") String url,
        @PathParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @HeaderParam("If-Match") String ifMatch,
        @HeaderParam("If-None-Match") String ifNoneMatch,
        Context context);

    @Put("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<ConfigurationSetting>> lockKeyValue(
        @HostParam("url") String url,
        @PathParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @HeaderParam("If-Match") String ifMatch,
        @HeaderParam("If-None-Match") String ifNoneMatch,
        Context context);

    @Delete("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    Mono<Response<ConfigurationSetting>> unlockKeyValue(
        @HostParam("url") String url,
        @PathParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @HeaderParam("If-Match") String ifMatch,
        @HeaderParam("If-None-Match") String ifNoneMatch,
        Context context);

    @Get("kv")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ConfigurationSettingPage.class)
    Mono<PagedResponse<ConfigurationSetting>> listKeyValues(
        @HostParam("url") String url,
        @QueryParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @QueryParam("$select") String fields,
        @HeaderParam("Accept-Datetime") String acceptDatetime,
        Context context);

    @Get("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ConfigurationSettingPage.class)
    Mono<PagedResponse<ConfigurationSetting>> listKeyValues(
        @HostParam("url") String url,
        @PathParam(value = "nextUrl", encoded = true) String nextUrl,
        Context context);

    @Get("revisions")
    @ExpectedResponses({200, 206})
    @UnexpectedResponseExceptionType(HttpResponseException.class)
    @ReturnValueWireType(ConfigurationSettingPage.class)
    Mono<PagedResponse<ConfigurationSetting>> listKeyValueRevisions(
        @HostParam("url") String url,
        @QueryParam("key") String key,
        @QueryParam("label") String label,
        @QueryParam("api-version") String apiVersion,
        @QueryParam("$select") String fields,
        @HeaderParam("Accept-Datetime") String acceptDatetime,
        @HeaderParam("Range") String range,
        Context context);
}
