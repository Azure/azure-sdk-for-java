package com.azure.storage.file.datalake

import com.azure.storage.blob.models.BlobType
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.file.datalake.models.DataLakeFileOutputStreamResult

class FileOutputStreamTest extends APISpec {
    DataLakeFileClient fc

    def setup() {
        fc = fsc.getFileClient(generatePathName())
        fc.create()
    }

    // Only run this test in live mode as BlobOutputStream dynamically assigns blocks
    @LiveOnly
    def "Upload download"() {
        when:
        int length = 6 * Constants.MB
        byte[] randomBytes = getRandomByteArray(length)

        DataLakeFileOutputStreamResult streamResult = fc.getOutputStream()
        OutputStream outStream = streamResult.getOutputStream()
        outStream.write(randomBytes, 1 * Constants.MB, 5 * Constants.MB)
        outStream.close()

        then:
        def inputStream = fc.openInputStream().getInputStream()
        int b
        def outputStream = new ByteArrayOutputStream()
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b)
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex)
        }

        byte[] randomBytes2 = outputStream.toByteArray()
        assert randomBytes2 == Arrays.copyOfRange(randomBytes, 1 * Constants.MB, 6 * Constants.MB)
    }


}
