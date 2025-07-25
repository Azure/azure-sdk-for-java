// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.storageactions.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.storageactions.models.ElseCondition;
import com.azure.resourcemanager.storageactions.models.IfCondition;
import com.azure.resourcemanager.storageactions.models.OnFailure;
import com.azure.resourcemanager.storageactions.models.OnSuccess;
import com.azure.resourcemanager.storageactions.models.StorageTaskAction;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperation;
import com.azure.resourcemanager.storageactions.models.StorageTaskOperationName;
import com.azure.resourcemanager.storageactions.models.StorageTaskProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class StorageTaskPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        StorageTaskProperties model = BinaryData.fromString(
            "{\"taskVersion\":4659788042682990061,\"enabled\":true,\"description\":\"fkgiawxk\",\"action\":{\"if\":{\"condition\":\"ryplwckbasyypn\",\"operations\":[{\"name\":\"UndeleteBlob\",\"parameters\":{\"ulzndlikwyqk\":\"cbacphejkotynqg\",\"rxybz\":\"fgibmadgakeq\",\"mnkzsmod\":\"qedqytbciqfoufl\"},\"onSuccess\":\"continue\",\"onFailure\":\"break\"},{\"name\":\"UndeleteBlob\",\"parameters\":{\"cuertu\":\"kwtmutduqktapspw\"},\"onSuccess\":\"continue\",\"onFailure\":\"break\"}]},\"else\":{\"operations\":[{\"name\":\"SetBlobExpiry\",\"parameters\":{\"mbmbexppbh\":\"mdgbbjfdd\"},\"onSuccess\":\"continue\",\"onFailure\":\"break\"}]}},\"provisioningState\":\"Failed\",\"creationTimeInUtc\":\"2021-08-14T01:11:33Z\"}")
            .toObject(StorageTaskProperties.class);
        Assertions.assertTrue(model.enabled());
        Assertions.assertEquals("fkgiawxk", model.description());
        Assertions.assertEquals("ryplwckbasyypn", model.action().ifProperty().condition());
        Assertions.assertEquals(StorageTaskOperationName.UNDELETE_BLOB,
            model.action().ifProperty().operations().get(0).name());
        Assertions.assertEquals("cbacphejkotynqg",
            model.action().ifProperty().operations().get(0).parameters().get("ulzndlikwyqk"));
        Assertions.assertEquals(OnSuccess.CONTINUE, model.action().ifProperty().operations().get(0).onSuccess());
        Assertions.assertEquals(OnFailure.BREAK, model.action().ifProperty().operations().get(0).onFailure());
        Assertions.assertEquals(StorageTaskOperationName.SET_BLOB_EXPIRY,
            model.action().elseProperty().operations().get(0).name());
        Assertions.assertEquals("mdgbbjfdd",
            model.action().elseProperty().operations().get(0).parameters().get("mbmbexppbh"));
        Assertions.assertEquals(OnSuccess.CONTINUE, model.action().elseProperty().operations().get(0).onSuccess());
        Assertions.assertEquals(OnFailure.BREAK, model.action().elseProperty().operations().get(0).onFailure());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        StorageTaskProperties model = new StorageTaskProperties().withEnabled(true)
            .withDescription("fkgiawxk")
            .withAction(new StorageTaskAction()
                .withIfProperty(new IfCondition().withCondition("ryplwckbasyypn")
                    .withOperations(Arrays.asList(
                        new StorageTaskOperation().withName(StorageTaskOperationName.UNDELETE_BLOB)
                            .withParameters(mapOf("ulzndlikwyqk", "cbacphejkotynqg", "rxybz", "fgibmadgakeq",
                                "mnkzsmod", "qedqytbciqfoufl"))
                            .withOnSuccess(OnSuccess.CONTINUE)
                            .withOnFailure(OnFailure.BREAK),
                        new StorageTaskOperation().withName(StorageTaskOperationName.UNDELETE_BLOB)
                            .withParameters(mapOf("cuertu", "kwtmutduqktapspw"))
                            .withOnSuccess(OnSuccess.CONTINUE)
                            .withOnFailure(OnFailure.BREAK))))
                .withElseProperty(new ElseCondition().withOperations(
                    Arrays.asList(new StorageTaskOperation().withName(StorageTaskOperationName.SET_BLOB_EXPIRY)
                        .withParameters(mapOf("mbmbexppbh", "mdgbbjfdd"))
                        .withOnSuccess(OnSuccess.CONTINUE)
                        .withOnFailure(OnFailure.BREAK)))));
        model = BinaryData.fromObject(model).toObject(StorageTaskProperties.class);
        Assertions.assertTrue(model.enabled());
        Assertions.assertEquals("fkgiawxk", model.description());
        Assertions.assertEquals("ryplwckbasyypn", model.action().ifProperty().condition());
        Assertions.assertEquals(StorageTaskOperationName.UNDELETE_BLOB,
            model.action().ifProperty().operations().get(0).name());
        Assertions.assertEquals("cbacphejkotynqg",
            model.action().ifProperty().operations().get(0).parameters().get("ulzndlikwyqk"));
        Assertions.assertEquals(OnSuccess.CONTINUE, model.action().ifProperty().operations().get(0).onSuccess());
        Assertions.assertEquals(OnFailure.BREAK, model.action().ifProperty().operations().get(0).onFailure());
        Assertions.assertEquals(StorageTaskOperationName.SET_BLOB_EXPIRY,
            model.action().elseProperty().operations().get(0).name());
        Assertions.assertEquals("mdgbbjfdd",
            model.action().elseProperty().operations().get(0).parameters().get("mbmbexppbh"));
        Assertions.assertEquals(OnSuccess.CONTINUE, model.action().elseProperty().operations().get(0).onSuccess());
        Assertions.assertEquals(OnFailure.BREAK, model.action().elseProperty().operations().get(0).onFailure());
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
