package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.Page;
import com.azure.applicationconfig.models.ConfigurationSetting;
import com.azure.applicationconfig.models.KeyValueCreateUpdateParameters;
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
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.http.rest.RestException;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.implementation.http.ContentType;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link ConfigurationClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface ApplicationConfigService {
    @GET("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<ConfigurationSetting>> getKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                         @QueryParam("$select") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime,
                                                         @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @PUT("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<ConfigurationSetting>> setKey(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label, @BodyParam(ContentType.APPLICATION_JSON) KeyValueCreateUpdateParameters keyValueParameters,
                                                    @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @GET("kv")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<ConfigurationSetting>>> listKeyValues(@HostParam("url") String url, @QueryParam("key") String key, @QueryParam("label") String label,
                                                                 @QueryParam("$select") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<ConfigurationSetting>>> listKeyValuesNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);

    @DELETE("kv/{key}")
    @ExpectedResponses({200, 204})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<ConfigurationSetting>> delete(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                    @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @PUT("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<ConfigurationSetting>> lockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                          @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @DELETE("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<ConfigurationSetting>> unlockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                            @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @GET("revisions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(RestException.class)
    Mono<RestResponse<Page<ConfigurationSetting>>> listKeyValueRevisions(@HostParam("url") String url,
                                                                         @QueryParam("key") String key, @QueryParam("label") String label, @QueryParam("$select") String fields,
                                                                         @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);
}
