/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.appservice.implementation.AppServiceCertificateInner;

/**
 * An immutable client-side representation of an Azure App Service Certificate.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public interface AppServiceCertificateKeyVaultBinding extends
        IndependentChildResource,
        Wrapper<AppServiceCertificateInner> {
    /**
     * @return the key vault resource Id
     */
    String keyVaultId();

    /**
     * @return the key vault secret name
     */
    String keyVaultSecretName();

    /**
     * @return the status of the Key Vault secret
     */
    KeyVaultSecretStatus provisioningState();
}