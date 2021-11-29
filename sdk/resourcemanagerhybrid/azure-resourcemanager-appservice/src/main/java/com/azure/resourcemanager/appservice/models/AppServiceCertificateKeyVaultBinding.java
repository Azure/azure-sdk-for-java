// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.models.AppServiceCertificateResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.IndependentChildResource;

/** An immutable client-side representation of an Azure App Service Key Vault binding. */
@Fluent
public interface AppServiceCertificateKeyVaultBinding
    extends IndependentChildResource<AppServiceManager, AppServiceCertificateResourceInner> {
    /** @return the key vault resource Id */
    String keyVaultId();

    /** @return the key vault secret name */
    String keyVaultSecretName();

    /** @return the status of the Key Vault secret */
    KeyVaultSecretStatus provisioningState();
}
