// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public final class CosmosBatchItemRequestOptionsTests  {

    @Test(groups = {"unit"})
    public void populateThroughputControlGroupToRequestOptions() {
        CosmosBatchItemRequestOptions batchRequestOptions = new CosmosBatchItemRequestOptions();

        RequestOptions internalRequestOptions = batchRequestOptions.toRequestOptions();
        assertThat(internalRequestOptions.getThroughputControlGroupName()).isNull();

        CosmosBatchItemRequestOptions batchRequestOptionsReturned =
            batchRequestOptions.setThroughputControlGroupName("SomeThroughputControlGroup");
        assertThat(batchRequestOptionsReturned).isSameAs(batchRequestOptions);

        internalRequestOptions = batchRequestOptions.toRequestOptions();
        assertThat(internalRequestOptions.getThroughputControlGroupName()).isNotNull();
        assertThat(internalRequestOptions.getThroughputControlGroupName()).isEqualTo("SomeThroughputControlGroup");
        
        batchRequestOptions.setThroughputControlGroupName(null);
        internalRequestOptions = batchRequestOptions.toRequestOptions();
        assertThat(internalRequestOptions.getThroughputControlGroupName()).isNull();
    }
}
