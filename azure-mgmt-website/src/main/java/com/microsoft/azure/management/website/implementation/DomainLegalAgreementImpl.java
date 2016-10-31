/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.website.DomainLegalAgreement;

/**
 * Implementation for {@link Tenant}.
 */
final class DomainLegalAgreementImpl extends
        WrapperImpl<TldLegalAgreementInner>
        implements
        DomainLegalAgreement {

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
