/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.models.*;
import com.microsoft.rest.v2.RestResponse;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents a URL to an Azure Storage Blob Service.
 */
public final class ServiceURL extends StorageURL {

    public ServiceURL(URL url, HttpPipeline pipeline) {
        super(url, pipeline);
    }

    public ContainerURL createContainerURL(String containerName) {
        try {
            return new ContainerURL(StorageURL.appendToURLPath(new URL(super.storageClient.url()), containerName),
                    super.storageClient.httpPipeline());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@link ServiceURL} with the given pipeline.
     *
     * @param pipeline
     *      An {@link HttpPipeline} object to set.
     * @return
     *      A {@link ServiceURL} object with the given pipeline.
     */
    public ServiceURL withPipeline(HttpPipeline pipeline) {
        try {
            return new ServiceURL(new URL(super.storageClient.url()), pipeline);
        } catch (MalformedURLException e) {
            // TODO: remove
        }
        return null;
    }

    /**
     * Returns a single segment of containers starting from the specified Marker.
     * Use an empty marker to start enumeration from the beginning. Container names are returned in lexicographic order.
     * After getting a segment, process it, and then call ListContainers again (passing the the previously-returned
     * Marker) to get the next segment. For more information, see
     * https://docs.microsoft.com/rest/api/storageservices/list-containers2.
     *
     * @param marker
     *      A {@code String} that identifies the portion of the list of containers to be returned with the next listing
     *      operation.
     * @param options
     *      A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link ServiceListContainersHeaders} and a
     *      {@link ListContainersResponse} body  if successful.
     */
    public Single<RestResponse<ServiceListContainersHeaders, ListContainersResponse>> listContainers(
            String marker, ListContainersOptions options) {
        options = options == null ? ListContainersOptions.DEFAULT : options;
        return this.storageClient.services().listContainersWithRestResponseAsync(options.getPrefix(), marker,
                options.getMaxResults(), options.getDetails().toIncludeType(), null, null);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see:
     * https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties.
     *
     * @return
     *      The {@link Single} which emits a {@link RestResponse} containing the {@link ServiceGetPropertiesHeaders} and a
     *      {@link StorageServiceProperties} body if successful.
     */
    public Single<RestResponse<ServiceGetPropertiesHeaders, StorageServiceProperties>> getProperties() {
        return this.storageClient.services().getPropertiesWithRestResponseAsync(null, null);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see:
     * https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties.
     *
     * @param properties
     *      A {@link StorageServiceProperties} object containing the configurations for the service.
     * @return
     *      A {@link Single} which emits a {@link RestResponse} containing the {@link ServiceSetPropertiesHeaders} and a
     *      {@code Void} body if successful.
     */
    public Single<RestResponse<ServiceSetPropertiesHeaders, Void>> setProperties(
            StorageServiceProperties properties) {
        return this.storageClient.services().setPropertiesWithRestResponseAsync(properties, null,
                null);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see: https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats.
     *
     * @return
     *      A {@link Single} which emits a {@link RestResponse} containing the {@link ServiceGetStatsHeaders} and a
     *      {@link StorageServiceStats} body if xssuccessful.
     */
    public Single<RestResponse<ServiceGetStatsHeaders, StorageServiceStats>> getStats() {
        return this.storageClient.services().getStatsWithRestResponseAsync(null, null);
    }
}
