// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Patch azure OAuth2ClientProperties.Registration to
 * suppress {@link OAuth2ClientProperties#validate()} validation exception.
 */
public class OAuth2ClientPropertiesBeanPostProcessor
        implements BeanPostProcessor, Ordered, BeanFactoryAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2ClientPropertiesBeanPostProcessor.class);

    private final AADAuthenticationProperties aadAuthenticationProperties;

    private boolean patchAzureOAuth2ClientProperties;

    @Nullable
    private BeanFactory beanFactory;

    public OAuth2ClientPropertiesBeanPostProcessor(AADAuthenticationProperties aadAuthenticationProperties) {
        this.aadAuthenticationProperties = aadAuthenticationProperties;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (this.beanFactory instanceof ListableBeanFactory && bean instanceof OAuth2ClientProperties) {
            OAuth2ClientProperties clientProperties = (OAuth2ClientProperties) bean;
            if (!patchAzureOAuth2ClientProperties && null != aadAuthenticationProperties && null != clientProperties
                && (!StringUtils.isEmpty(aadAuthenticationProperties.getClientId())
                && !StringUtils.isEmpty(aadAuthenticationProperties.getClientSecret()))) {
                Map<String, OAuth2ClientProperties.Registration> registrationMap = clientProperties.getRegistration();
                String azureRegistrationId = ClientRegistrationInitialization.getDefaultClientRegistrationId();
                if (null != registrationMap && registrationMap.containsKey(azureRegistrationId)) {
                    OAuth2ClientProperties.Registration azureRegistration = registrationMap.get(azureRegistrationId);
                    if (StringUtils.isEmpty(azureRegistration.getClientId())
                        && StringUtils.isEmpty(azureRegistration.getClientSecret())) {
                        azureRegistration.setClientId(aadAuthenticationProperties.getClientId());
                        azureRegistration.setClientSecret(aadAuthenticationProperties.getClientSecret());
                        patchAzureOAuth2ClientProperties = true;
                        LOGGER.debug("Patch azure registration configuration "
                            + "'azure.activedirectory.client-id' and 'azure.activedirectory.client-secret' items.");
                    }
                }
            }
        }
        return bean;
    }
}
