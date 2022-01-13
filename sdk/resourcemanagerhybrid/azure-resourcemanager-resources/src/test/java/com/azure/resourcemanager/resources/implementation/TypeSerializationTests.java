// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.resources.models.DeploymentProperties;
import com.azure.resourcemanager.resources.fluent.models.DeploymentExtendedInner;
import com.azure.resourcemanager.resources.fluent.models.DeploymentInner;
import com.azure.resourcemanager.resources.models.Tags;
import com.azure.resourcemanager.resources.models.TagsPatchOperation;
import com.azure.resourcemanager.resources.models.TagsPatchResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TypeSerializationTests {

    public static final class Map1<K, V> extends AbstractMap<K, V> {
        private final K k0;
        private final V v0;

        public Map1(K k0, V v0) {
            this.k0 = Objects.requireNonNull(k0);
            this.v0 = Objects.requireNonNull(v0);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            Entry<K, V> entry = new AbstractMap.SimpleEntry<>(k0, v0);
            return new HashSet<>(Collections.singletonList(entry));
        }

        @Override
        public V get(Object o) {
            return o.equals(k0) ? v0 : null;
        }

        @Override
        public boolean containsKey(Object o) {
            return o.equals(k0);
        }

        @Override
        public boolean containsValue(Object o) {
            return o.equals(v0); // implicit nullcheck of o
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int hashCode() {
            return k0.hashCode() ^ v0.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Map1<?, ?> map1 = (Map1<?, ?>) o;
            return k0.equals(map1.k0) && v0.equals(map1.v0);
        }
    }

    @Test
    public void testTagsPatchResourceSerialization() throws Exception {
        Map<String, String> tags = new Map1<>("tag.1", "value.1");

        TagsPatchResource tagsPatchResource = new TagsPatchResource()
            .withOperation(TagsPatchOperation.REPLACE)
            .withProperties(new Tags().withTags(tags));

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        String tagsPatchResourceJson = serializerAdapter.serialize(tagsPatchResource, SerializerEncoding.JSON);
        Assertions.assertTrue(tagsPatchResourceJson.contains("tag.1"));
    }

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
