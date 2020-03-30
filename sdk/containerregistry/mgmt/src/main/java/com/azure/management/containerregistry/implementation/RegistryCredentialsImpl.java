/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerregistry.implementation;

import com.azure.management.containerregistry.AccessKeyType;
import com.azure.management.containerregistry.RegistryCredentials;
import com.azure.management.containerregistry.RegistryPassword;
import com.azure.management.containerregistry.models.RegistryListCredentialsResultInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation for RegistryCredentials.
 */
public class RegistryCredentialsImpl extends WrapperImpl<RegistryListCredentialsResultInner> implements RegistryCredentials {
    private Map<AccessKeyType, String> accessKeys;

    protected RegistryCredentialsImpl(RegistryListCredentialsResultInner innerObject) {
        super(innerObject);

        this.accessKeys = new HashMap<>();
        if (this.inner().passwords() != null) {
            for (RegistryPassword registryPassword : this.inner().passwords()) {
                switch (registryPassword.name()) {
                    case PASSWORD:
                        this.accessKeys.put(AccessKeyType.PRIMARY, registryPassword.value());
                        break;
                    case PASSWORD2:
                        this.accessKeys.put(AccessKeyType.SECONDARY, registryPassword.value());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public Map<AccessKeyType, String> accessKeys() {
        return Collections.unmodifiableMap(this.accessKeys);
    }

    @Override
    public String username() {
        return this.inner().username();
    }
}
