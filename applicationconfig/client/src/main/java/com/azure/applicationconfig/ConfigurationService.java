// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.ConfigurationSettingPage;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.common.annotations.BodyParam;
import com.azure.common.annotations.DELETE;
import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.GET;
import com.azure.common.annotations.HeaderParam;
import com.azure.common.annotations.Host;
import com.azure.common.annotations.HostParam;
import com.azure.common.annotations.PUT;
import com.azure.common.annotations.PathParam;
import com.azure.common.annotations.QueryParam;
import com.azure.common.annotations.ReturnValueWireType;
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.rest.PagedResponse;
import com.azure.common.http.rest.Response;
import com.azure.common.implementation.http.ContentType;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link ConfigurationAsyncClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface ConfigurationService {
    @GET("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    Mono<Response<ConfigurationSetting>> getKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                     @QueryParam("$select") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime,
                                                     @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @PUT("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    Mono<Response<ConfigurationSetting>> setKey(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                    @BodyParam(ContentType.APPLICATION_JSON) ConfigurationSetting keyValueParameters,
                                                    @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @DELETE("kv/{key}")
    @ExpectedResponses({200, 204})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    Mono<Response<ConfigurationSetting>> delete(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                    @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @PUT("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    Mono<Response<ConfigurationSetting>> lockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                          @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @DELETE("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    Mono<Response<ConfigurationSetting>> unlockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                            @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @GET("kv")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    @ReturnValueWireType(ConfigurationSettingPage.class)
    Mono<PagedResponse<ConfigurationSetting>> listKeyValues(@HostParam("url") String url, @QueryParam("key") String key, @QueryParam("label") String label,
                                                            @QueryParam("$select") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    @ReturnValueWireType(ConfigurationSettingPage.class)
    Mono<PagedResponse<ConfigurationSetting>> listKeyValues(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);

    @GET("revisions")
    @ExpectedResponses({200, 206})
    @UnexpectedResponseExceptionType(ServiceRequestException.class)
    @ReturnValueWireType(ConfigurationSettingPage.class)
    Mono<PagedResponse<ConfigurationSetting>> listKeyValueRevisions(@HostParam("url") String url,
                                                                    @QueryParam("key") String key, @QueryParam("label") String label, @QueryParam("$select") String fields,
                                                                    @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);
}
