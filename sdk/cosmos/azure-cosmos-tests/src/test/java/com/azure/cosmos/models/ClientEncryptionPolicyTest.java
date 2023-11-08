// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientEncryptionPolicyTest {

    @Test(groups = { "unit" })
    public void policyFormatVersionTest() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("{\"id\":\"030ed59b-90cb-4339" +
            "-8d3d-775fda8af59a\",\"indexingPolicy\":{\"indexingMode\":\"consistent\",\"automatic\":true," +
            "\"includedPaths\":[{\"path\":\"\\/*\"}],\"excludedPaths\":[{\"path\":\"\\/\\\"_etag\\\"\\/?\"}]}," +
            "\"partitionKey\":{\"paths\":[\"\\/users\"],\"kind\":\"Hash\"}," +
            "\"conflictResolutionPolicy\":{\"mode\":\"LastWriterWins\",\"conflictResolutionPath\":\"\\/_ts\"," +
            "\"conflictResolutionProcedure\":\"\"},\"allowMaterializedViews\":false," +
            "\"geospatialConfig\":{\"type\":\"Geography\"}," +
            "\"clientEncryptionPolicy\":{\"includedPaths\":[{\"path\":\"\\/path1\"," +
            "\"clientEncryptionKeyId\":\"dekId1\",\"encryptionAlgorithm\":\"AEAD_AES_256_CBC_HMAC_SHA256\"," +
            "\"encryptionType\":\"Randomized\"},{\"path\":\"\\/path2\",\"clientEncryptionKeyId\":\"dekId2\"," +
            "\"encryptionAlgorithm\":\"AEAD_AES_256_CBC_HMAC_SHA256\",\"encryptionType\":\"Deterministic\"}]," +
            "\"policyFormatVersion\":2,\"newproperty\":\"value\"},\"_rid\":\"A3NIAPtyhlE=\",\"_ts\":1621365358," +
            "\"_self\":\"dbs\\/A3NIAA==\\/colls\\/A3NIAPtyhlE=\\/\"," +
            "\"_etag\":\"\\\"00000000-0000-0000-4c1a-3bb9032901d7\\\"\",\"_docs\":\"docs\\/\"," +
            "\"_sprocs\":\"sprocs\\/\",\"_triggers\":\"triggers\\/\",\"_udfs\":\"udfs\\/\"," +
            "\"_conflicts\":\"conflicts\\/\"}\n");

        //Currently service is not returning policyFormatVersion, so validating V2 policy deserialization
        assertThat(containerProperties.getClientEncryptionPolicy().getPolicyFormatVersion()).isEqualTo(2);
    }
}
