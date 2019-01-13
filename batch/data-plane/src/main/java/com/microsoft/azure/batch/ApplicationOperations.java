// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.ApplicationGetOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationListOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationSummary;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;

import java.io.IOException;
import java.util.Collection;

/**
 * Performs application-related operations on an Azure Batch account.
 */
public class ApplicationOperations implements IInheritedBehaviors {

    ApplicationOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
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
     * Lists the {@link ApplicationSummary applications} in the Batch account.
     *
     * @return A list of {@link ApplicationSummary} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ApplicationSummary> listApplications() throws BatchErrorException, IOException {
        return listApplications(null);
    }

    /**
     * Lists the {@link ApplicationSummary applications} in the Batch account.
     *
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return A list of {@link ApplicationSummary} objects.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public PagedList<ApplicationSummary> listApplications(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ApplicationListOptions options = new ApplicationListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().applications().list(options);
    }

    /**
     * Gets information about the specified application.
     *
     * @param applicationId The ID of the application to get.
     * @return An {@link ApplicationSummary} containing information about the specified application.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ApplicationSummary getApplication(String applicationId) throws BatchErrorException, IOException {
        return getApplication(applicationId, null);
    }

    /**
     * Gets information about the specified application.
     *
     * @param applicationId The ID of the application to get.
     * @param additionalBehaviors A collection of {@link BatchClientBehavior} instances that are applied to the Batch service request.
     * @return An {@link ApplicationSummary} containing information about the specified application.
     * @throws BatchErrorException Exception thrown when an error response is received from the Batch service.
     * @throws IOException Exception thrown when there is an error in serialization/deserialization of data sent to/received from the Batch service.
     */
    public ApplicationSummary getApplication(String applicationId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ApplicationGetOptions options = new ApplicationGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.customBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        return this.parentBatchClient.protocolLayer().applications().get(applicationId, options);
    }
}
