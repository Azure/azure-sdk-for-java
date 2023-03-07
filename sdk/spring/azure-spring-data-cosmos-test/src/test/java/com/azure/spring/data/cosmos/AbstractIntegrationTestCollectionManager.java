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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractIntegrationTestCollectionManager<T> implements TestRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIntegrationTestCollectionManager.class);
    private static final Duration LEASE_DURATION = Duration.ofMinutes(5);
    private static final ConcurrentMap<String, DeleteContainerAction> CONTAINER_CLEANUP_REGISTRY = new ConcurrentHashMap<>();

    static {
        // since collections are sometimes re-used between tests, wait until the end of the test run to delete them
        Runtime.getRuntime().addShutdownHook(new Thread(AbstractIntegrationTestCollectionManager::deleteRegisteredCollections));
    }

    public static void registerContainerForCleanup(Object template, String containerName) {
        DeleteContainerAction action;
        if (template instanceof CosmosTemplate) {
            action = new DeleteContainerAction((CosmosTemplate) template, containerName);
        } else if (template instanceof ReactiveCosmosTemplate) {
            action = new DeleteContainerAction((ReactiveCosmosTemplate) template, containerName);
        } else {
            throw new IllegalStateException("Template must be instance of CosmosTemplate or ReactiveCosmosTemplate, was " + template);
        }
        CONTAINER_CLEANUP_REGISTRY.putIfAbsent(containerName, action);
    }

    private static void deleteRegisteredCollections() {
        CONTAINER_CLEANUP_REGISTRY.values().forEach(DeleteContainerAction::deleteContainer);
    }

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
            registerContainerForCleanup(template, entityInfo.getContainerName());
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

    private void releaseLocks() {
        for (ContainerRefs containerRef : containerRefs.values()) {
            try {
                containerRef.lock.release();
            } catch (Exception ex) {
                LOGGER.info("Failed to delete lock for container=" + containerRef.getContainerName());
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
                    releaseLocks();
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

    }

    private static class DeleteContainerAction {

        private CosmosTemplate template;
        private ReactiveCosmosTemplate reactiveTemplate;
        private String containerName;

        public DeleteContainerAction(CosmosTemplate template, String containerName) {
            this.template = template;
            this.containerName = containerName;
        }

        public DeleteContainerAction(ReactiveCosmosTemplate reactiveTemplate, String containerName) {
            this.reactiveTemplate = reactiveTemplate;
            this.containerName = containerName;
        }

        public void deleteContainer() {
            try {
                if (template != null) {
                    template.deleteContainer(containerName);
                } else {
                    reactiveTemplate.deleteContainer(containerName);
                }
            } catch (Exception ex) {
                LOGGER.info("Failed to delete container=" + containerName);
            }
        }
    }

}
