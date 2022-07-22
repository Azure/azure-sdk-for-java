package com.azure.spring.cloud.autoconfigure.jdbc.extension.mysql;// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.extension.mysql.AzureIdentityMysqlAuthenticationPlugin;
import com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc.implementation.JdbcPluginPropertiesUtils;
import com.azure.spring.cloud.autoconfigure.jdbc.SpringJdbcPluginPasswordResolver;
import com.mysql.cj.protocol.Protocol;
import com.mysql.cj.protocol.a.NativePacketPayload;
import org.springframework.context.ApplicationContext;
import java.util.HashMap;
import java.util.Map;

/**
 * The Authentication plugin that enables Azure AD managed identity support.
 */
public class AzureSpringIdentityMysqlAuthenticationPlugin extends AzureIdentityMysqlAuthenticationPlugin {

    private static ApplicationContext context;

    @Override
    public void initPasswordResolver(Protocol<NativePacketPayload> protocol) {
        Map<String, String> map = new HashMap<>();
        JdbcPluginPropertiesUtils.convertPropertySetToConfigMap(protocol.getPropertySet(), map);
        setPasswordResolver(new SpringJdbcPluginPasswordResolver(map));
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void setContext(ApplicationContext context) {
        context = context;
    }
}
