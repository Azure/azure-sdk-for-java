// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.credential.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;
import com.azure.identity.extensions.implementation.credential.provider.DefaultTokenCredentialProvider;
import com.azure.identity.extensions.implementation.credential.provider.TokenCredentialProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

/**
 * TokenCredentialProvider implementation for Spring context.
 */
public class SpringTokenCredentialProvider implements TokenCredentialProvider, ApplicationContextAware {

    public static final String DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME = "springCloudAzureDefaultCredential";
    public static final String PASSWORDLESS_TOKEN_CREDENTIAL_BEAN_NAME = "passwordlessTokenCredential";
    private static ApplicationContext globalApplicationContext;
    private ApplicationContext applicationContext;
    private String tokenCredentialBeanName = DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

    /**
     * Create a {@link SpringTokenCredentialProvider} instance with a {@link TokenCredentialProviderOptions}.
     * If option token-credential-bean-name is specified, it will get this bean from application context,
     * otherwise it will delegate {@link DefaultTokenCredentialProvider} to create a token credential.
     * @param options the token credential provider options.
     */
    public SpringTokenCredentialProvider(TokenCredentialProviderOptions options) {
        String beanName = options == null ? null : options.getTokenCredentialBeanName();
        if (beanName != null && !beanName.isEmpty()) {
            this.tokenCredentialBeanName = beanName;
        }
    }

    public TokenCredential get() {
        ApplicationContext context = getApplicationContext();
        Objects.requireNonNull(context);

        return context.getBean(this.tokenCredentialBeanName, TokenCredential.class);
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
