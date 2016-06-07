/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Parameters for a CloudJobOperations.Terminate request.
 */
public class JobTerminateParameter {
    /**
     * The text you want to appear as the job's TerminateReason. The default
     * is 'UserTerminate'.
     */
    private String terminateReason;

    /**
     * Get the terminateReason value.
     *
     * @return the terminateReason value
     */
    public String terminateReason() {
        return this.terminateReason;
    }

    /**
     * Set the terminateReason value.
     *
     * @param terminateReason the terminateReason value to set
     * @return the JobTerminateParameter object itself.
     */
    public JobTerminateParameter withTerminateReason(String terminateReason) {
        this.terminateReason = terminateReason;
        return this;
    }

}
