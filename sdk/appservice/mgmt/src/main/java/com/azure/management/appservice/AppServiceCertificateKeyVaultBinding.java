/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.core.annotation.Fluent;
import com.azure.management.appservice.models.AppServiceCertificateResourceInner;
import com.azure.management.appservice.implementation.AppServiceManager;
import com.azure.management.resources.fluentcore.arm.models.IndependentChildResource;

/**
 * An immutable client-side representation of an Azure App Service Key Vault binding.
 */
@Fluent
public interface AppServiceCertificateKeyVaultBinding extends
        IndependentChildResource<AppServiceManager, AppServiceCertificateResourceInner> {
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