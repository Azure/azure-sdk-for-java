// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share

import com.azure.storage.common.implementation.Constants
import spock.lang.Requires

class StorageFileInputOutputStreamTests extends APISpec {
    def fileClient
    int length

    def setup() {
        def shareName = testResourceName.randomName(methodName, 60)
        def filePath = testResourceName.randomName(methodName, 60)
        def shareClient = shareBuilderHelper(interceptorManager, shareName).buildClient()
        shareClient.create()
        fileClient = shareClient.getFileClient(filePath)
    }

    @Requires({ liveMode() })
    def "Upload download"() {
        when:
        length = 30 * Constants.MB
        fileClient.create(length)
        byte[] randomBytes = FileTestHelper.getRandomBuffer(length)

        StorageFileOutputStream outStream = fileClient.getFileOutputStream()
        outStream.write(randomBytes)
        outStream.close()

        then:
        StorageFileInputStream inputStream = fileClient.openInputStream()
        int b
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == randomBytes
    }


    @Requires({ liveMode() })
    def "Stream with offset"() {
        when:
        length = 7 * Constants.MB
        fileClient.create(length)
        byte[] randomBytes = FileTestHelper.getRandomBuffer(9 * Constants.MB)

        StorageFileOutputStream outStream = fileClient.getFileOutputStream()
        outStream.write(randomBytes, 2 * Constants.MB, length)
        outStream.close()

        then:
        StorageFileInputStream inputStream = fileClient.openInputStream()
        byte[] b = new byte[length]
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        try {
            if (inputStream.read(b) != -1) {
                outputStream.write(b, 0, b.length)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }
        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 2 * Constants.MB, 9 * Constants.MB)
    }
}
