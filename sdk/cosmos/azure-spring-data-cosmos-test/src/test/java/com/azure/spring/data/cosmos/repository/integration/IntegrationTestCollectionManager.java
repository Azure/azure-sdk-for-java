// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.integration;

import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.repository.support.CosmosEntityInformation;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class IntegrationTestCollectionManager implements TestRule {

    private static Logger LOGGER = LoggerFactory.getLogger(IntegrationTestCollectionManager.class);

    private CosmosTemplate template;
    private Map<Class, ContainerRefs> containerRefs = new HashMap<>();
    private boolean isSetupDone;

    public void ensureContainersCreatedAndEmpty(CosmosTemplate template, Class... entityTypes) {
        if (!isSetupDone) {
            this.template = template;
            for (Class entityType : entityTypes) {
                final CosmosEntityInformation entityInfo = new CosmosEntityInformation(entityType);
                final CosmosContainerProperties properties = template.createContainerIfNotExists(entityInfo);
                containerRefs.put(entityType, new ContainerRefs(entityInfo, properties));
            }
            isSetupDone = true;
        }
        deleteContainerData();
    }

    public <T> CosmosEntityInformation<T, ?> getEntityInformation(Class<T> entityType) {
        return containerRefs.get(entityType).getCosmosEntityInformation();
    }

    public CosmosContainerProperties getContainerProperties(Class entityType) {
        return containerRefs.get(entityType).getCosmosContainerProperties();
    }

    private void deleteContainerData() {
        for (ContainerRefs containerRef : containerRefs.values()) {
            template.deleteAll(containerRef.getContainerName(), containerRef.getJavaType());
        }
    }

    private void deleteContainers() {
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

        private CosmosEntityInformation cosmosEntityInformation;
        private CosmosContainerProperties cosmosContainerProperties;

        public ContainerRefs(CosmosEntityInformation cosmosEntityInformation, CosmosContainerProperties cosmosContainerProperties) {
            this.cosmosEntityInformation = cosmosEntityInformation;
            this.cosmosContainerProperties = cosmosContainerProperties;
        }

        public CosmosEntityInformation getCosmosEntityInformation() {
            return cosmosEntityInformation;
        }

        public CosmosContainerProperties getCosmosContainerProperties() {
            return cosmosContainerProperties;
        }

        public String getContainerName() {
            return cosmosEntityInformation.getContainerName();
        }

        public Class getJavaType() {
            return cosmosEntityInformation.getJavaType();
        }

    }
}
