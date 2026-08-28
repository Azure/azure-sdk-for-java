// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.ai.translation.document.models.BatchOptions;
import com.azure.ai.translation.document.models.DocumentStatusResult;
import com.azure.ai.translation.document.models.DocumentTranslationInput;
import com.azure.ai.translation.document.models.TranslationBatch;
import com.azure.ai.translation.document.models.TranslationSource;
import com.azure.ai.translation.document.models.TranslationTarget;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Unit tests validating serialization of the image translation and custom deployment name features
 * added in the 2026-03-01 service version. These do not require a live service.
 */
public class DocumentTranslationModelTests {

    @Test
    public void translationTargetSerializesDeploymentName() throws IOException {
        TranslationTarget target = new TranslationTarget("https://myblob.blob.core.windows.net/target", "fr")
            .setDeploymentName("myDeployment");

        String json = toJson(target);

        Assertions.assertTrue(json.contains("\"deploymentName\":\"myDeployment\""),
            "Expected serialized TranslationTarget to contain the deploymentName property. Actual: " + json);
    }

    @Test
    public void translationBatchSerializesTranslateTextWithinImage() throws IOException {
        TranslationSource source = new TranslationSource("https://myblob.blob.core.windows.net/source");
        TranslationTarget target = new TranslationTarget("https://myblob.blob.core.windows.net/target", "fr");
        DocumentTranslationInput input = new DocumentTranslationInput(source, new ArrayList<>(Arrays.asList(target)));

        TranslationBatch batch = new TranslationBatch(new ArrayList<>(Arrays.asList(input)))
            .setOptions(new BatchOptions().setTranslateTextWithinImage(true));

        String json = toJson(batch);

        Assertions.assertTrue(json.contains("\"translateTextWithinImage\":true"),
            "Expected serialized TranslationBatch to contain translateTextWithinImage. Actual: " + json);
    }

    @Test
    public void documentStatusResultDeserializesDeploymentNameAndImageUsage() throws IOException {
        String json = "{" + "\"path\":\"https://target/doc.txt\"," + "\"sourcePath\":\"https://source/doc.txt\","
            + "\"createdDateTimeUtc\":\"2026-03-01T00:00:00.0000000Z\","
            + "\"lastActionDateTimeUtc\":\"2026-03-01T00:05:00.0000000Z\"," + "\"status\":\"Succeeded\","
            + "\"to\":\"es\"," + "\"progress\":1.0," + "\"id\":\"doc-1\"," + "\"characterCharged\":100,"
            + "\"totalImageScansSucceeded\":6," + "\"totalImageScansFailed\":1," + "\"imageCharged\":3,"
            + "\"imageCharacterDetected\":1257," + "\"deploymentName\":\"myDeployment\"" + "}";

        DocumentStatusResult result;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            result = DocumentStatusResult.fromJson(reader);
        }

        Assertions.assertEquals("myDeployment", result.getDeploymentName());
        Assertions.assertEquals(6, result.getTotalImageScansSucceededCount());
        Assertions.assertEquals(1, result.getTotalImageScansFailedCount());
        Assertions.assertEquals(3, result.getImageChargedCount());
        Assertions.assertEquals(1257, result.getImageCharacterDetectedCount());
    }

    private static String toJson(com.azure.json.JsonSerializable<?> serializable) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {
            serializable.toJson(writer);
            writer.flush();
            return stream.toString(StandardCharsets.UTF_8.name());
        }
    }
}
