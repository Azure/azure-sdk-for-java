// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.Encryptor;
import com.azure.cosmos.implementation.Utils;
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

    @BeforeClass
    public static void ClassInitilize() {
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
        }).when(NewEncryptionProcessorTests.mockEncryptor).encryptAsync(Mockito.any(), Mockito.any(), Mockito.any());

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
        }).when(NewEncryptionProcessorTests.mockEncryptor).decryptAsync(Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test(enabled = false)
    public void InvalidPathToEncrypt() {
        TestCommon.TestDoc testDoc = TestDoc.Create();

        EncryptionOptions encryptionOptionsWithInvalidPathToEncrypt = new EncryptionOptions();
        encryptionOptionsWithInvalidPathToEncrypt.setDataEncryptionKeyId(NewEncryptionProcessorTests.dekId)
            .setEncryptionAlgorithm(CosmosEncryptionAlgorithm.AEAES_256_CBC_HMAC_SHA_256_RANDOMIZED)
            .setPathsToEncrypt(ImmutableList.of("/SensitiveStr", "/Invalid"));

        try {
            EncryptionProcessor.encryptAsync(
                testDoc.ToStream(),
                NewEncryptionProcessorTests.mockEncryptor,
                encryptionOptionsWithInvalidPathToEncrypt);

            fail("Invalid path to encrypt didn't result in exception.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("PathsToEncrypt includes a path: '/Invalid' which was not found.");
        }
    }

//        [TestMethod]
//    public async Task EncryptDecryptPropertyWithNullValue()
//{
//    TestDoc testDoc = TestDoc.Create();
//    testDoc.SensitiveStr = null;
//
//    JObject encryptedDoc = await NewEncryptionProcessorTests.VerifyEncryptionSucceeded(testDoc);
//
//    JObject decryptedDoc = await EncryptionProcessor.DecryptAsync(
//    encryptedDoc,
//    NewEncryptionProcessorTests.mockEncryptor.Object,
//    new CosmosDiagnosticsContext(),
//    CancellationToken.None);
//
//    NewEncryptionProcessorTests.VerifyDecryptionSucceeded(
//        decryptedDoc,
//        testDoc);
//}

    @Test
    public void ValidateEncryptDecryptDocument() {
        TestCommon.TestDoc testDoc = TestCommon.TestDoc.Create();

        ObjectNode encryptedDoc = NewEncryptionProcessorTests.VerifyEncryptionSucceeded(testDoc);
        ObjectNode decryptedDoc = EncryptionProcessor.decryptAsync(
            encryptedDoc,
            NewEncryptionProcessorTests.mockEncryptor).block();

        NewEncryptionProcessorTests.VerifyDecryptionSucceeded(
            decryptedDoc,
            testDoc);
    }

    //
//        [TestMethod]
//    public async Task ValidateDecryptStream()
//{
//    TestDoc testDoc = TestDoc.Create();
//
//    Stream encryptedStream = await EncryptionProcessor.EncryptAsync(
//    testDoc.ToStream(),
//    NewEncryptionProcessorTests.mockEncryptor.Object,
//    NewEncryptionProcessorTests.encryptionOptions,
//    new CosmosDiagnosticsContext(),
//    CancellationToken.None);
//
//    Stream decryptedStream = await EncryptionProcessor.DecryptAsync(
//    encryptedStream,
//    NewEncryptionProcessorTests.mockEncryptor.Object,
//    new CosmosDiagnosticsContext(),
//    CancellationToken.None);
//
//    JObject decryptedDoc = EncryptionProcessor.BaseSerializer.FromStream<JObject>(decryptedStream);
//    NewEncryptionProcessorTests.VerifyDecryptionSucceeded(
//        decryptedDoc,
//        testDoc);
//}
//
//        [TestMethod]
//    public async Task DecryptStreamWithoutEncryptedProperty()
//{
//    TestDoc testDoc = TestDoc.Create();
//    Stream docStream = testDoc.ToStream();
//
//    Stream decryptedStream = await EncryptionProcessor.DecryptAsync(
//    docStream,
//    NewEncryptionProcessorTests.mockEncryptor.Object,
//    new CosmosDiagnosticsContext(),
//    CancellationToken.None);
//
//    Assert.IsTrue(decryptedStream.CanSeek);
//    Assert.AreEqual(0, decryptedStream.Position);
//    Assert.AreEqual(docStream.Length, decryptedStream.Length);
//}
//
    private static ObjectNode VerifyEncryptionSucceeded(TestCommon.TestDoc testDoc) {
        byte[] encryptedStream = EncryptionProcessor.encryptAsync(
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

    private static void VerifyDecryptionSucceeded(
        ObjectNode decryptedDoc,
        TestCommon.TestDoc expectedDoc) {

        assertThat(decryptedDoc.get("SensitiveStr").textValue()).isEqualTo(expectedDoc.SensitiveStr);
        assertThat(decryptedDoc.get("SensitiveInt").intValue()).isEqualTo(expectedDoc.SensitiveInt);
        assertThat(decryptedDoc.get(Constants.ENCRYPTION_ALGORITHM)).isNull();
    }
}
