// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery

import com.azure.storage.blob.models.BlobRequestConditions
import com.azure.storage.blob.models.BlobStorageException
import com.azure.storage.blob.specialized.BlobClientBase
import com.azure.storage.common.ErrorReceiver
import com.azure.storage.common.ProgressReceiver
import com.azure.storage.common.implementation.Constants
import com.azure.storage.quickquery.models.BlobQuickQueryDelimitedSerialization
import com.azure.storage.quickquery.models.BlobQuickQueryError

import com.azure.storage.quickquery.models.BlobQuickQueryJsonSerialization
import spock.lang.Requires
import spock.lang.Unroll

class BlobQQInputStreamTest extends APISpec {

    // Generates and uploads a small 1KB CSV file
    def uploadCsv(BlobQuickQueryDelimitedSerialization s, int numCopies) {
        String header = String.join(new String(s.getColumnSeparator()), "rn1", "rn2", "rn3", "rn4")
            .concat(new String(s.getRecordSeparator()))
        byte[] headers = header.getBytes()

        String csv = String.join(new String(s.getColumnSeparator()), "100", "200", "300", "400")
            .concat(new String(s.getRecordSeparator()))
            .concat(String.join(new String(s.getColumnSeparator()), "300", "400", "500", "600")
                .concat(new String(s.getRecordSeparator())))

        byte[] csvData = csv.getBytes()

        int headerLength = s.isHeadersPresent() ? headers.length : 0
        byte[] data = new byte[headerLength + csvData.length * numCopies]
        if (s.isHeadersPresent()) {
            System.arraycopy(headers, 0, data, 0, headers.length)
        }

        for (int i = 0; i < numCopies; i++) {
            int o = i * csvData.length + headerLength;
            System.arraycopy(csvData, 0, data, o, csvData.length)
        }

        InputStream inputStream = new ByteArrayInputStream(data)

        bc.upload(inputStream, data.length, true)

    }

    def uploadSmallJson(BlobQuickQueryJsonSerialization serialization) {
        byte[] data = ('{\n' +
            '\t"name": "owner"\n' +
            '}').getBytes()

        InputStream inputStream = new ByteArrayInputStream(data)

        bc.upload(inputStream, data.length, true)
    }

    byte[] readFromInputStream(InputStream stream, int numBytesToRead) {
        byte[] queryData = new byte[numBytesToRead]

        def totalRead = 0
        def bytesRead = 0
        def length = numBytesToRead

        while (bytesRead != -1 && totalRead < numBytesToRead) {
            bytesRead = stream.read(queryData, totalRead, length)
            if (bytesRead != -1) {
                totalRead += bytesRead
                length -= bytesRead
            }
        }

        stream.close()
        return queryData
    }

    BlobQuickQueryClient qqClient
    BlobQuickQueryAsyncClient qqAsyncClient

    def setup() {
        qqClient = new BlobQuickQueryClientBuilder(bc).buildClient()
        qqAsyncClient = new BlobQuickQueryClientBuilder(bcAsync).buildAsyncClient()
    }

