// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.dataprotection.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.dataprotection.models.AzureBackupDiscreteRecoveryPoint;
import com.azure.resourcemanager.dataprotection.models.RecoveryPointCompletionState;
import com.azure.resourcemanager.dataprotection.models.RecoveryPointDataStoreDetails;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class AzureBackupDiscreteRecoveryPointTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureBackupDiscreteRecoveryPoint model = BinaryData.fromString(
            "{\"objectType\":\"AzureBackupDiscreteRecoveryPoint\",\"friendlyName\":\"ldsyuuximerqfob\",\"recoveryPointDataStoresDetails\":[{\"creationTime\":\"2021-04-12T06:30:25Z\",\"expiryTime\":\"2021-10-07T08:54:51Z\",\"id\":\"utwpfhp\",\"metaData\":\"m\",\"state\":\"skdsnfdsdoakg\",\"type\":\"lmkk\",\"visible\":true,\"rehydrationExpiryTime\":\"2020-12-20T17:30:39Z\",\"rehydrationStatus\":\"DELETE_IN_PROGRESS\"},{\"creationTime\":\"2021-05-16T03:51:54Z\",\"expiryTime\":\"2021-07-06T21:17:30Z\",\"id\":\"sttwvogvbbe\",\"metaData\":\"cngqqmoakufgmjz\",\"state\":\"rdgrtw\",\"type\":\"nuuzkopbm\",\"visible\":false,\"rehydrationExpiryTime\":\"2021-08-21T07:56:19Z\",\"rehydrationStatus\":\"CREATE_IN_PROGRESS\"},{\"creationTime\":\"2021-07-25T01:07:20Z\",\"expiryTime\":\"2021-12-03T00:28:31Z\",\"id\":\"iuiefozbhdmsm\",\"metaData\":\"zqhof\",\"state\":\"maequiahxicslfa\",\"type\":\"z\",\"visible\":true,\"rehydrationExpiryTime\":\"2021-07-09T22:00:16Z\",\"rehydrationStatus\":\"CREATE_IN_PROGRESS\"},{\"creationTime\":\"2021-10-10T06:29:17Z\",\"expiryTime\":\"2021-01-11T10:09:18Z\",\"id\":\"ccsphkaivwi\",\"metaData\":\"scywuggwoluhc\",\"state\":\"wem\",\"type\":\"i\",\"visible\":false,\"rehydrationExpiryTime\":\"2021-01-14T04:14:03Z\",\"rehydrationStatus\":\"FAILED\"}],\"recoveryPointTime\":\"2021-02-08T08:17:57Z\",\"policyName\":\"swe\",\"policyVersion\":\"qwdxggicc\",\"recoveryPointId\":\"xqhuexm\",\"recoveryPointType\":\"tlstvlzywem\",\"retentionTagName\":\"rncsdtclu\",\"retentionTagVersion\":\"ypbsfgytguslfead\",\"expiryTime\":\"2021-10-11T19:56:34Z\",\"recoveryPointState\":\"Partial\"}")
            .toObject(AzureBackupDiscreteRecoveryPoint.class);
        Assertions.assertEquals("ldsyuuximerqfob", model.friendlyName());
        Assertions.assertEquals(OffsetDateTime.parse("2021-04-12T06:30:25Z"),
            model.recoveryPointDataStoresDetails().get(0).creationTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-10-07T08:54:51Z"),
            model.recoveryPointDataStoresDetails().get(0).expiryTime());
        Assertions.assertEquals("utwpfhp", model.recoveryPointDataStoresDetails().get(0).id());
        Assertions.assertEquals("m", model.recoveryPointDataStoresDetails().get(0).metadata());
        Assertions.assertEquals("skdsnfdsdoakg", model.recoveryPointDataStoresDetails().get(0).state());
        Assertions.assertEquals("lmkk", model.recoveryPointDataStoresDetails().get(0).type());
        Assertions.assertEquals(true, model.recoveryPointDataStoresDetails().get(0).visible());
        Assertions.assertEquals(OffsetDateTime.parse("2021-02-08T08:17:57Z"), model.recoveryPointTime());
        Assertions.assertEquals("swe", model.policyName());
        Assertions.assertEquals("qwdxggicc", model.policyVersion());
        Assertions.assertEquals("xqhuexm", model.recoveryPointId());
        Assertions.assertEquals("tlstvlzywem", model.recoveryPointType());
        Assertions.assertEquals("rncsdtclu", model.retentionTagName());
        Assertions.assertEquals("ypbsfgytguslfead", model.retentionTagVersion());
        Assertions.assertEquals(RecoveryPointCompletionState.PARTIAL, model.recoveryPointState());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureBackupDiscreteRecoveryPoint model
            = new AzureBackupDiscreteRecoveryPoint().withFriendlyName("ldsyuuximerqfob")
                .withRecoveryPointDataStoresDetails(Arrays.asList(
                    new RecoveryPointDataStoreDetails().withCreationTime(OffsetDateTime.parse("2021-04-12T06:30:25Z"))
                        .withExpiryTime(OffsetDateTime.parse("2021-10-07T08:54:51Z"))
                        .withId("utwpfhp")
                        .withMetadata("m")
                        .withState("skdsnfdsdoakg")
                        .withType("lmkk")
                        .withVisible(true),
                    new RecoveryPointDataStoreDetails().withCreationTime(OffsetDateTime.parse("2021-05-16T03:51:54Z"))
                        .withExpiryTime(OffsetDateTime.parse("2021-07-06T21:17:30Z"))
                        .withId("sttwvogvbbe")
                        .withMetadata("cngqqmoakufgmjz")
                        .withState("rdgrtw")
                        .withType("nuuzkopbm")
                        .withVisible(false),
                    new RecoveryPointDataStoreDetails().withCreationTime(OffsetDateTime.parse("2021-07-25T01:07:20Z"))
                        .withExpiryTime(OffsetDateTime.parse("2021-12-03T00:28:31Z"))
                        .withId("iuiefozbhdmsm")
                        .withMetadata("zqhof")
                        .withState("maequiahxicslfa")
                        .withType("z")
                        .withVisible(true),
                    new RecoveryPointDataStoreDetails().withCreationTime(OffsetDateTime.parse("2021-10-10T06:29:17Z"))
                        .withExpiryTime(OffsetDateTime.parse("2021-01-11T10:09:18Z"))
                        .withId("ccsphkaivwi")
                        .withMetadata("scywuggwoluhc")
                        .withState("wem")
                        .withType("i")
                        .withVisible(false)))
                .withRecoveryPointTime(OffsetDateTime.parse("2021-02-08T08:17:57Z"))
                .withPolicyName("swe")
                .withPolicyVersion("qwdxggicc")
                .withRecoveryPointId("xqhuexm")
                .withRecoveryPointType("tlstvlzywem")
                .withRetentionTagName("rncsdtclu")
                .withRetentionTagVersion("ypbsfgytguslfead")
                .withRecoveryPointState(RecoveryPointCompletionState.PARTIAL);
        model = BinaryData.fromObject(model).toObject(AzureBackupDiscreteRecoveryPoint.class);
        Assertions.assertEquals("ldsyuuximerqfob", model.friendlyName());
        Assertions.assertEquals(OffsetDateTime.parse("2021-04-12T06:30:25Z"),
            model.recoveryPointDataStoresDetails().get(0).creationTime());
        Assertions.assertEquals(OffsetDateTime.parse("2021-10-07T08:54:51Z"),
            model.recoveryPointDataStoresDetails().get(0).expiryTime());
        Assertions.assertEquals("utwpfhp", model.recoveryPointDataStoresDetails().get(0).id());
        Assertions.assertEquals("m", model.recoveryPointDataStoresDetails().get(0).metadata());
        Assertions.assertEquals("skdsnfdsdoakg", model.recoveryPointDataStoresDetails().get(0).state());
        Assertions.assertEquals("lmkk", model.recoveryPointDataStoresDetails().get(0).type());
        Assertions.assertEquals(true, model.recoveryPointDataStoresDetails().get(0).visible());
        Assertions.assertEquals(OffsetDateTime.parse("2021-02-08T08:17:57Z"), model.recoveryPointTime());
        Assertions.assertEquals("swe", model.policyName());
        Assertions.assertEquals("qwdxggicc", model.policyVersion());
        Assertions.assertEquals("xqhuexm", model.recoveryPointId());
        Assertions.assertEquals("tlstvlzywem", model.recoveryPointType());
        Assertions.assertEquals("rncsdtclu", model.retentionTagName());
        Assertions.assertEquals("ypbsfgytguslfead", model.retentionTagVersion());
        Assertions.assertEquals(RecoveryPointCompletionState.PARTIAL, model.recoveryPointState());
    }
}
