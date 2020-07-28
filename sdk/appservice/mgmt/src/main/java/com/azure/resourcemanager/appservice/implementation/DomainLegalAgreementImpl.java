// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.resourcemanager.appservice.models.DomainLegalAgreement;
import com.azure.resourcemanager.appservice.fluent.inner.TldLegalAgreementInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Implementation for {@link DomainLegalAgreement}. */
final class DomainLegalAgreementImpl extends WrapperImpl<TldLegalAgreementInner> implements DomainLegalAgreement {

    DomainLegalAgreementImpl(TldLegalAgreementInner innerModel) {
        super(innerModel);
    }

    @Override
    public String agreementKey() {
        return inner().agreementKey();
    }

    @Override
    public String title() {
        return inner().title();
    }

    @Override
    public String content() {
        return inner().content();
    }

    @Override
    public String url() {
        return inner().url();
    }
}
