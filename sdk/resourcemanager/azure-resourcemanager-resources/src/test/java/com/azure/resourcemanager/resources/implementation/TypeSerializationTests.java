// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.models.DeploymentProperties;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentExtendedInner;
import com.azure.resourcemanager.resources.fluent.inner.DeploymentInner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TypeSerializationTests {

    @Test
    @Disabled("To fix later as swagger changes on DeploymentExtendedInner")
    public void testDeploymentSerialization() throws Exception {
        final String templateJson = "{ \"/subscriptions/<redacted>/resourceGroups/<redacted>/providers/Microsoft.ManagedIdentity/userAssignedIdentities/<redacted>\": {} }";

        DeploymentImpl deployment = new DeploymentImpl(new DeploymentExtendedInner(), "", null);
        deployment.withTemplate(templateJson);

        AzureJacksonAdapter serializerAdapter = new AzureJacksonAdapter();
        String deploymentJson = serializerAdapter.serialize(createRequestFromInner(deployment), SerializerEncoding.JSON);
        Assertions.assertTrue(deploymentJson.contains("Microsoft.ManagedIdentity"));
    }

    private static DeploymentInner createRequestFromInner(DeploymentImpl deployment) {
        DeploymentInner inner = new DeploymentInner()
                .withProperties(new DeploymentProperties());
        inner.properties().withMode(deployment.mode());
        //inner.properties().withTemplate(deployment.template());
        inner.properties().withTemplateLink(deployment.templateLink());
        inner.properties().withParameters(deployment.parameters());
        inner.properties().withParametersLink(deployment.parametersLink());
        return inner;
    }
}
