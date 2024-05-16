// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.implementation.ProxyResourceAccessHelper;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.ReadValueCallback;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ResourceTests {

    private static class ProxyResourceWithSystemData extends ProxyResource {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private SystemData systemData;

        /**
         * Get the systemData value.
         *
         * @return the metadata pertaining to creation and last modification of the resource.
         * */
        public SystemData systemData() {
            return this.systemData;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject().writeJsonField("systemData", systemData).writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link ProxyResourceWithSystemData}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link ProxyResourceWithSystemData} that the JSON stream represented, may return null.
         * @throws IOException If a {@link ProxyResourceWithSystemData} fails to be read from the {@code jsonReader}.
         */
        public static ProxyResourceWithSystemData fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                ProxyResourceWithSystemData proxyResourceWithSystemData = new ProxyResourceWithSystemData();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("id".equals(fieldName)) {
                        ProxyResourceAccessHelper.setId(proxyResourceWithSystemData, reader.getString());
                    } else if ("name".equals(fieldName)) {
                        ProxyResourceAccessHelper.setName(proxyResourceWithSystemData, reader.getString());
                    } else if ("type".equals(fieldName)) {
                        ProxyResourceAccessHelper.setType(proxyResourceWithSystemData, reader.getString());
                    } else if ("systemData".equals(fieldName)) {
                        proxyResourceWithSystemData.systemData = SystemData.fromJson(reader);
                    } else {
                        reader.skipChildren();
                    }
                }

                return proxyResourceWithSystemData;
            });
        }
    }

    private static class ResourceWithSystemData extends Resource {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private SystemData systemData;

        /**
         * Get the systemData value.
         *
         * @return the metadata pertaining to creation and last modification of the resource.
         * */
        public SystemData systemData() {
            return this.systemData;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("location", location())
                .writeMapField("tags", tags(), JsonWriter::writeString)
                .writeJsonField("systemData", systemData)
                .writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link ResourceWithSystemData}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link ResourceWithSystemData} that the JSON stream represented, may return null.
         * @throws IOException If a {@link ResourceWithSystemData} fails to be read from the {@code jsonReader}.
         */
        public static ResourceWithSystemData fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                ResourceWithSystemData resourceWithSystemData = new ResourceWithSystemData();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("id".equals(fieldName)) {
                        ProxyResourceAccessHelper.setId(resourceWithSystemData, reader.getString());
                    } else if ("name".equals(fieldName)) {
                        ProxyResourceAccessHelper.setName(resourceWithSystemData, reader.getString());
                    } else if ("type".equals(fieldName)) {
                        ProxyResourceAccessHelper.setType(resourceWithSystemData, reader.getString());
                    } else if ("location".equals(fieldName)) {
                        resourceWithSystemData.withLocation(reader.getString());
                    } else if ("tags".equals(fieldName)) {
                        resourceWithSystemData.withTags(reader.readMap(JsonReader::getString));
                    } else if ("systemData".equals(fieldName)) {
                        resourceWithSystemData.systemData = SystemData.fromJson(reader);
                    } else {
                        reader.skipChildren();
                    }
                }

                return resourceWithSystemData;
            });
        }
    }

    private static class SubResourceResource extends SubResource {
        private SubResource subResource;
        private List<SubResource> subResourceList;
        private SubResourceResource subResourceResource;
        private List<SubResourceResource> subResourceResourceList;

        public SubResource subResource() {
            return subResource;
        }

        public SubResourceResource withSubResource(SubResource subResource) {
            this.subResource = subResource;
            return this;
        }

        public List<SubResource> subResourceList() {
            return subResourceList;
        }

        public SubResourceResource withSubResourceList(List<SubResource> subResourceList) {
            this.subResourceList = subResourceList;
            return this;
        }

        public SubResourceResource subResourceResource() {
            return subResourceResource;
        }

        public SubResourceResource withSubResourceResource(SubResourceResource subResourceResource) {
            this.subResourceResource = subResourceResource;
            return this;
        }

        public List<SubResourceResource> subResourceResourceList() {
            return subResourceResourceList;
        }

        public SubResourceResource withSubResourceResourceList(List<SubResourceResource> subResourceResourceList) {
            this.subResourceResourceList = subResourceResourceList;
            return this;
        }

        @Override
        public SubResourceResource withId(String id) {
            super.withId(id);
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("id", id())
                .writeJsonField("subResource", this.subResource)
                .writeArrayField("subResourceList", this.subResourceList, JsonWriter::writeJson)
                .writeJsonField("subResourceResource", this.subResourceResource)
                .writeArrayField("subResourceResourceList", this.subResourceResourceList, JsonWriter::writeJson)
                .writeEndObject();
        }

        /**
         * Reads a JSON stream into a {@link SubResourceResource}.
         *
         * @param jsonReader The {@link JsonReader} being read.
         * @return The {@link SubResourceResource} that the JSON stream represented, may return null.
         * @throws IOException If a {@link SubResourceResource} fails to be read from the {@code jsonReader}.
         */
        public static SubResourceResource fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                SubResourceResource subResource = new SubResourceResource();

                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("id".equals(fieldName)) {
                        subResource.withId(reader.getString());
                    } else if ("subResource".equals(fieldName)) {
                        subResource.withSubResource(reader.readObject(reader1 -> SubResource.fromJson(reader1)));
                    } else if ("subResourceList".equals(fieldName)) {
                        subResource.withSubResourceList(reader.readArray(reader1 -> SubResource.fromJson(reader1)));
                    } else if ("subResourceResource".equals(fieldName)) {
                        subResource.withSubResourceResource(
                            reader.readObject(reader1 -> SubResourceResource.fromJson(reader1)));
                    } else if ("subResourceResourceList".equals(fieldName)) {
                        subResource.withSubResourceResourceList(
                            reader.readArray(reader1 -> SubResourceResource.fromJson(reader1)));
                    } else {
                        reader.skipChildren();
                    }
                }

                return subResource;
            });
        }
    }

    @Test
    public void testSerialization() throws IOException {
        String cosmosAccountJson
            = "{\"id\":\"/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.DocumentDB/databaseAccounts/c1weidxu\","
                + "\"name\":\"c1weidxu\",\"location\":\"West US\",\"type\":\"Microsoft.DocumentDB/databaseAccounts\",\"kind\":\"GlobalDocumentDB\","
                + "\"tags\":{\"defaultExperience\":\"Core (SQL)\",\"hidden-cosmos-mmspecial\":\"\",\"CosmosAccountType\":\"Non-Production\"},"
                + "\"systemData\":{\"createdBy\":\"00000000-0000-0000-0000-000000000000\",\"createdByType\":\"Application\",\"createdAt\":\"2021-03-03T02:03:46.3387771Z\","
                + "\"lastModifiedBy\":\"johndoe@microsoft.com\",\"lastModifiedByType\":\"User\",\"lastModifiedAt\":\"2021-03-03T02:04:42.8252362Z\"},"
                + "\"properties\":{\"provisioningState\":\"Succeeded\",\"documentEndpoint\":\"https://c1weidxu.documents.azure.com:443/\",\"publicNetworkAccess\":\"Enabled\","
                + "\"enableAutomaticFailover\":false,\"enableMultipleWriteLocations\":false,\"enablePartitionKeyMonitor\":false,\"isVirtualNetworkFilterEnabled\":false,"
                + "\"virtualNetworkRules\":[],\"EnabledApiTypes\":\"Sql\",\"disableKeyBasedMetadataWriteAccess\":false,\"enableFreeTier\":false,\"enableAnalyticalStorage\":false,"
                + "\"instanceId\":\"f5a124e6-988e-4936-8c9b-38e011c80ef4\",\"createMode\":\"Default\",\"databaseAccountOfferType\":\"Standard\",\"enableCassandraConnector\":false,"
                + "\"connectorOffer\":\"\",\"consistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\",\"maxIntervalInSeconds\":5,\"maxStalenessPrefix\":100},"
                + "\"configurationOverrides\":{},\"writeLocations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\","
                + "\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],"
                + "\"readLocations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\","
                + "\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"locations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\","
                + "\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],"
                + "\"failoverPolicies\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"failoverPriority\":0}],\"cors\":[],\"capabilities\":[],\"ipRules\":[],"
                + "\"backupPolicy\":{\"type\":\"Periodic\",\"periodicModeProperties\":{\"backupIntervalInMinutes\":240,\"backupRetentionIntervalInHours\":8}}}}";

        ResourceWithSystemData cosmosAccountResource = deserialize(cosmosAccountJson, ResourceWithSystemData::fromJson);
        Assertions.assertEquals(
            "/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.DocumentDB/databaseAccounts/c1weidxu",
            cosmosAccountResource.id());
        Assertions.assertEquals(Region.US_WEST, Region.fromName(cosmosAccountResource.location()));
        Assertions.assertEquals("Microsoft.DocumentDB/databaseAccounts", cosmosAccountResource.type());
        Assertions.assertEquals(3, cosmosAccountResource.tags().size());
        Assertions.assertNotNull(cosmosAccountResource.systemData());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("00000000-0000-0000-0000-000000000000", cosmosAccountResource.systemData().createdBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.APPLICATION,
            cosmosAccountResource.systemData().createdByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("johndoe@microsoft.com", cosmosAccountResource.systemData().lastModifiedBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.USER,
            cosmosAccountResource.systemData().lastModifiedByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().lastModifiedAt());

        ProxyResourceWithSystemData proxyResource
            = deserialize(cosmosAccountJson, ProxyResourceWithSystemData::fromJson);
        Assertions.assertNotNull(proxyResource.systemData());
        Assertions.assertNotNull(proxyResource.systemData().createdAt());
        Assertions.assertEquals("00000000-0000-0000-0000-000000000000", proxyResource.systemData().createdBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.APPLICATION, proxyResource.systemData().createdByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("johndoe@microsoft.com", cosmosAccountResource.systemData().lastModifiedBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.USER,
            cosmosAccountResource.systemData().lastModifiedByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().lastModifiedAt());

        String vaultJson
            = "{\"id\":\"/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu\",\"name\":\"v1weidxu\","
                + "\"type\":\"Microsoft.KeyVault/vaults\",\"location\":\"centralus\",\"tags\":{},\"properties\":{\"sku\":{\"family\":\"A\",\"name\":\"standard\"},"
                + "\"tenantId\":\"###\",\"accessPolicies\":[],\"enabledForDeployment\":false,\"vaultUri\":\"https://v1weidxu.vault.azure.net/\",\"provisioningState\":\"Succeeded\"}}";
        ResourceWithSystemData vaultResource = deserialize(vaultJson, ResourceWithSystemData::fromJson);
        Assertions.assertEquals(
            "/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu",
            vaultResource.id());
        Assertions.assertEquals(Region.US_CENTRAL, Region.fromName(vaultResource.location()));
        Assertions.assertEquals("Microsoft.KeyVault/vaults", vaultResource.type());
        Assertions.assertEquals(0, vaultResource.tags().size());
        Assertions.assertNull(vaultResource.systemData());

        // test SubResource
        SubResourceResource subResourceResourceRoot = new SubResourceResource();
        SubResource subResource = new SubResource().withId(UUID.randomUUID().toString());
        SubResourceResource subResourceResourceNest = new SubResourceResource().withId(UUID.randomUUID().toString());
        subResourceResourceRoot.withSubResource(subResource)
            .withSubResourceList(Collections.singletonList(subResource))
            .withSubResourceResource(subResourceResourceNest)
            .withSubResourceResourceList(Collections.singletonList(subResourceResourceNest));

        String json = serializeToString(subResourceResourceRoot);
        SubResourceResource subResourceResourceRootDeserialized = deserialize(json, SubResourceResource::fromJson);
        Assertions.assertEquals(subResource.id(), subResourceResourceRootDeserialized.subResource().id());
        Assertions.assertEquals(subResource.id(),
            subResourceResourceRootDeserialized.subResourceList().iterator().next().id());
        Assertions.assertEquals(subResourceResourceNest.id(),
            subResourceResourceRootDeserialized.subResourceResource().id());
        Assertions.assertEquals(subResourceResourceNest.id(),
            subResourceResourceRootDeserialized.subResourceResourceList().iterator().next().id());
    }

    private static <T extends JsonSerializable<T>> String serializeToString(T serializable) throws IOException {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = JsonProviders.createWriter(writer);
        serializable.toJson(jsonWriter);
        jsonWriter.flush();
        return writer.toString();
    }

    private static <T> T deserialize(String json, ReadValueCallback<JsonReader, T> reader) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return reader.read(jsonReader);
        }
    }
}
