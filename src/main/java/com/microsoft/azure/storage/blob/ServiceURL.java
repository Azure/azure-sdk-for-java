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

import static com.microsoft.azure.storage.blob.Utility.*;

/**
 * Represents a URL to a blob service. This class does not hold any state about a particular storage account but is
 * instead a convenient way of sending off appropriate requests to the resource on the service.
 * It may also be used to construct URLs to blobs and containers.
 * Please see <a href=https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction>here</a> for more
 * information on containers.
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
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_list"Sample code for ServiceURL.listContainersSegment")]
     *
     * @param marker
     *      Identifies the portion of the list to be returned with the next list operation.
     *      This value is returned in the response of a previous list operation as the
     *      ListContainersSegmentResponse.body().nextMarker(). Set to null to list the first segment.
     * @param options
     *      A {@link ListContainersOptions} which specifies what data should be returned by the service.
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceListContainersSegmentResponse> listContainersSegment(
            String marker, ListContainersOptions options) {
        options = options == null ? ListContainersOptions.DEFAULT : options;
        return addErrorWrappingToSingle(
                this.storageClient.generatedServices().listContainersSegmentWithRestResponseAsync(options.getPrefix(),
                marker, options.getMaxResults(), options.getDetails().toIncludeType(), null, null));
    }

    /**
     * Gets the properties of a storage account’s Blob service. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-properties">Azure Docs</a>.
     *
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_getsetprops "Sample code for ServiceURL.getProperties")]
     *
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceGetPropertiesResponse> getProperties() {
        return addErrorWrappingToSingle(
                this.storageClient.generatedServices().getPropertiesWithRestResponseAsync(null, null));
    }

    /**
     * Sets properties for a storage account's Blob service endpoint. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/set-blob-service-properties">Azure Docs</a>.
     * Note that setting the default service version has no effect when using this client because this client explicitly
     * sets the version header on each request, overriding the default.
     *
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_getsetprops "Sample code for ServiceURL.setProperties")]
     *
     * @param properties
     *      Configures the service.
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceSetPropertiesResponse> setProperties(StorageServiceProperties properties) {
        return addErrorWrappingToSingle(
                this.storageClient.generatedServices().setPropertiesWithRestResponseAsync(properties, null,
                null));
    }

    /**
     * Retrieves statistics related to replication for the Blob service. It is only available on the secondary
     * location endpoint when read-access geo-redundant replication is enabled for the storage account. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storageservices/get-blob-service-stats">Azure Docs</a>.
     *
     * @apiNote
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=service_stats "Sample code for ServiceURL.getStats")]
     *
     * @return
     *      Emits the successful response.
     */
    public Single<ServiceGetStatisticsResponse> getStatistics() {
        return addErrorWrappingToSingle(
                this.storageClient.generatedServices().getStatisticsWithRestResponseAsync(null, null));
    }
}
