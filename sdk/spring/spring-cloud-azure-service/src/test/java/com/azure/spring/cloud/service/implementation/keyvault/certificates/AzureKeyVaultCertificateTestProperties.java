// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;

class AzureKeyVaultCertificateTestProperties extends AzureHttpSdkProperties implements CertificateClientProperties {

    private String endpoint;
    private CertificateServiceVersion serviceVersion;

    private boolean challengeResourceVerificationEnabled = true;

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public CertificateServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(CertificateServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public boolean isChallengeResourceVerificationEnabled() {
        return challengeResourceVerificationEnabled;
    }

    public void setChallengeResourceVerificationEnabled(boolean challengeResourceVerificationEnabled) {
        this.challengeResourceVerificationEnabled = challengeResourceVerificationEnabled;
    }
}
