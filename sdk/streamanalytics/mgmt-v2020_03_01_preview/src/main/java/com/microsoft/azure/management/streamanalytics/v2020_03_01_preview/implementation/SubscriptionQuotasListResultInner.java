/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.streamanalytics.v2020_03_01_preview.implementation;

import java.util.List;
import com.microsoft.azure.management.streamanalytics.v2020_03_01_preview.SubscriptionQuota;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the GetQuotas operation. It contains a list of quotas for the
 * subscription in a particular region.
 */
public class SubscriptionQuotasListResultInner {
    /**
     * List of quotas for the subscription in a particular region.
     */
    @JsonProperty(value = "value", access = JsonProperty.Access.WRITE_ONLY)
    private List<SubscriptionQuota> value;

    /**
     * Get list of quotas for the subscription in a particular region.
     *
     * @return the value value
     */
    public List<SubscriptionQuota> value() {
        return this.value;
    }

}
