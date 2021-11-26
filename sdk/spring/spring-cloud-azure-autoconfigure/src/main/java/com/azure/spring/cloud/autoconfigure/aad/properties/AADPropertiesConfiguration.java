// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * Configure properties for Azure Active Directory.
 * </p>
 */
@Configuration
@EnableConfigurationProperties({
    AzureGlobalProperties.class,
    AADAuthenticationProperties.class,
    AADResourceServerProperties.class
})
public class AADPropertiesConfiguration implements InitializingBean {

    @Autowired
    AzureGlobalProperties global;

    @Autowired
    AADAuthenticationProperties aad;

    @Override
    public void afterPropertiesSet() throws Exception {
        aad.setDefaultValueFromAzureGlobalProperties(global);
    }
}
