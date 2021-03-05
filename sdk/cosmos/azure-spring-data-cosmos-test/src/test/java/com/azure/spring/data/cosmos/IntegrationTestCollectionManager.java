// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public abstract class IntegrationTestCollectionManager<T> implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestCollectionManager.class);
    private static final Duration LEASE_DURATION = Duration.ofMinutes(5);

    protected T template;
    private Map<Class, ContainerRefs> containerRefs = new HashMap<>();
    private boolean isSetupDone;

    protected abstract ContainerLock createLock(CosmosEntityInformation entityInfo, Duration leaseDuration);
    protected abstract CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation entityInfo);
    protected abstract void deleteContainerData(CosmosEntityInformation entityInfo);
    protected abstract void deleteContainer(CosmosEntityInformation entityInfo);

    public void ensureContainersCreated(T template, Class... entityTypes) {
        if (!isSetupDone) {
            this.template = template;
            initContainerRefs(entityTypes);
            isSetupDone = true;
        } else {
            refreshContainerLeases();
        }
    }

    private void initContainerRefs(Class[] entityTypes) {
        for (Class entityType : entityTypes) {
            CosmosEntityInformation entityInfo = new CosmosEntityInformation(entityType);
            CosmosContainerProperties properties = createContainerIfNotExists(entityInfo);
            ContainerLock lock = createLock(entityInfo, LEASE_DURATION);
            lock.acquire(LEASE_DURATION.multipliedBy(2));
            containerRefs.put(entityType, new ContainerRefs(entityInfo, properties, lock));
        }
    }

    public void ensureContainersCreatedAndEmpty(T template, Class... entityTypes) {
        ensureContainersCreated(template, entityTypes);
        deleteContainerData();
    }

    public <E> CosmosEntityInformation<E, ?> getEntityInformation(Class<E> entityType) {
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
            deleteContainerData(containerRef.cosmosEntityInformation);
        }
    }

    private void refreshContainerLeases() {
        for (ContainerRefs containerRef : containerRefs.values()) {
            containerRef.lock.renew();
        }
    }

    private void deleteContainers() {
        // since we're deleting the containers, there's no need to release the locks
        for (ContainerRefs containerRef : containerRefs.values()) {
            try {
                deleteContainer(containerRef.cosmosEntityInformation);
            } catch (Exception ex) {
                LOGGER.info("Failed to delete container=" + containerRef.getContainerName());
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

    public static class Synchronous extends IntegrationTestCollectionManager<CosmosTemplate> {

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

    public static class Reactive extends IntegrationTestCollectionManager<ReactiveCosmosTemplate> {

        @Override
        public ContainerLock createLock(CosmosEntityInformation entityInfo, Duration leaseDuration) {
            return new ContainerLock(template, entityInfo.getContainerName(), leaseDuration);
        }

        @Override
        public CosmosContainerProperties createContainerIfNotExists(CosmosEntityInformation entityInfo) {
            return template.createContainerIfNotExists(entityInfo).block().getProperties();
        }

        @Override
        public void deleteContainerData(CosmosEntityInformation entityInfo) {
            template.deleteAll(entityInfo.getContainerName(), entityInfo.getJavaType()).block();
        }

        @Override
        public void deleteContainer(CosmosEntityInformation entityInfo) {
            template.deleteContainer(entityInfo.getContainerName());
        }

    }
}
