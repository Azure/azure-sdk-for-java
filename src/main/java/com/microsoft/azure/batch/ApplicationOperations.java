/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.batch.protocol.implementation.api.ApplicationGetHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.ApplicationGetOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ApplicationListHeadersInner;
import com.microsoft.azure.batch.protocol.implementation.api.ApplicationListOptionsInner;
import com.microsoft.azure.batch.protocol.implementation.api.ApplicationSummaryInner;
import com.microsoft.azure.batch.protocol.implementation.api.BatchErrorException;
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

    public List<ApplicationSummaryInner> listApplications() throws BatchErrorException, IOException {
        return listApplications(null);
    }

    public List<ApplicationSummaryInner> listApplications(Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ApplicationListOptionsInner options = new ApplicationListOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<PagedList<ApplicationSummaryInner>, ApplicationListHeadersInner> response = this._parentBatchClient.getProtocolLayer().applications().list(options);

        return response.getBody();
    }

    public ApplicationSummaryInner getApplication(String applicationId) throws BatchErrorException, IOException {
        return getApplication(applicationId, null);
    }

    public ApplicationSummaryInner getApplication(String applicationId, Iterable<BatchClientBehavior> additionalBehaviors) throws BatchErrorException, IOException {
        ApplicationGetOptionsInner options = new ApplicationGetOptionsInner();
        BehaviorManager bhMgr = new BehaviorManager(this.getCustomBehaviors(), additionalBehaviors);
        bhMgr.applyRequestBehaviors(options);

        ServiceResponseWithHeaders<ApplicationSummaryInner, ApplicationGetHeadersInner> response = this._parentBatchClient.getProtocolLayer().applications().get(applicationId, options);

        return response.getBody();
    }
}
