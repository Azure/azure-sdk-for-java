/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.SecurityGroupNetworkInterface;
import com.microsoft.azure.management.network.SecurityGroupViewResult;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation of SecurityGroupViewResult.
 */
@LangDefinition
class SecurityGroupViewResultImpl extends WrapperImpl<SecurityGroupViewResultInner> implements SecurityGroupViewResult {
    private Map<String, SecurityGroupNetworkInterface> networkInterfaces;

    SecurityGroupViewResultImpl(SecurityGroupViewResultInner innerObject) {
        super(innerObject);
        initializeFromInner();
    }

    private void initializeFromInner() {
        this.networkInterfaces = new TreeMap<>();
        List<SecurityGroupNetworkInterface> securityGroupNetworkInterfaces = this.inner().networkInterfaces();
        if (securityGroupNetworkInterfaces != null) {
            for (SecurityGroupNetworkInterface networkInterface : securityGroupNetworkInterfaces) {
                this.networkInterfaces.put(networkInterface.id(), networkInterface);
            }
        }
    }

    @Override
    public Map<String, SecurityGroupNetworkInterface> networkInterfaces() {
        return Collections.unmodifiableMap(this.networkInterfaces);
    }
}