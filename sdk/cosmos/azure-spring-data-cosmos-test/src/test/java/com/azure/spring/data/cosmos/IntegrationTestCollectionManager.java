// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;

import java.time.Duration;

public class IntegrationTestCollectionManager extends AbstractIntegrationTestCollectionManager<CosmosTemplate> {

    @Override
    public ContainerLock createLock(CosmosEntityInformation entityInfo, Duration leaseDuration) {
        return new ContainerLock(template, entityInfo.getContainerName(), leaseDuration);
    }

    @Override
    public CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation entityInfo) {
        return template.createContainerIfNotExists(entityInfo);
    }

    @Override
    public void deleteContainerData(CosmosEntityInformation entityInfo) {
        template.deleteAll(entityInfo.getContainerName(), entityInfo.getJavaType());
    }

    @Override
    public void deleteContainer(CosmosEntityInformation entityInfo) {
        template.deleteContainer(entityInfo.getContainerName());
    }

}
