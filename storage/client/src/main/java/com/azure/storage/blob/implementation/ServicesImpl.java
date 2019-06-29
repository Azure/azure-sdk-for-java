// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation;

import com.azure.core.annotations.BodyParam;
import com.azure.core.annotations.ExpectedResponses;
import com.azure.core.annotations.GET;
import com.azure.core.annotations.HeaderParam;
import com.azure.core.annotations.Host;
import com.azure.core.annotations.HostParam;
import com.azure.core.annotations.POST;
import com.azure.core.annotations.PUT;
import com.azure.core.annotations.QueryParam;
import com.azure.core.annotations.Service;
import com.azure.core.annotations.UnexpectedResponseExceptionType;
import com.azure.core.implementation.RestProxy;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.KeyInfo;
import com.azure.storage.blob.models.ListContainersIncludeType;
import com.azure.storage.blob.models.ServicesGetAccountInfoResponse;
import com.azure.storage.blob.models.ServicesGetPropertiesResponse;
import com.azure.storage.blob.models.ServicesGetStatisticsResponse;
import com.azure.storage.blob.models.ServicesGetUserDelegationKeyResponse;
import com.azure.storage.blob.models.ServicesListContainersSegmentResponse;
import com.azure.storage.blob.models.ServicesSetPropertiesResponse;
import com.azure.storage.blob.models.StorageErrorException;
import com.azure.storage.blob.models.StorageServiceProperties;
import reactor.core.publisher.Mono;

/**
 * An instance of this class provides access to all the operations defined in
 * Services.
 */
public final class ServicesImpl {
    /**
     * The proxy service used to perform REST calls.
     */
    private ServicesService service;

    /**
     * The service client containing this operation class.
     */
    private AzureBlobStorageImpl client;

    /**
     * Initializes an instance of ServicesImpl.
     *
     * @param client the instance of the service client containing this operation class.
     */
    public ServicesImpl(AzureBlobStorageImpl client) {
        this.service = RestProxy.create(ServicesService.class, client);
        this.client = client;
    }

