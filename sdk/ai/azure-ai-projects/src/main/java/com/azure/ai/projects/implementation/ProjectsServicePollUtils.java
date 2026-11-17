// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.ai.projects.models.JobStatus;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;

final class ProjectsServicePollUtils {
    private ProjectsServicePollUtils() {
    }

    static <T> PollResponse<T> remapStatus(PollResponse<T> response) {
        if (JobStatus.CANCELLED.toString().equalsIgnoreCase(response.getStatus().toString())) {
            return new PollResponse<>(LongRunningOperationStatus.USER_CANCELLED, response.getValue(),
                response.getRetryAfter());
        }
        return response;
    }
}
