package com.azure.applicationconfig;

import com.azure.applicationconfig.implementation.PageImpl;
import com.azure.applicationconfig.models.Key;
import com.azure.applicationconfig.models.KeyValue;
import com.azure.applicationconfig.models.KeyValueCreateUpdateParameters;
import com.azure.applicationconfig.models.Label;
import com.microsoft.azure.v3.CloudException;
import com.microsoft.rest.v3.RestResponse;
import com.microsoft.rest.v3.annotations.BodyParam;
import com.microsoft.rest.v3.annotations.DELETE;
import com.microsoft.rest.v3.annotations.ExpectedResponses;
import com.microsoft.rest.v3.annotations.GET;
import com.microsoft.rest.v3.annotations.HeaderParam;
import com.microsoft.rest.v3.annotations.Host;
import com.microsoft.rest.v3.annotations.HostParam;
import com.microsoft.rest.v3.annotations.PUT;
import com.microsoft.rest.v3.annotations.PathParam;
import com.microsoft.rest.v3.annotations.QueryParam;
import com.microsoft.rest.v3.annotations.UnexpectedResponseExceptionType;
import reactor.core.publisher.Mono;

/**
 * The interface defining all the services for {@link com.azure.applicationconfig.AzConfigClient} to be used
 * by the proxy service to perform REST calls.
 *
 * This is package-private so that these REST calls are transparent to the user.
 */
@Host("{url}")
interface ApplicationConfigService {
    @GET("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<KeyValue>> getKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                             @QueryParam("fields") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime,
                                             @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @PUT("kv/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<KeyValue>> setKey(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label, @BodyParam("application/json") KeyValueCreateUpdateParameters keyValueParameters,
                                        @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @GET("kv")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<KeyValue>>> listKeyValues(@HostParam("url") String url, @QueryParam("key") String key, @QueryParam("label") String label,
                                                         @QueryParam("fields") String fields, @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<KeyValue>>> listKeyValuesNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);

    @DELETE("kv/{key}")
    @ExpectedResponses({200, 204})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<KeyValue>> delete(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                        @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @PUT("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<KeyValue>> lockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                              @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @DELETE("locks/{key}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<KeyValue>> unlockKeyValue(@HostParam("url") String url, @PathParam("key") String key, @QueryParam("label") String label,
                                                @HeaderParam("If-Match") String ifMatch, @HeaderParam("If-None-Match") String ifNoneMatch);

    @GET("revisions")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<KeyValue>>> listKeyValueRevisions(@HostParam("url") String url,
                                                                 @QueryParam("key") String key, @QueryParam("label") String label, @QueryParam("fields") String fields,
                                                                 @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

    @GET("labels")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<Label>>> listLabels(@HostParam("url") String url, @QueryParam("name") String name, @QueryParam("fields") String fields,
                                                   @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<Label>>> listLabelsNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);

    @GET("keys")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<Key>>> listKeys(@HostParam("url") String url, @QueryParam("name") String name, @QueryParam("fields") String fields,
                                               @HeaderParam("Accept-Datetime") String acceptDatetime, @HeaderParam("Range") String range);

    @GET("{nextUrl}")
    @ExpectedResponses({200})
    @UnexpectedResponseExceptionType(CloudException.class)
    Mono<RestResponse<PageImpl<Key>>> listKeysNext(@HostParam("url") String url, @PathParam(value = "nextUrl", encoded = true) String nextUrl);
}
