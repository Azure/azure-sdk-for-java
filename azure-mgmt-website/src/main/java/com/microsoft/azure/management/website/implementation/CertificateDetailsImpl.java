/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.website.AppServicePlan;
import com.microsoft.azure.management.website.CertificateDetails;
import org.joda.time.DateTime;

/**
 * The implementation for {@link AppServicePlan}.
 */
class CertificateDetailsImpl
    extends
        IndexableWrapperImpl<
                                CertificateDetailsInner>
    implements
        CertificateDetails {

    CertificateDetailsImpl(String key, CertificateDetailsInner innerObject) {
        super(key, innerObject);
    }

    @Override
    public Integer version() {
        return inner().version();
    }

    @Override
    public String serialNumber() {
        return inner().serialNumber();
    }

    @Override
    public String thumbprint() {
        return inner().thumbprint();
    }

    @Override
    public String subject() {
        return inner().subject();
    }

    @Override
    public DateTime notBefore() {
        return inner().notBefore();
    }

    @Override
    public DateTime notAfter() {
        return inner().notAfter();
    }

    @Override
    public String signatureAlgorithm() {
        return inner().signatureAlgorithm();
    }

    @Override
    public String issuer() {
        return inner().issuer();
    }

    @Override
    public String rawData() {
        return inner().rawData();
    }
}
