// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.fluent.inner.TldLegalAgreementInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** An immutable client-side representation of an Azure domain legal agreement. */
@Fluent
public interface DomainLegalAgreement extends HasInner<TldLegalAgreementInner> {
    /** @return unique identifier for the agreement */
    String agreementKey();

    /** @return agreement title */
    String title();

    /** @return agreement details */
    String content();

    /** @return url where a copy of the agreement details is hosted */
    String url();
}