    /**
     * The interface defining all the services for Services to be used by the
     * proxy service to perform REST calls.
     */
    @Host("{url}")
    @Service("Storage Blobs Services")
    private interface ServicesService {
        @PUT("")
        @ExpectedResponses({202})
        @UnexpectedResponseExceptionType(StorageErrorException.class)
        Mono<ServicesSetPropertiesResponse> setProperties(@HostParam("url") String url, @BodyParam("application/xml; charset=utf-8") StorageServiceProperties storageServiceProperties, @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-client-request-id") String requestId, @QueryParam("restype") String restype, @QueryParam("comp") String comp, Context context);

        @GET("")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(StorageErrorException.class)
        Mono<ServicesGetPropertiesResponse> getProperties(@HostParam("url") String url, @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-client-request-id") String requestId, @QueryParam("restype") String restype, @QueryParam("comp") String comp, Context context);

        @GET("")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(StorageErrorException.class)
        Mono<ServicesGetStatisticsResponse> getStatistics(@HostParam("url") String url, @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-client-request-id") String requestId, @QueryParam("restype") String restype, @QueryParam("comp") String comp, Context context);

        @GET("")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(StorageErrorException.class)
        Mono<ServicesListContainersSegmentResponse> listContainersSegment(@HostParam("url") String url, @QueryParam("prefix") String prefix, @QueryParam("marker") String marker, @QueryParam("maxresults") Integer maxresults, @QueryParam("include") ListContainersIncludeType include, @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-client-request-id") String requestId, @QueryParam("comp") String comp, Context context);

        @POST("")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(StorageErrorException.class)
        Mono<ServicesGetUserDelegationKeyResponse> getUserDelegationKey(@HostParam("url") String url, @BodyParam("application/xml; charset=utf-8") KeyInfo keyInfo, @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-client-request-id") String requestId, @QueryParam("restype") String restype, @QueryParam("comp") String comp, Context context);

        @GET("")
        @ExpectedResponses({200})
        @UnexpectedResponseExceptionType(StorageErrorException.class)
        Mono<ServicesGetAccountInfoResponse> getAccountInfo(@HostParam("url") String url, @HeaderParam("x-ms-version") String version, @QueryParam("restype") String restype, @QueryParam("comp") String comp, Context context);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.
     *
     * @param storageServiceProperties The StorageService properties.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesSetPropertiesResponse> setPropertiesWithRestResponseAsync(StorageServiceProperties storageServiceProperties, Context context) {
        final Integer timeout = null;
        final String requestId = null;
        final String restype = "service";
        final String comp = "properties";
        return service.setProperties(this.client.url(), storageServiceProperties, timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.
     *
     * @param storageServiceProperties The StorageService properties.
     * @param timeout The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesSetPropertiesResponse> setPropertiesWithRestResponseAsync(StorageServiceProperties storageServiceProperties, Integer timeout, String requestId, Context context) {
        final String restype = "service";
        final String comp = "properties";
        return service.setProperties(this.client.url(), storageServiceProperties, timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * gets the properties of a storage account's Blob service, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetPropertiesResponse> getPropertiesWithRestResponseAsync(Context context) {
        final Integer timeout = null;
        final String requestId = null;
        final String restype = "service";
        final String comp = "properties";
        return service.getProperties(this.client.url(), timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * gets the properties of a storage account's Blob service, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.
     *
     * @param timeout The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetPropertiesResponse> getPropertiesWithRestResponseAsync(Integer timeout, String requestId, Context context) {
        final String restype = "service";
        final String comp = "properties";
        return service.getProperties(this.client.url(), timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location endpoint when read-access geo-redundant replication is enabled for the storage account.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetStatisticsResponse> getStatisticsWithRestResponseAsync(Context context) {
        final Integer timeout = null;
        final String requestId = null;
        final String restype = "service";
        final String comp = "stats";
        return service.getStatistics(this.client.url(), timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary location endpoint when read-access geo-redundant replication is enabled for the storage account.
     *
     * @param timeout The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetStatisticsResponse> getStatisticsWithRestResponseAsync(Integer timeout, String requestId, Context context) {
        final String restype = "service";
        final String comp = "stats";
        return service.getStatistics(this.client.url(), timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * The List Containers Segment operation returns a list of the containers under the specified account.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesListContainersSegmentResponse> listContainersSegmentWithRestResponseAsync(Context context) {
        final String prefix = null;
        final String marker = null;
        final Integer maxresults = null;
        final ListContainersIncludeType include = null;
        final Integer timeout = null;
        final String requestId = null;
        final String comp = "list";
        return service.listContainersSegment(this.client.url(), prefix, marker, maxresults, include, timeout, this.client.version(), requestId, comp, context);
    }

    /**
     * The List Containers Segment operation returns a list of the containers under the specified account.
     *
     * @param prefix Filters the results to return only containers whose name begins with the specified prefix.
     * @param marker A string value that identifies the portion of the list of containers to be returned with the next listing operation. The operation returns the NextMarker value within the response body if the listing operation did not return all containers remaining to be listed with the current page. The NextMarker value can be used as the value for the marker parameter in a subsequent call to request the next page of list items. The marker value is opaque to the client.
     * @param maxresults Specifies the maximum number of containers to return. If the request does not specify maxresults, or specifies a value greater than 5000, the server will return up to 5000 items. Note that if the listing operation crosses a partition boundary, then the service will return a continuation token for retrieving the remainder of the results. For this reason, it is possible that the service will return fewer results than specified by maxresults, or than the default of 5000.
     * @param include Include this parameter to specify that the container's metadata be returned as part of the response body. Possible values include: 'metadata'.
     * @param timeout The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesListContainersSegmentResponse> listContainersSegmentWithRestResponseAsync(String prefix, String marker, Integer maxresults, ListContainersIncludeType include, Integer timeout, String requestId, Context context) {
        final String comp = "list";
        return service.listContainersSegment(this.client.url(), prefix, marker, maxresults, include, timeout, this.client.version(), requestId, comp, context);
    }

    /**
     * Retrieves a user delgation key for the Blob service. This is only a valid operation when using bearer token authentication.
     *
     * @param keyInfo the KeyInfo value.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetUserDelegationKeyResponse> getUserDelegationKeyWithRestResponseAsync(KeyInfo keyInfo, Context context) {
        final Integer timeout = null;
        final String requestId = null;
        final String restype = "service";
        final String comp = "userdelegationkey";
        return service.getUserDelegationKey(this.client.url(), keyInfo, timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * Retrieves a user delgation key for the Blob service. This is only a valid operation when using bearer token authentication.
     *
     * @param keyInfo the KeyInfo value.
     * @param timeout The timeout parameter is expressed in seconds. For more information, see &lt;a href="https://docs.microsoft.com/en-us/rest/api/storageservices/fileservices/setting-timeouts-for-blob-service-operations"&gt;Setting Timeouts for Blob Service Operations.&lt;/a&gt;.
     * @param requestId Provides a client-generated, opaque value with a 1 KB character limit that is recorded in the analytics logs when storage analytics logging is enabled.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetUserDelegationKeyResponse> getUserDelegationKeyWithRestResponseAsync(KeyInfo keyInfo, Integer timeout, String requestId, Context context) {
        final String restype = "service";
        final String comp = "userdelegationkey";
        return service.getUserDelegationKey(this.client.url(), keyInfo, timeout, this.client.version(), requestId, restype, comp, context);
    }

    /**
     * Returns the sku name and account kind.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @return a Mono which performs the network request upon subscription.
     */
    public Mono<ServicesGetAccountInfoResponse> getAccountInfoWithRestResponseAsync(Context context) {
        final String restype = "account";
        final String comp = "properties";
        return service.getAccountInfo(this.client.url(), this.client.version(), restype, comp, context);
    }
}
