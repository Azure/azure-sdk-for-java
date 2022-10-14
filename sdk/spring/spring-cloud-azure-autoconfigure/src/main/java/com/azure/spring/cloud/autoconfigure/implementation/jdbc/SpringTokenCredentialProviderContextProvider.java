// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.spring.cloud.service.implementation.identity.credential.provider.SpringTokenCredentialProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Provide applicationContext for SpringTokenCredentialProvider.
 */
public final class SpringTokenCredentialProviderContextProvider implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringTokenCredentialProvider.setGlobalApplicationContext(applicationContext);
    }
}
