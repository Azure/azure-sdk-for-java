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

import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.http.HttpPipeline;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents a URL to a blob service. This class does not hold any state about a particular storage account but is
 * instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs and containers.
 * Please refer to the following for more information on containers:
 * https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction
 */
public final class ServiceURL extends StorageURL {

    /**
     * Creates a {@code ServiceURL} object pointing to the account specified by the URL and using the provided pipeline
     * to make HTTP requests.
     * @param url
     *      A url to an Azure Storage account.
     * @param pipeline
     *      A pipeline which configures the behavior of HTTP exchanges. Please refer to the createPipeline method on
     *      {@link StorageURL} for more information.
     */
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
     * the <a href="https://docs.microsoft.com/rest/api/storageservices/list-containers2">Azure Docs</a>.
     *
     * @param marker
     *      A {@code String} value that identifies the portion of the list to be returned with the next list operation.
     *      This value is returned in the response of a previous list operation. Set to null if this is the first
     *      segment.
     * @param options
     *      A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceListContainersSegmentResponse> listContainersSegment(
            String marker, ListContainersOptions options) {
        options = options == null ? ListContainersOptions.DEFAULT : options;
        return this.storageClient.generatedServices().listContainersSegmentWithRestResponseAsync(options.getPrefix(), marker,
                options.getMaxResults(), options.getDetails().toIncludeType(), null, null);
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceGetPropertiesResponse> getProperties() {
        return this.storageClient.generatedServices().getPropertiesWithRestResponseAsync(null, null);
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     *
     * @param properties
     *      Configures the service.
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceSetPropertiesResponse> setProperties(
            StorageServiceProperties properties) {
        return this.storageClient.generatedServices().setPropertiesWithRestResponseAsync(properties, null,
                null);
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceGetStatisticsResponse> getStatistics() {
        return this.storageClient.generatedServices().getStatisticsWithRestResponseAsync(null, null);
    }

    // TODO: Preflight request
}
