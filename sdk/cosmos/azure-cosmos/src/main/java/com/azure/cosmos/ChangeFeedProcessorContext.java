// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.util.Beta;

import java.util.function.BiConsumer;

/**
 * Encapsulates properties which are mapped to a batch of change feed documents
 * processed when {@link  ChangeFeedProcessorBuilder#handleAllVersionsAndDeletesChanges(BiConsumer)}
 * lambda is invoked.
 * <br>
 * <br>
 * NOTE: This interface is not designed to be implemented by end users.
 * */
@Beta(value = Beta.SinceVersion.V4_51_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface ChangeFeedProcessorContext {
    /**
     * Gets the lease token corresponding to the source of
     * a batch of change feed documents.
     *
     * @return the lease token
     * */
    @Beta(value = Beta.SinceVersion.V4_51_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getLeaseToken();

    /**
     * Gets the request charge as request units (RU) consumed in obtaining the change feed batch.
     * <br/>
     * For more information about the RU and factors that can impact the effective charges please visit
     * <a href="https://docs.microsoft.com/en-us/azure/cosmos-db/request-units">Request Units in Azure Cosmos DB</a>
     *
     * @return the request charge.
     */
    @Beta(value = Beta.SinceVersion.V4_56_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    double getRequestCharge();

    /**
     * Gets the session token associated with the change feed batch.
     *
     * @return the session token
     * */
    @Beta(value = Beta.SinceVersion.V4_56_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getSessionToken();

    /**
     * Gets the diagnostics associated with the change feed batch.
     *
     * @return the diagnostics
     * */
    @Beta(value = Beta.SinceVersion.V4_56_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    CosmosDiagnostics getDiagnostics();
}
