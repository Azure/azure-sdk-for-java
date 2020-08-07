/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

/**
 * Credentials to authenticate with AAD. Support below authentication approaches:
 * - Certificate
 * - Secret
 * TODO:
 * - MSI support
 * - Extract to autoconfigure package and reuse by other services
 */
@Validated
public class Credentials {

    @NotEmpty
    private String clientId;

    private String clientSecret;

    private Resource clientCertificate;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Resource getClientCertificate() {
        return clientCertificate;
    }

    public void setClientCertificate(Resource clientCertificate) {
        this.clientCertificate = clientCertificate;
    }
}
