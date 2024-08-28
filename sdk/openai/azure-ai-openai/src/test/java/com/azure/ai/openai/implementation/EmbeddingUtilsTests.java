// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.implementation.EmbeddingsUtils.addEncodingFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for EmbeddingItem.
 */
public class EmbeddingUtilsTests {

    @Test
    public void testEmbeddingItemConvertBase64ToFloatList() {
        // Arrange
        List<Float> expectedFloatList = Arrays.asList(-0.0069292835f, -0.005336422f, 0.011875892f, -0.025001878f, -0.024692714f, 0.039787933f,
                -0.010101565f, -0.009402588f, -0.013186474f, -0.009940263f, -0.011680985f, 0.007870215f, -0.014087079f,
                0.0077694007f, 0.010168774f, -0.00503398f, 0.022945272f, -0.0015122104f, 0.014933917f, -0.010262867f,
                0.0048625963f, 0.012460613f, 0.004909643f, 0.010807263f, -0.0066033183f, -3.5746972E-4f, 0.005578376f,
                -0.012494218f, 0.016372196f, 0.00452991f, 0.006562993f, -0.007070423f, -0.015216196f, -0.0065898765f,
                -0.018724523f, 0.0040796073f, 0.0032126068f, -0.01899336f, 0.030163554f, -0.007594656f, 0.008233145f,
                0.00942275f, -0.0010887914f, -4.0955696E-4f, -0.008831308f, -0.028631182f, 0.0030176996f, 0.009295053f,
                0.01654694f, 0.0064756204f, 0.019436942f, 0.014718847f, -0.025754621f, -0.009913378f, 3.3152518E7f);
        String embedding = "Cw/juyfdrrsak0I8vdDMvGBIyrys\u002BCI9CYElvFANGrwVDFi8fNwivJphP7wT8gA8fs1mvHWW/jvumiY8FvSku7n3uzxcNca6Y610PJYlKLxqVp87mSdMPBLhoDvyEDE8pmDYu8Zqu7nPyrY7jLRMvPwehjykb5Q7YA7XOwKv57taTXm85O/Xuyxkmbw6roU7mYpSO/eXm7yPGfc8mNz4u1DkBjzhYRo8x7WOus651rkxsRC88YvqvJnERTtHShg8c42HPHEx1Ds5Op88UidxPFv70ry5ayK8A\u002B/8S8";

        // Act
        List<Float> floats = EmbeddingsUtils.convertBase64ToFloatList(embedding);
        String convertFloatListToBase64 = EmbeddingsUtils.convertFloatListToBase64(expectedFloatList);
        List<Float> reconvertedBackToFloats = EmbeddingsUtils.convertBase64ToFloatList(convertFloatListToBase64);

        // Assert
        for (int i = 0; i < expectedFloatList.size(); i++) {
            assertEquals(expectedFloatList.get(i).floatValue(), floats.get(i).floatValue());
        }

        // Testing if the converted Based64 can be converted back to floats correctly, as the converted Base64 String
        // has a different suffix, '/8Sw', which is different from the original base64 string, '/8S8'.
        // Since decoding the string results in the same expected float list, the conversion is correct.
        // And it is not expected to compare those Base64 strings.
        for (int i = 0; i < expectedFloatList.size(); i++) {
            assertEquals(expectedFloatList.get(i).floatValue(), reconvertedBackToFloats.get(i).floatValue());
        }
    }

    @Test
    public void testBinaryDataHasNoEncodingFormat() {
        // Arrange
        String jsonWithoutEncodingFormat = "{\"input\":[\"Your text string goes here\"]}";
        String expectedJson = "{\"input\":[\"Your text string goes here\"],\"encoding_format\":\"base64\"}";
        // Act
        BinaryData binaryData = addEncodingFormat(BinaryData.fromString(jsonWithoutEncodingFormat));
        // Assert
        assertEquals(expectedJson, binaryData.toString());
    }

    @Test
    public void testBinaryDataHasEncodingFormat() {
        // Arrange
        String jsonWithEncodingFormat = "{\"input\":[\"Your text string goes here\"],\"encoding_format\":\"float\"}";
        // Act
        BinaryData binaryData = addEncodingFormat(BinaryData.fromString(jsonWithEncodingFormat));
        // Assert
        assertEquals(jsonWithEncodingFormat, binaryData.toString());
    }

    @Test
    public void testEmbeddingItemSerializationAndDeserialization() throws IOException {
        // Test EmbeddingItem deserialization
        // Arrange
        String embeddingItemJson = "{\"index\":1,\"embedding\":[0.014820415,-0.010995179,0.0050360323,-0.009382891]}";
        JsonReader reader = JsonProviders.createReader(embeddingItemJson);
        // Act
        EmbeddingItem embeddingItem = EmbeddingItem.fromJson(reader);
        // Assert
        List<Float> embedding = embeddingItem.getEmbedding();
        assertNotNull(embedding);
        assertEquals(4, embedding.size());
        assertEquals(0.014820415f, embedding.get(0));
        assertEquals(-0.010995179f, embedding.get(1));
        assertEquals(0.0050360323f, embedding.get(2));
        assertEquals(-0.009382891f, embedding.get(3));
        assertEquals(1, embeddingItem.getPromptIndex());

        // Test EmbeddingItem serialization
        // Use above embeddingItem to serialize it back to JSON
        // Arrange
        OutputStream outputStream = new ByteArrayOutputStream();
        // Act
        JsonWriter jsonWriter = embeddingItem.toJson(JsonProviders.createWriter(outputStream));
        jsonWriter.close();
        // Assert
        assertEquals(embeddingItemJson, outputStream.toString());
    }

    @Test
    public void testEmbeddingItemsInEmbeddings() throws IOException {
        // Test Embeddings deserialization
        // Arrange
        String embeddingsJson = "{\"data\":[{\"index\":0,\"embedding\":[0.014820415,-0.010995179,0.0050360323,-0.009382891]}],\"usage\":{\"prompt_tokens\":9,\"total_tokens\":9}}";
        JsonReader reader = JsonProviders.createReader(embeddingsJson);
        // Act
        Embeddings embeddings = Embeddings.fromJson(reader);
        // Assert
        List<EmbeddingItem> data = embeddings.getData();
        assertNotNull(data);
        assertEquals(1, data.size());
        EmbeddingItem embeddingItem = data.get(0);
        List<Float> embedding = embeddingItem.getEmbedding();
        assertNotNull(embedding);
        assertEquals(4, embedding.size());
        assertEquals(0.014820415f, embedding.get(0));
        assertEquals(-0.010995179f, embedding.get(1));
        assertEquals(0.0050360323f, embedding.get(2));
        assertEquals(-0.009382891f, embedding.get(3));
        assertEquals(0, embeddingItem.getPromptIndex());

        // Test Embeddings serialization
        // Use above embeddingItem to serialize it back to JSON
        // Arrange
        OutputStream outputStream = new ByteArrayOutputStream();
        // Act
        JsonWriter jsonWriter = embeddings.toJson(JsonProviders.createWriter(outputStream));
        jsonWriter.close();
        // Assert
        assertEquals(embeddingsJson, outputStream.toString());
    }
}
