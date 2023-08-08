// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.models.BlobRange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.stream.Stream;

import static com.azure.core.test.utils.TestUtils.assertByteBuffersEqual;

public class DecryptionTests extends BlobCryptographyTestBase {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String KEY_ID = "keyId";
    private FakeKey fakeKey;
    private BlobDecryptionPolicy blobDecryptionPolicy;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        fakeKey = new FakeKey(KEY_ID, getRandomByteArray(256));
        blobDecryptionPolicy = new BlobDecryptionPolicy(fakeKey, null, false);
    }

    @ParameterizedTest
    @MethodSource("decryptionSupplier")
    public void decryption(int testCase) throws InvalidKeyException, JsonProcessingException, MalformedURLException {
        EncryptedFlux flow = new EncryptedFlux(testCase, fakeKey, this);
        String encryptionDataString = MAPPER.writeValueAsString(flow.getEncryptionData());
        ByteBuffer desiredOutput = flow.getPlainText();
        desiredOutput.position(EncryptedFlux.DATA_OFFSET);
        desiredOutput.limit(EncryptedFlux.DATA_OFFSET + EncryptedFlux.DATA_COUNT);

        // This BlobRange will result in an EncryptedBlobRange of 0-64. This will allow us ample room to setup
        // ByteBuffers with start/end in the locations described in the docs for EncryptedFlux. The validity of variable
        //range downloads is tested in EncryptedBlobAPITest, so we are ok to use constants here; here we are only
        // testing how the counting and data trimming logic works.
        BlobRange blobRange = new BlobRange(EncryptedFlux.DATA_OFFSET, (long) EncryptedFlux.DATA_COUNT);
        EncryptionData encryptionData = EncryptionData.fromJsonString(encryptionDataString);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(blobDecryptionPolicy.decryptBlob(flow,
                new EncryptedBlobRange(blobRange, encryptionData), true, encryptionData,
                new URL("http://www.foo.com/path"))))
            .assertNext(bytes -> assertByteBuffersEqual(desiredOutput, ByteBuffer.wrap(bytes)))
            .verifyComplete();
    }

    private static Stream<Integer> decryptionSupplier() {
        return Stream.of(EncryptedFlux.CASE_ZERO, EncryptedFlux.CASE_ONE, EncryptedFlux.CASE_TWO,
            EncryptedFlux.CASE_THREE, EncryptedFlux.CASE_FOUR, EncryptedFlux.CASE_FIVE, EncryptedFlux.CASE_SIX,
            EncryptedFlux.CASE_SEVEN, EncryptedFlux.CASE_EIGHT, EncryptedFlux.CASE_NINE, EncryptedFlux.CASE_TEN,
            EncryptedFlux.CASE_ELEVEN, EncryptedFlux.CASE_TWELVE, EncryptedFlux.CASE_THIRTEEN,
            EncryptedFlux.CASE_FOURTEEN, EncryptedFlux.CASE_FIFTEEN, EncryptedFlux.CASE_SIXTEEN,
            EncryptedFlux.CASE_SEVENTEEN, EncryptedFlux.CASE_EIGHTEEN, EncryptedFlux.CASE_NINETEEN,
            EncryptedFlux.CASE_TWENTY, EncryptedFlux.CASE_TWENTY_ONE, EncryptedFlux.CASE_TWENTY_TWO);
    }
}
