// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.passwordless;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;

import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreNull;
import static com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils.copyPropertiesIgnoreTargetNonNull;

/**
 * Util class for passwordless connections.
 *
 * @since 4.7.0
 */
public class PasswordlessUtil {

    private PasswordlessUtil() {
    }

    /**
     * Merge properties in AzureGlobalProperties and AzurePasswordlessProperties, return a new AzurePasswordlessProperties instance.
     * If the properties in AzurePasswordlessProperties are not set, it will try to get values from AzureGlobalProperties.  <p/>
     * The type of managedIdentityEnabled in Credential is boolean, and it has default value false, it will not get value from AzureGlobalProperties.
     *
     * @param azureGlobalProperties  An AzureGlobalProperties instance contains global properties.
     * @param passwordlessProperties An AzurePasswordlessProperties instance contains passwordless specific properties.
     * @return An AzurePasswordlessProperties instance contains the merged results.
     */
    public static AzurePasswordlessProperties mergeAzureProperties(AzureGlobalProperties azureGlobalProperties, AzurePasswordlessProperties passwordlessProperties) {
        AzurePasswordlessProperties result = new AzurePasswordlessProperties();
        result.setScopes(passwordlessProperties.getScopes());
        result.getCredential().setManagedIdentityEnabled(passwordlessProperties.isPasswordlessEnabled());

        copyPropertiesIgnoreNull(passwordlessProperties.getCredential(), result.getCredential());
        copyPropertiesIgnoreNull(passwordlessProperties.getProfile(), result.getProfile());
        copyPropertiesIgnoreNull(passwordlessProperties.getClient(), result.getClient());
        copyPropertiesIgnoreNull(passwordlessProperties.getProxy(), result.getProxy());

        if (azureGlobalProperties != null) {
            copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getCredential(), result.getCredential());
            copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getProfile(), result.getProfile());
            copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getClient(), result.getClient());
            copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getProxy(), result.getProxy());
        }

        return result;
    }
}
