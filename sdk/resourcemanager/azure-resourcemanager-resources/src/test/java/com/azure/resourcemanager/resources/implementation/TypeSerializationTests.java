// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.models.DeploymentProperties;
import com.azure.resourcemanager.resources.fluent.models.DeploymentExtendedInner;
import com.azure.resourcemanager.resources.fluent.models.DeploymentInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class TypeSerializationTests {

    @Test
    public void testDeploymentSerialization() throws Exception {
        final String templateJson = "{ \"/subscriptions/<redacted>/resourceGroups/<redacted>/providers/Microsoft.ManagedIdentity/userAssignedIdentities/<redacted>\": {} }";

        DeploymentImpl deployment = new DeploymentImpl(new DeploymentExtendedInner(), "", null);
        deployment.withTemplate(templateJson);

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        String deploymentJson = serializerAdapter.serialize(createRequestFromInner(deployment), SerializerEncoding.JSON);
        Assertions.assertTrue(deploymentJson.contains("Microsoft.ManagedIdentity"));
    }

    private static DeploymentInner createRequestFromInner(DeploymentImpl deployment) throws NoSuchFieldException, IllegalAccessException {
        Field field = DeploymentImpl.class.getDeclaredField("deploymentCreateUpdateParameters");
        field.setAccessible(true);
        DeploymentInner implInner = (DeploymentInner) field.get(deployment);

        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(deployment.mode());
        inner.properties().withTemplate(implInner.properties().template());
        inner.properties().withTemplateLink(deployment.templateLink());
        inner.properties().withParameters(deployment.parameters());
        inner.properties().withParametersLink(deployment.parametersLink());
        return inner;
    }
}
