// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.billing.models;

import com.azure.resourcemanager.billing.fluent.models.ValidateSubscriptionTransferEligibilityResultInner;

/** An immutable client-side representation of ValidateSubscriptionTransferEligibilityResult. */
public interface ValidateSubscriptionTransferEligibilityResult {
    /**
     * Gets the isMoveEligible property: Specifies whether the subscription is eligible to be transferred.
     *
     * @return the isMoveEligible value.
     */
    Boolean isMoveEligible();

    /**
     * Gets the errorDetails property: Validation error details.
     *
     * @return the errorDetails value.
     */
    ValidateSubscriptionTransferEligibilityError errorDetails();

    /**
     * Gets the inner com.azure.resourcemanager.billing.fluent.models.ValidateSubscriptionTransferEligibilityResultInner
     * object.
     *
     * @return the inner object.
     */
    ValidateSubscriptionTransferEligibilityResultInner innerModel();
}
