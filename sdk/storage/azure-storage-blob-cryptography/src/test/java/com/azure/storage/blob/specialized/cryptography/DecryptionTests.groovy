/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.storage.blob.specialized.cryptography

import com.azure.storage.blob.models.BlobRange
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Requires
import spock.lang.Unroll

class DecryptionTests extends APISpec {
    String keyId
    def fakeKey
    BlobDecryptionPolicy blobDecryptionPolicy
    String blobName

    def setup() {
        keyId = "keyId"
        fakeKey = new FakeKey(keyId, getRandomByteArray(256))

        blobDecryptionPolicy = new BlobDecryptionPolicy(fakeKey, null, false)

        blobName = generateBlobName()
    }

    @Unroll
    def "Decryption"() {
        setup:
        def flow = new EncryptedFlux(testCase, fakeKey, this)

        def encryptionDataString = new ObjectMapper().writeValueAsString(flow.getEncryptionData())
        def desiredOutput = flow.getPlainText().position(EncryptedFlux.DATA_OFFSET)
            .limit(EncryptedFlux.DATA_OFFSET + EncryptedFlux.DATA_COUNT)

        /*
        This BlobRange will result in an EncryptedBlobRange of 0-64. This will allow us ample room to setup ByteBuffers
        with start/end in the locations described in the docs for EncryptedFlux. The validity of variable
        range downloads is tested in EncryptedBlobAPITest, so we are ok to use constants here; here we are only testing
        how the counting and data trimming logic works.
         */
        def blobRange = new BlobRange(EncryptedFlux.DATA_OFFSET, EncryptedFlux.DATA_COUNT)

        when:
        def decryptedData = collectBytesInBuffer(
            blobDecryptionPolicy.decryptBlob(encryptionDataString, flow, new EncryptedBlobRange(blobRange), true))
            .block()

        then:
        decryptedData == desiredOutput

        where:
        testCase                      | _
        EncryptedFlux.CASE_ZERO       | _
        EncryptedFlux.CASE_ONE        | _
        EncryptedFlux.CASE_TWO        | _
        EncryptedFlux.CASE_THREE      | _
        EncryptedFlux.CASE_FOUR       | _
        EncryptedFlux.CASE_FIVE       | _
        EncryptedFlux.CASE_SIX        | _
        EncryptedFlux.CASE_SEVEN      | _
        EncryptedFlux.CASE_EIGHT      | _
        EncryptedFlux.CASE_NINE       | _
        EncryptedFlux.CASE_TEN        | _
        EncryptedFlux.CASE_ELEVEN     | _
        EncryptedFlux.CASE_TWELVE     | _
        EncryptedFlux.CASE_THIRTEEN   | _
        EncryptedFlux.CASE_FOURTEEN   | _
        EncryptedFlux.CASE_FIFTEEN    | _
        EncryptedFlux.CASE_SIXTEEN    | _
        EncryptedFlux.CASE_SEVENTEEN  | _
        EncryptedFlux.CASE_EIGHTEEN   | _
        EncryptedFlux.CASE_NINETEEN   | _
        EncryptedFlux.CASE_TWENTY     | _
        EncryptedFlux.CASE_TWENTY_ONE | _
        EncryptedFlux.CASE_TWENTY_TWO | _
    }
}
