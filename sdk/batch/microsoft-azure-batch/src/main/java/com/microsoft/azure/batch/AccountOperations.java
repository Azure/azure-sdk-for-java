// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.*;
import com.microsoft.azure.batch.protocol.models.AccountListSupportedImagesOptions;
import com.microsoft.azure.batch.protocol.models.ImageInformation;

import java.io.IOException;
import java.util.Collection;

/**
 * Performs account-related operations on an Azure Batch account.
 */
public class AccountOperations implements IInheritedBehaviors {

    AccountOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.inheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> customBehaviors;

    private final BatchClient parentBatchClient;

    /**
     * Gets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @return A collection of {@link BatchClientBehavior} instances.
     */
    @Override
    public Collection<BatchClientBehavior> customBehaviors() {
        return customBehaviors;
    }

    /**
     * Sets a collection of behaviors that modify or customize requests to the Batch service.
     *
     * @param behaviors The collection of {@link BatchClientBehavior} instances.
     * @return The current instance.
     */
    @Override
    public IInheritedBehaviors withCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        customBehaviors = behaviors;
        return this;
    }

    /**
     * Lists the node agent SKU values supported by the Batch service.
     *
     * @return A list of {@link ImageInformation} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException         Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ImageInformation> listSupportedImages() throws BatchErrorException, IOException {
        return listSupportedImages(null, null);
    }

    /**
     * Lists the node agent SKU values supported by the Batch service.
     *
     * @param detailLevel A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @return A list of {@link ImageInformation} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException         Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ImageInformation> listSupportedImages(DetailLevel detailLevel) throws BatchErrorException, IOException {
        return listSupportedImages(detailLevel, null);
    }

    /**
     * Lists the node agent SKU values supported by the Batch service.
     *
     * @param detailLevel         A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link ImageInformation} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException         Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ImageInformation> listSupportedImages(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        AccountListSupportedImagesOptions options = new AccountListSupportedImagesOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().accounts().listSupportedImages(options);
    }

    /**
     * Gets the number of nodes in each state, grouped by pool.
     *
     * @return A list of {@link PoolNodeCounts} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException         Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<PoolNodeCounts> listPoolNodeCounts() throws BatchErrorException, IOException {
        return listPoolNodeCounts(null, null);
    }

    /**
     * Gets the number of nodes in each state, grouped by pool.
     *
     * @param detailLevel         A {@link DetailLevel} used for filtering the list and for controlling which properties are retrieved from the service.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link PoolNodeCounts} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException         Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<PoolNodeCounts> listPoolNodeCounts(DetailLevel detailLevel, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        AccountListPoolNodeCountsOptions options = new AccountListPoolNodeCountsOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.appendDetailLevelToPerCallBehaviors(detailLevel);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().accounts().listPoolNodeCounts(options);
    }
}