    def "Min"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, 32)

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage")

        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        for (int j = 0; j < downloadedData.length; j++) {
            assert queryData[j] == downloadedData[j]
        }
    }

    def "Snapshot"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, 32)

        // Create snapshot of blob
        BlobClientBase blobClient = bc.createSnapshot()
        qqClient = new BlobQuickQueryClientBuilder(blobClient).buildClient()

        // Overwrite blob to be empty.
        bc.upload(new ByteArrayInputStream(new byte[0]), 0, true)

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        blobClient.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage")

        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        for (int j = 0; j < downloadedData.length; j++) {
            assert queryData[j] == downloadedData[j]
        }
    }

    @Unroll
    def "Different sizes"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, numCopies)

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage")

        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        for (int j = 0; j < downloadedData.length; j++) {
            assert queryData[j] == downloadedData[j]
        }

        // To calculate the size of data being tested = numCopies * 32 bytes
        where:
        numCopies | _
        1         | _ // 32 bytes
        32        | _ // 1 KB
        256       | _ // 8 KB
        400       | _ // 12 ish KB
        4000      | _ // 125 KB
    }

    @Requires( { liveMode() } )
    def "Stream can handle very large amounts of data"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, 20480) // 20 MB of data.

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage")

        int read = 0
        while (read != -1) {
            read = qqStream.read()
        }

        then:
        notThrown(IOException)
    }


    @Unroll
    def "Input delimited"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(true)
        uploadCsv(ser, 32)

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage", ser, ser, null, null, null)

        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        def beginIndex = 16
        for (int j = beginIndex; j < downloadedData.length; j++) {
            assert queryData[j - beginIndex] == downloadedData[j]
        }
    }

    @Unroll
    def "Input json"() {
        setup:
        BlobQuickQueryJsonSerialization ser = new BlobQuickQueryJsonSerialization()
            .setRecordSeparator(recordSeparator as char)
        uploadSmallJson(ser)

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage", ser, ser, null, null, null)

        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        for (int j = 0; j < downloadedData.length; j++) {
            assert queryData[j] == downloadedData[j]
        }

        //TODO : ADD MORE DATA SIZES
        where:
        numBytes | recordSeparator || _
        0      | '\n'              || _
        1      | '\n'              || _
    }

    def "Non fatal error"() {
        setup:
        BlobQuickQueryDelimitedSerialization base = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)

        uploadCsv(base.setColumnSeparator('.' as char), 32)

        MockErrorReceiver receiver = new MockErrorReceiver("InvalidColumnOrdinal")

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT _1 from BlobStorage WHERE _2 > 250",
            base.setColumnSeparator(',' as char), base.setColumnSeparator(',' as char), null,
            receiver, null)


        readFromInputStream(qqStream, Constants.KB)

        then:
        receiver.numErrors > 0
        notThrown(BlobStorageException)
    }

    def "Fatal error"() {
        setup:
        BlobQuickQueryDelimitedSerialization base = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(true)

        uploadCsv(base.setColumnSeparator('.' as char), 32)

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage",
            new BlobQuickQueryJsonSerialization(), null, null, null, null)


        readFromInputStream(qqStream, Constants.KB)

        then:
        thrown(IOException)
    }

    def "Progress receiver"() {
        setup:
        BlobQuickQueryDelimitedSerialization base = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)

        uploadCsv(base.setColumnSeparator('.' as char), 32)

        def mockReceiver = new MockProgressReceiver()
        def sizeofBlobToRead = bc.getProperties().getBlobSize()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage",
            null, null, null, null, mockReceiver)

        /* The Avro stream has the following pattern
           n * (data record -> progress record) -> end record */
        // 1KB of data will only come back as a single data record.
        /* Pretend to read more data because the input stream will not parse records following the data record if it
         doesn't need to. */
        readFromInputStream(qqStream, Constants.MB)

        then:
        // At least the size of blob to read will be in the progress list
        mockReceiver.progressList.contains(sizeofBlobToRead)
    }

    @Requires( { liveMode() } ) // Large amount of data.
    def "Multiple records with progress receiver"() {
        setup:
        BlobQuickQueryDelimitedSerialization ser = new BlobQuickQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, 512000)

        def mockReceiver = new MockProgressReceiver()

        when:
        InputStream qqStream = qqClient.openInputStream("SELECT * from BlobStorage",
            null, null, null, null, mockReceiver)

        /* The Avro stream has the following pattern
           n * (data record -> progress record) -> end record */
        // 1KB of data will only come back as a single data record.
        /* Pretend to read more data because the input stream will not parse records following the data record if it
         doesn't need to. */
        readFromInputStream(qqStream, 16 * Constants.MB)

        then:
        long temp = 0
        // Make sure theyre all increasingly bigger
        for (long progress : mockReceiver.progressList) {
            assert progress >= temp
            temp = progress
        }
    }

    class MockProgressReceiver implements ProgressReceiver {

        List<Long> progressList

        MockProgressReceiver() {
            this.progressList = new ArrayList<>()
        }

        @Override
        void reportProgress(long bytesRead) {
            progressList.add(bytesRead)
        }
    }

    class MockErrorReceiver implements ErrorReceiver<BlobQuickQueryError> {

        String expectedType
        int numErrors

        MockErrorReceiver(String expectedType) {
            this.expectedType = expectedType
            this.numErrors = 0
        }

        @Override
        void reportError(BlobQuickQueryError nonFatalError) {
            assert !nonFatalError.isFatal()
            assert nonFatalError.getName() == expectedType
            numErrors++
        }
    }

    @Unroll
    def "Query input output IA"() {
        when:
        InputStream stream = qqClient.openInputStream("SELECT * from BlobStorage", input, output, null, null, null)

        stream.read()

        stream.close()

        then:
        thrown(IOException)

        where:
        input                                                    | output                                                   || _
        new MockSerialization().setRecordSeparator('\n' as char) | null                                                     || _
        null                                                     | new MockSerialization().setRecordSeparator('\n' as char) || _
    }

    @Unroll
    def "Query AC"() {
        setup:
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        InputStream stream = qqClient.openInputStream("SELECT * from BlobStorage", null, null, bac, null, null)

        stream.read()

        stream.close()

        then:
        notThrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }

    @Unroll
    def "Query AC fail"() {
        setup:
        setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)

        when:
        InputStream stream = qqClient.openInputStream("SELECT * from BlobStorage", null, null, bac, null, null)

        stream.read()

        stream.close()

        then:
        thrown(IOException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID
        newDate  | null       | null        | null         | null
        null     | oldDate    | null        | null         | null
        null     | null       | garbageEtag | null         | null
        null     | null       | null        | receivedEtag | null
        null     | null       | null        | null         | garbageLeaseID
    }

    // TODO : Query CPK test, Query encryption scope test
}
