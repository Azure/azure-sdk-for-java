// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.fluent.models.TldLegalAgreementInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable client-side representation of an Azure domain legal agreement. */
@Fluent
public interface DomainLegalAgreement extends HasInnerModel<TldLegalAgreementInner> {
    /**
     * Gets unique identifier for the agreement.
     *
     * @return unique identifier for the agreement
     */
    String agreementKey();

    /**
     * Gets agreement title.
     *
     * @return agreement title
     */
    String title();

    /**
     * Gets agreement details.
     *
     * @return agreement details
     */
    String content();

    /**
     * Gets url where a copy of the agreement details is hosted.
     *
     * @return url where a copy of the agreement details is hosted */
    String url();
}
