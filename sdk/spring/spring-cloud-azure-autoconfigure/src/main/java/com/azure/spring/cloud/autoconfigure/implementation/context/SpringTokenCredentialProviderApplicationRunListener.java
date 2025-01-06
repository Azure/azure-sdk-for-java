// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * {@link SpringApplicationRunListener SpringApplicationRunListener } for Spring Cloud Azure passwordless to get the custom token credential bean.
 *
 * @since 5.19.0
 */
public class SpringTokenCredentialProviderApplicationRunListener implements SpringApplicationRunListener, Ordered {

    private static final String AZURE_AUTHENTICATION_TEMPLATE_CLASS_NAME =
        "com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        if (ClassUtils.isPresent(AZURE_AUTHENTICATION_TEMPLATE_CLASS_NAME, null)) {
            SpringTokenCredentialProvider.setGlobalApplicationContext(context);
        }
    }
}
