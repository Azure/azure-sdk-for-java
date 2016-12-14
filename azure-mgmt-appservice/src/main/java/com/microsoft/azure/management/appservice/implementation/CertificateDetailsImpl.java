/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.CertificateDetails;
import org.joda.time.DateTime;

/**
 * The implementation for {@link AppServicePlan}.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class CertificateDetailsImpl
        extends
        IndexableWrapperImpl<
                CertificateDetailsInner>
        implements
        CertificateDetails {

    CertificateDetailsImpl(CertificateDetailsInner innerObject) {
        super(innerObject);
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