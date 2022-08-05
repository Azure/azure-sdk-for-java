// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProvider;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProviderOptions;
import com.azure.spring.cloud.service.implementation.identity.impl.StaticAccessTokenCache;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.CacheableTokenCredential;
import com.azure.spring.cloud.service.implementation.identity.impl.credential.adapter.CacheableSpringTokenCredential;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

public class SpringTokenCredentialProvider implements TokenCredentialProvider, ApplicationContextAware {

    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "springCloudAzureDefaultCredential";
    private static ApplicationContext globalApplicationContext;
    private ApplicationContext applicationContext;
    private TokenCredentialProviderOptions options;
    private final StaticAccessTokenCache cache = new StaticAccessTokenCache();
    private String tokenCredentialBeanName = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

    public SpringTokenCredentialProvider() {

    }

    public SpringTokenCredentialProvider(TokenCredentialProviderOptions options) {
        this.options = options;
        String beanName = options.getTokenCredentialBeanName();
        if (beanName != null && !beanName.isEmpty()) {
            this.tokenCredentialBeanName = beanName;
        }
    }

    public TokenCredential get() {
        ApplicationContext context = getApplicationContext();
        Objects.requireNonNull(context);

        TokenCredential tokenCredential = context.getBean(this.tokenCredentialBeanName, TokenCredential.class);
        CacheableSpringTokenCredential delegate = new CacheableSpringTokenCredential(options, tokenCredential);

        boolean cachedEnabled = options.isCachedEnabled();
        if (cachedEnabled) {
            return new CacheableTokenCredential(cache, delegate);
        } else {
            return tokenCredential;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static void setGlobalApplicationContext(ApplicationContext applicationContext) {
        globalApplicationContext = applicationContext;
    }

    private ApplicationContext getApplicationContext() {
        return this.applicationContext == null ? globalApplicationContext : this.applicationContext;
    }
}
