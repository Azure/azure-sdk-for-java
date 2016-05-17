/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.models.ApplicationGetHeaders;
import com.microsoft.azure.batch.protocol.models.ApplicationGetOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationListHeaders;
import com.microsoft.azure.batch.protocol.models.ApplicationListOptions;
import com.microsoft.azure.batch.protocol.models.ApplicationSummary;
import com.microsoft.azure.batch.protocol.models.BatchErrorException;
import com.microsoft.rest.ServiceResponseWithHeaders;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class ApplicationOperations  implements IInheritedBehaviors {

    ApplicationOperations(BatchClient batchClient, Collection<BatchClientBehavior> customBehaviors) {
        _parentBatchClient = batchClient;

        // inherit from instantiating parent
        InternalHelper.InheritClientBehaviorsAndSetPublicProperty(this, customBehaviors);
    }

    private Collection<BatchClientBehavior> _customBehaviors;

    private BatchClient _parentBatchClient;

    @Override
    public Collection<BatchClientBehavior> getCustomBehaviors() {
        return _customBehaviors;
    }

    @Override
    public void setCustomBehaviors(Collection<BatchClientBehavior> behaviors) {
        _customBehaviors = behaviors;
    }

    public List<ApplicationSummary> listApplications() throws BatchErrorException, IOException {
        return listApplications(null);
    }

    public List<ApplicationSummary> listApplications(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ApplicationListOptions options = new ApplicationListOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<ApplicationSummary>, ApplicationListHeaders> response = this._parentBatchClient.getProtocolLayer().applications().list(options);

        return response.getBody();
    }

    public ApplicationSummary getApplication(String applicationId) throws BatchErrorException, IOException {
        return getApplication(applicationId, null);
    }

    public ApplicationSummary getApplication(String applicationId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ApplicationGetOptions options = new ApplicationGetOptions();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<ApplicationSummary, ApplicationGetHeaders> response = this._parentBatchClient.getProtocolLayer().applications().get(applicationId, options);

        return response.getBody();
    }
}
