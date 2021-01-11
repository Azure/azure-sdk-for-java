// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.DecryptionContext;
import com.azure.cosmos.encryption.DecryptionInfo;
import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.encryption.TestCommon.TestDoc;
import com.azure.cosmos.encryption.CosmosEncryptionAlgorithm;
import com.azure.cosmos.encryption.EncryptionOptions;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class NewEncryptionProcessorTests {
    private static Encryptor mockEncryptor;
    private static EncryptionOptions encryptionOptions;
    private final static String dekId = "dekId";

    @BeforeClass(groups = "unit")
    public static void classInitilize() {
        NewEncryptionProcessorTests.encryptionOptions = new EncryptionOptions();
        encryptionOptions
            .setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED)
            .setDataEncryptionKeyId(dekId)
            .setPathsToEncrypt(TestCommon.TestDoc.PathsToEncrypt);

        NewEncryptionProcessorTests.mockEncryptor = Mockito.mock(Encryptor.class);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                byte[] plainText = invocationOnMock.getArgument(0, byte[].class);

                if (dekId == NewEncryptionProcessorTests.dekId) {
                    return Mono.just(TestCommon.EncryptData(plainText));
                } else {
                    throw new IllegalArgumentException("DEK not found.");
                }
            }
        }).when(NewEncryptionProcessorTests.mockEncryptor).encrypt(Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                byte[] plainText = invocationOnMock.getArgument(0, byte[].class);

                if (dekId == NewEncryptionProcessorTests.dekId) {
                    return Mono.just(TestCommon.DecryptData(plainText));
                } else {
                    throw new IllegalArgumentException("Null DEK was returned.");
                }
            }
        }).when(NewEncryptionProcessorTests.mockEncryptor).decrypt(Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test(enabled = false, groups = "unit")
    public void InvalidPathToEncrypt() {
        TestCommon.TestDoc testDoc = TestDoc.Create();

        EncryptionOptions encryptionOptionsWithInvalidPathToEncrypt = new EncryptionOptions();
        encryptionOptionsWithInvalidPathToEncrypt.setDataEncryptionKeyId(NewEncryptionProcessorTests.dekId)
            .setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED)
            .setPathsToEncrypt(ImmutableList.of("/SensitiveStr", "/Invalid"));

        try {
            EncryptionProcessor.encrypt(
                testDoc.ToStream(),
                NewEncryptionProcessorTests.mockEncryptor,
                encryptionOptionsWithInvalidPathToEncrypt);

            fail("Invalid path to encrypt didn't result in exception.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("PathsToEncrypt includes a path: '/Invalid' which was not found.");
        }
    }

//    TODO: moderakh add tests
//    public void EncryptDecryptPropertyWithNullValue() {
//    }

    @Test(groups = "unit")
    public void ValidateEncryptDecryptDocument() {
        TestCommon.TestDoc testDoc = TestCommon.TestDoc.Create();

        ObjectNode encryptedDoc = NewEncryptionProcessorTests.verifyEncryptionSucceeded(testDoc);
        Pair<ObjectNode, DecryptionContext> resultPair =
            EncryptionProcessor.decrypt(
            encryptedDoc,
            NewEncryptionProcessorTests.mockEncryptor).block();

        ObjectNode decryptedDoc = resultPair.getKey();
        DecryptionContext decryptionContext = resultPair.getRight();

        NewEncryptionProcessorTests.verifyDecryptionSucceeded(
            decryptedDoc,
            testDoc,
            decryptionContext);
    }

//    TODO: moderakh add the test
//    public void validateDecryptStream() {
//    }

    private static ObjectNode verifyEncryptionSucceeded(TestCommon.TestDoc testDoc) {
        byte[] encryptedStream = EncryptionProcessor.encrypt(
            testDoc.ToStream(),
            NewEncryptionProcessorTests.mockEncryptor,
            NewEncryptionProcessorTests.encryptionOptions).block();

//    ObjectNode encryptedDoc = NewEncryptionProcessor.BaseSerializer.FromStream<JObject>(encryptedStream);
        ObjectNode encryptedDoc = Utils.parse(encryptedStream, ObjectNode.class);

        assertThat(encryptedDoc.get("id").asText()).isEqualTo(testDoc.Id);
        assertThat(encryptedDoc.get("PK").asText()).isEqualTo(testDoc.PK);
        assertThat(encryptedDoc.get("NonSensitive").asText()).isEqualTo(testDoc.NonSensitive);
        assertThat(encryptedDoc.get("SensitiveStr")).isNull();
        ;
        assertThat(encryptedDoc.get("SensitiveInt")).isNull();
        ;


        JsonNode eiJProp = encryptedDoc.get(Constants.ENCRYPTION_INFO);
        assertThat(eiJProp).isNotNull();
        assertThat(eiJProp.asText()).isNotNull();

        assertThat(eiJProp.getNodeType()).isEqualTo(JsonNodeType.OBJECT);

//    EncryptionProperties encryptionProperties = ((JObject)eiJProp.Value).ToObject<EncryptionProperties>();
//
//    Assert.IsNotNull(encryptionProperties);
//    Assert.AreEqual(NewEncryptionProcessorTests.dekId, encryptionProperties.DataEncryptionKeyId);
//    Assert.AreEqual(2, encryptionProperties.EncryptionFormatVersion);
//    Assert.IsNotNull(encryptionProperties.EncryptedData);

        return encryptedDoc;
    }

    private static void verifyDecryptionSucceeded(
        ObjectNode decryptedDoc,
        TestCommon.TestDoc expectedDoc,
        DecryptionContext decryptionContext) {

        assertThat(decryptedDoc.get("SensitiveStr").textValue()).isEqualTo(expectedDoc.SensitiveStr);
        assertThat(decryptedDoc.get("SensitiveInt").intValue()).isEqualTo(expectedDoc.SensitiveInt);
        assertThat(decryptedDoc.get(Constants.ENCRYPTION_ALGORITHM)).isNull();

        assertThat(decryptionContext).isNotNull();
        assertThat(decryptionContext.getDecryptionInfoList()).isNotNull();
        DecryptionInfo decryptionInfo = decryptionContext.getDecryptionInfoList().get(0);
        assertThat(decryptionInfo.getDataEncryptionKeyId()).isEqualTo(NewEncryptionProcessorTests.dekId);
        assertThat(decryptionInfo.getPathsDecrypted()).hasSize(TestDoc.PathsToEncrypt.size());

        assertThat(EncryptionTests.TestDoc.PathsToEncrypt.stream().filter(path -> decryptionInfo.getPathsDecrypted().contains(path)).findAny()).isNotPresent();
    }
}
