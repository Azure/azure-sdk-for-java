// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.ConfigurationServiceResourceInner;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceGitProperty;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceProperties;
import com.azure.resourcemanager.appplatform.models.ConfigurationServiceSettings;
import com.azure.resourcemanager.appplatform.models.SpringConfigurationService;
import com.azure.resourcemanager.appplatform.models.SpringConfigurationServices;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

public class SpringConfigurationServicesImpl
    extends ExternalChildResourcesNonCachedImpl
        <SpringConfigurationServiceImpl, SpringConfigurationService, ConfigurationServiceResourceInner, SpringServiceImpl, SpringService>
    implements SpringConfigurationServices {

    public SpringConfigurationServicesImpl(SpringServiceImpl parentImpl) {
        super(parentImpl, parentImpl.taskGroup(), "SpringConfigurationService");
    }

    void prepareCreateOrUpdate(ConfigurationServiceGitProperty property) {
        prepareInlineDefine(
            new SpringConfigurationServiceImpl(
                Constants.DEFAULT_TANZU_COMPONENT_NAME,
                parent(),
                new ConfigurationServiceResourceInner()
                    .withProperties(
                        new ConfigurationServiceProperties()
                            .withSettings(
                                new ConfigurationServiceSettings()
                                    .withGitProperty(property))
                    )
            )
        );
    }

    @Override
    public AppPlatformManager manager() {
        return parent().manager();
    }

    @Override
    public SpringServiceImpl parent() {
        return getParent();
    }
}
