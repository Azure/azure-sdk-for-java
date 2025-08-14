// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.DomainLegalAgreement;
import com.azure.resourcemanager.appservice.fluent.models.TldLegalAgreementInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Implementation for {@link DomainLegalAgreement}. */
final class DomainLegalAgreementImpl extends WrapperImpl<TldLegalAgreementInner> implements DomainLegalAgreement {

    DomainLegalAgreementImpl(TldLegalAgreementInner innerModel) {
        super(innerModel);
    }

    @Override
    public String agreementKey() {
        return innerModel().agreementKey();
    }

    @Override
    public String title() {
        return innerModel().title();
    }

    @Override
    public String content() {
        return innerModel().content();
    }

    @Override
    public String url() {
        return innerModel().url();
    }
}
