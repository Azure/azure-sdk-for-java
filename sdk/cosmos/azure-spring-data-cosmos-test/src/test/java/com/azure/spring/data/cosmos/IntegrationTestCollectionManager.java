// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class IntegrationTestCollectionManager implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestCollectionManager.class);
    private static final Duration LEASE_DURATION = Duration.ofMinutes(5);

    private CosmosTemplate template;
    private Map<Class, ContainerRefs> containerRefs = new HashMap<>();
    private boolean isSetupDone;

    public void ensureContainersCreated(CosmosTemplate template, Class... entityTypes) {
        if (!isSetupDone) {
            this.template = template;
            for (Class entityType : entityTypes) {
                CosmosEntityInformation entityInfo = new CosmosEntityInformation(entityType);
                CosmosContainerProperties properties = template.createContainerIfNotExists(entityInfo);
                ContainerLock lock = new ContainerLock(template, entityInfo.getContainerName(), LEASE_DURATION);
                lock.acquire(LEASE_DURATION.multipliedBy(2));
                containerRefs.put(entityType, new ContainerRefs(entityInfo, properties, lock));
            }
            isSetupDone = true;
        } else {
            refreshContainerLeases();
        }
    }

    public void ensureContainersCreatedAndEmpty(CosmosTemplate template, Class... entityTypes) {
        ensureContainersCreated(template, entityTypes);
        deleteContainerData();
    }

    public <T> CosmosEntityInformation<T, ?> getEntityInformation(Class<T> entityType) {
        return containerRefs.get(entityType).cosmosEntityInformation;
    }

    public CosmosContainerProperties getContainerProperties(Class entityType) {
        return containerRefs.get(entityType).cosmosContainerProperties;
    }

    public String getContainerName(Class entityType) {
        return containerRefs.get(entityType).getContainerName();
    }

    private void deleteContainerData() {
        for (ContainerRefs containerRef : containerRefs.values()) {
            template.deleteAll(containerRef.getContainerName(), containerRef.getJavaType());
        }
    }

    private void refreshContainerLeases() {
        for (ContainerRefs containerRef : containerRefs.values()) {
            containerRef.lock.renew();
        }
    }

    private void deleteContainers() {
        // since we're deleting the containers, there's no need to release the locks
        for (ContainerRefs entityInfo : containerRefs.values()) {
            try {
                template.deleteContainer(entityInfo.getContainerName());
            } catch (Exception ex) {
                LOGGER.info("Failed to delete container=" + entityInfo.getContainerName());
            }
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    deleteContainers();
                }
            }
        };
    }

    private static class ContainerRefs {

        CosmosEntityInformation cosmosEntityInformation;
        CosmosContainerProperties cosmosContainerProperties;
        ContainerLock lock;

        public ContainerRefs(CosmosEntityInformation cosmosEntityInformation, CosmosContainerProperties cosmosContainerProperties, ContainerLock lock) {
            this.cosmosEntityInformation = cosmosEntityInformation;
            this.cosmosContainerProperties = cosmosContainerProperties;
            this.lock = lock;
        }

        public String getContainerName() {
            return cosmosEntityInformation.getContainerName();
        }

        public Class getJavaType() {
            return cosmosEntityInformation.getJavaType();
        }

    }

}
