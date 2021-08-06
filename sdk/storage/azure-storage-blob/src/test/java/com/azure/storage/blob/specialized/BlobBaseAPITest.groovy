package com.azure.storage.blob.specialized

import com.azure.core.test.TestMode
import com.azure.storage.blob.APISpec
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobServiceVersion
import com.azure.storage.blob.models.*
import com.azure.storage.blob.options.BlobQueryOptions
import com.azure.storage.common.implementation.Constants
import com.azure.storage.common.test.shared.extensions.LiveOnly
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion
import reactor.core.Exceptions
import spock.lang.Requires
import spock.lang.Retry
import spock.lang.Unroll
import spock.lang.Ignore

import java.util.function.Consumer

class BlobBaseAPITest extends APISpec {

    BlobClient bc
    String blobName

    def setup() {
        blobName = generateBlobName()
        bc = cc.getBlobClient(blobName)
        bc.upload(new ByteArrayInputStream(new byte[0]), 0)
    }

    /* Quick Query Tests. */

    // Generates and uploads a CSV file
    def uploadCsv(BlobQueryDelimitedSerialization s, int numCopies) {
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
            int o = i * csvData.length + headerLength
            System.arraycopy(csvData, 0, data, o, csvData.length)
        }

        InputStream inputStream = new ByteArrayInputStream(data)

        bc.upload(inputStream, data.length, true)
    }

    def uploadSmallJson(int numCopies) {
        StringBuilder b = new StringBuilder()
        b.append('{\n')
        for(int i = 0; i < numCopies; i++) {
            b.append(String.format('\t"name%d": "owner%d",\n', i, i))
        }
        b.append('}')

        InputStream inputStream = new ByteArrayInputStream(b.toString().getBytes())

        bc.upload(inputStream, b.length(), true)
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

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query min"() {
        setup:
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, numCopies)
        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStream(expression)
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        notThrown(IOException)
        queryData == downloadedData

        /* Output Stream. */
        when:
        OutputStream os = new ByteArrayOutputStream()
        bc.query(os, expression)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        osData == downloadedData

        // To calculate the size of data being tested = numCopies * 32 bytes
        where:
        numCopies | _
        1         | _ // 32 bytes
        32        | _ // 1 KB
        256       | _ // 8 KB
        400       | _ // 12 ish KB
        4000      | _ // 125 KB
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query csv serialization separator"() {
        setup:
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator as char)
            .setColumnSeparator(columnSeparator as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(headersPresent)
        uploadCsv(ser, 32)
        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(new BlobQueryOptions(expression).setInputSerialization(ser).setOutputSerialization(ser)).getValue()
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        notThrown(IOException)
        if (headersPresent) {
            /* Account for 16 bytes of header. */
            for (int j = 16; j < downloadedData.length; j++) {
                assert queryData[j - 16] == downloadedData[j]
            }
            for (int k = downloadedData.length - 16; k < downloadedData.length; k++) {
                assert queryData[k] == 0
            }
        } else {
            queryData == downloadedData
        }

        /* Output Stream. */
        when:
        OutputStream os = new ByteArrayOutputStream()
        bc.queryWithResponse(new BlobQueryOptions(expression, os)
            .setInputSerialization(ser).setOutputSerialization(ser), null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        if (headersPresent) {
            assert osData.length == downloadedData.length - 16
            /* Account for 16 bytes of header. */
            for (int j = 16; j < downloadedData.length; j++) {
                assert osData[j - 16] == downloadedData[j]
            }
        } else {
            osData == downloadedData
        }

        where:
        recordSeparator | columnSeparator | headersPresent || _
        '\n'            | ','             | false          || _ /* Default. */
        '\n'            | ','             | true           || _ /* Headers. */
        '\t'            | ','             | false          || _ /* Record separator. */
        '\r'            | ','             | false          || _
        '<'             | ','             | false          || _
        '>'             | ','             | false          || _
        '&'             | ','             | false          || _
        '\\'            | ','             | false          || _
        ','             | '.'             | false          || _ /* Column separator. */
//        ','             | '\n'            | false          || _ /* Keep getting a qq error: Field delimiter and record delimiter must be different characters. */
        ','             | ';'             | false          || _
        '\n'            | '\t'            | false          || _
//        '\n'            | '\r'            | false          || _ /* Keep getting a qq error: Field delimiter and record delimiter must be different characters. */
        '\n'            | '<'             | false          || _
        '\n'            | '>'             | false          || _
        '\n'            | '&'             | false          || _
        '\n'            | '\\'            | false          || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query csv serialization escape and field quote"() {
        setup:
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\\' as char) /* Escape set here. */
            .setFieldQuote('"' as char)  /* Field quote set here*/
            .setHeadersPresent(false)
        uploadCsv(ser, 32)

        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(new BlobQueryOptions(expression).setInputSerialization(ser).setOutputSerialization(ser)).getValue()
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        notThrown(IOException)
        queryData == downloadedData


        /* Output Stream. */
        when:
        OutputStream os = new ByteArrayOutputStream()
        bc.queryWithResponse(new BlobQueryOptions(expression, os)
            .setInputSerialization(ser).setOutputSerialization(ser), null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        osData == downloadedData
    }

    /* Note: Input delimited tested everywhere */
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query Input json"() {
        setup:
        BlobQueryJsonSerialization ser = new BlobQueryJsonSerialization()
            .setRecordSeparator('\n' as char)
        uploadSmallJson(numCopies)
        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        bc.download(downloadData)
        downloadData.write(10) /* writing extra new line */
        byte[] downloadedData = downloadData.toByteArray()
        OutputStream os = new ByteArrayOutputStream()
        BlobQueryOptions options = new BlobQueryOptions(expression, os)
            .setInputSerialization(ser).setOutputSerialization(ser)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        notThrown(IOException)
        queryData == downloadedData

        /* Output Stream. */
        when:
        bc.queryWithResponse(options, null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        osData == downloadedData

        where:
        numCopies || _
        0         || _
        10        || _
        100       || _
        1000      || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_10_02")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query Input parquet"() {
        setup:
        String fileName = "parquet.parquet"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        BlobQueryParquetSerialization ser = new BlobQueryParquetSerialization()
        bc.uploadFromFile(f.getAbsolutePath(), true)
        byte[] expectedData = "0,mdifjt55.ea3,mdifjt55.ea3\n".getBytes()

        def expression = "select * from blobstorage where id < 1;"

        BlobQueryOptions optionsIs = new BlobQueryOptions(expression).setInputSerialization(ser)
        OutputStream os = new ByteArrayOutputStream()
        BlobQueryOptions optionsOs = new BlobQueryOptions(expression, os).setInputSerialization(ser)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(optionsIs).getValue()
        byte[] queryData = readFromInputStream(qqStream, expectedData.length)

        then:
        notThrown(IOException)
        queryData == expectedData

        /* Output Stream. */
        when:
        bc.queryWithResponse(optionsOs, null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        osData == expectedData
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query Input csv Output json"() {
        setup:
        BlobQueryDelimitedSerialization inSer = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(inSer, 1)
        BlobQueryJsonSerialization outSer = new BlobQueryJsonSerialization()
            .setRecordSeparator('\n' as char)
        def expression = "SELECT * from BlobStorage"
        byte[] expectedData = "{\"_1\":\"100\",\"_2\":\"200\",\"_3\":\"300\",\"_4\":\"400\"}".getBytes()
        OutputStream os = new ByteArrayOutputStream()
        BlobQueryOptions options = new BlobQueryOptions(expression, os).setInputSerialization(inSer).setOutputSerialization(outSer)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()
        byte[] queryData = readFromInputStream(qqStream, expectedData.length)

        then:
        notThrown(IOException)
        for (int j = 0; j < expectedData.length; j++) {
            assert queryData[j] == expectedData[j]
        }

        /* Output Stream. */
        when:
        bc.queryWithResponse(options, null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        for (int j = 0; j < expectedData.length; j++) {
            assert osData[j] == expectedData[j]
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query Input json Output csv"() {
        setup:
        BlobQueryJsonSerialization inSer = new BlobQueryJsonSerialization()
            .setRecordSeparator('\n' as char)
        uploadSmallJson(2)
        BlobQueryDelimitedSerialization outSer = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        def expression = "SELECT * from BlobStorage"
        byte[] expectedData = "owner0,owner1\n".getBytes()
        OutputStream os = new ByteArrayOutputStream()
        BlobQueryOptions options = new BlobQueryOptions(expression, os).setInputSerialization(inSer).setOutputSerialization(outSer)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()
        byte[] queryData = readFromInputStream(qqStream, expectedData.length)

        then:
        notThrown(IOException)
        for (int j = 0; j < expectedData.length; j++) {
            assert queryData[j] == expectedData[j]
        }

        /* Output Stream. */
        when:
        bc.queryWithResponse(options, null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        for (int j = 0; j < expectedData.length; j++) {
            assert osData[j] == expectedData[j]
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query Input csv Output arrow"() {
        setup:
        BlobQueryDelimitedSerialization inSer = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(inSer, 32)
        List<BlobQueryArrowField> schema = new ArrayList<>()
        schema.add(new BlobQueryArrowField(BlobQueryArrowFieldType.DECIMAL).setName("Name").setPrecision(4).setScale(2))
        BlobQueryArrowSerialization outSer = new BlobQueryArrowSerialization().setSchema(schema)
        def expression = "SELECT _2 from BlobStorage WHERE _1 > 250;"
        String expectedData = "/////4AAAAAQAAAAAAAKAAwABgAFAAgACgAAAAABAwAMAAAACAAIAAAABAAIAAAABAAAAAEAAAAUAAAAEAAUAAgABgAHAAwAAAAQABAAAAAAAAEHJAAAABQAAAAEAAAAAAAAAAgADAAEAAgACAAAAAQAAAACAAAABAAAAE5hbWUAAAAAAAAAAP////9wAAAAEAAAAAAACgAOAAYABQAIAAoAAAAAAwMAEAAAAAAACgAMAAAABAAIAAoAAAAwAAAABAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAAAAAP////+IAAAAFAAAAAAAAAAMABYABgAFAAgADAAMAAAAAAMDABgAAAAAAgAAAAAAAAAACgAYAAwABAAIAAoAAAA8AAAAEAAAACAAAAAAAAAAAAAAAAIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAABAAAAIAAAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAAkAEAAAAAAAAAAAAAAAAAAJABAAAAAAAAAAAAAAAAAACQAQAAAAAAAAAAAAAAAAAA"
        OutputStream os = new ByteArrayOutputStream()
        BlobQueryOptions options = new BlobQueryOptions(expression, os).setInputSerialization(inSer).setOutputSerialization(outSer)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()
        byte[] queryData = readFromInputStream(qqStream, 912)

        then:
        notThrown(IOException)
        Base64.getEncoder().encodeToString(queryData) == expectedData

        /* Output Stream. */
        when:
        bc.queryWithResponse(options, null, null)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        Base64.getEncoder().encodeToString(osData) == expectedData
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query non fatal error"() {
        setup:
        BlobQueryDelimitedSerialization base = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(base.setColumnSeparator('.' as char), 32)
        MockErrorConsumer receiver = new MockErrorConsumer("InvalidColumnOrdinal")
        def expression = "SELECT _1 from BlobStorage WHERE _2 > 250"
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setInputSerialization(base.setColumnSeparator(',' as char))
            .setOutputSerialization(base.setColumnSeparator(',' as char))
            .setErrorConsumer(receiver)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()
        readFromInputStream(qqStream, Constants.KB)

        then:
        receiver.numErrors > 0
        notThrown(IOException)

        /* Output Stream. */
        when:
        receiver = new MockErrorConsumer("InvalidColumnOrdinal")
        options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(base.setColumnSeparator(',' as char))
            .setOutputSerialization(base.setColumnSeparator(',' as char))
            .setErrorConsumer(receiver)
        bc.queryWithResponse(options, null, null)

        then:
        notThrown(IOException)
        receiver.numErrors > 0
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query fatal error"() {
        setup:
        BlobQueryDelimitedSerialization base = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(true)
        uploadCsv(base.setColumnSeparator('.' as char), 32)
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(new BlobQueryJsonSerialization())

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()
        readFromInputStream(qqStream, Constants.KB)

        then:
        thrown(Throwable)

        /* Output Stream. */
        when:
        bc.queryWithResponse(options, null, null)

        then:
        thrown(Exceptions.ReactiveException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query progress receiver"() {
        setup:
        BlobQueryDelimitedSerialization base = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)

        uploadCsv(base.setColumnSeparator('.' as char), 32)

        def mockReceiver = new MockProgressConsumer()
        def sizeofBlobToRead = bc.getProperties().getBlobSize()
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setProgressConsumer(mockReceiver as Consumer)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()

        /* The QQ Avro stream has the following pattern
           n * (data record -> progress record) -> end record */
        // 1KB of data will only come back as a single data record.
        /* Pretend to read more data because the input stream will not parse records following the data record if it
         doesn't need to. */
        readFromInputStream(qqStream, Constants.MB)

        then:
        // At least the size of blob to read will be in the progress list
        mockReceiver.progressList.contains(sizeofBlobToRead)

        /* Output Stream. */
        when:
        mockReceiver = new MockProgressConsumer()
        options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setProgressConsumer(mockReceiver as Consumer)
        bc.queryWithResponse(options, null, null)

        then:
        mockReceiver.progressList.contains(sizeofBlobToRead)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @LiveOnly // Large amount of data.
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query multiple records with progress receiver"() {
        setup:
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, 512000)

        def mockReceiver = new MockProgressConsumer()
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setProgressConsumer(mockReceiver as Consumer)

        /* Input Stream. */
        when:
        InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue()

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

        /* Output Stream. */
        when:
        mockReceiver = new MockProgressConsumer()
        temp = 0
        options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setProgressConsumer(mockReceiver as Consumer)
        bc.queryWithResponse(options, null, null)

        then:
        // Make sure theyre all increasingly bigger
        for (long progress : mockReceiver.progressList) {
            assert progress >= temp
            temp = progress
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query snapshot"() {
        setup:
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as char)
            .setEscapeChar('\0' as char)
            .setFieldQuote('\0' as char)
            .setHeadersPresent(false)
        uploadCsv(ser, 32)
        def expression = "SELECT * from BlobStorage"

        /* Create snapshot of blob. */
        def snapshotClient = bc.createSnapshot()
        bc.upload(new ByteArrayInputStream(new byte[0]), 0, true) /* Make the blob empty. */

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        snapshotClient.download(downloadData)
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */
        when:
        InputStream qqStream = snapshotClient.openQueryInputStream(expression)
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)

        then:
        notThrown(IOException)
        queryData == downloadedData

        /* Output Stream. */
        when:
        OutputStream os = new ByteArrayOutputStream()
        snapshotClient.query(os, expression)
        byte[] osData = os.toByteArray()

        then:
        notThrown(BlobStorageException)
        osData == downloadedData
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query input output IA"() {
        setup:
        /* Mock random impl of QQ Serialization*/
        BlobQuerySerialization ser = new RandomOtherSerialization()
        def inSer = input ? ser : null
        def outSer = output ? ser : null
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(inSer)
            .setOutputSerialization(outSer)

        when:
        bc.openQueryInputStreamWithResponse(options).getValue() /* Don't need to call read. */

        then:
        thrown(IllegalArgumentException)

        when:
        bc.queryWithResponse(options, null, null)

        then:
        thrown(IllegalArgumentException)

        where:
        input   | output   || _
        true    | false    || _
        false   | true     || _
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query arrow input IA"() {
        setup:
        def inSer = new BlobQueryArrowSerialization()
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setInputSerialization(inSer)

        when:
        InputStream stream = bc.openQueryInputStreamWithResponse(options).getValue()  /* Don't need to call read. */

        then:
        thrown(IllegalArgumentException)

        when:
        options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(inSer)
        bc.queryWithResponse(options, null, null)

        then:
        thrown(IllegalArgumentException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2020_10_02")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query parquet output IA"() {
        setup:
        def outSer = new BlobQueryParquetSerialization()
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setOutputSerialization(outSer)

        when:
        InputStream stream = bc.openQueryInputStreamWithResponse(options).getValue()  /* Don't need to call read. */

        then:
        thrown(IllegalArgumentException)

        when:
        options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setOutputSerialization(outSer)
        bc.queryWithResponse(options, null, null)

        then:
        thrown(IllegalArgumentException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query error"() {
        setup:
        bc = cc.getBlobClient(generateBlobName())

        when:
        bc.openQueryInputStream("SELECT * from BlobStorage") /* Don't need to call read. */

        then:
        thrown(BlobStorageException)

        when:
        bc.query(new ByteArrayOutputStream(), "SELECT * from BlobStorage")

        then:
        thrown(BlobStorageException)
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { env.testMode == TestMode.LIVE })
    def "Query AC"() {
        setup:
        def t = new HashMap<String, String>()
        t.put("foo", "bar")
        bc.setTags(t)
        match = setupBlobMatchCondition(bc, match)
        leaseID = setupBlobLeaseCondition(bc, leaseID)
        def bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags)
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions optionsIs = new BlobQueryOptions(expression)
            .setRequestConditions(bac)
        BlobQueryOptions optionsOs = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setRequestConditions(bac)

        when:
        InputStream stream = bc.openQueryInputStreamWithResponse(optionsIs).getValue()
        stream.read()
        stream.close()

        then:
        notThrown(BlobStorageException)

        when:
        bc.queryWithResponse(optionsOs, null, null)

        then:
        notThrown(BlobStorageException)

        where:
        modified | unmodified | match        | noneMatch   | leaseID         | tags
        null     | null       | null         | null        | null            | null
        oldDate  | null       | null         | null        | null            | null
        null     | newDate    | null         | null        | null            | null
        null     | null       | receivedEtag | null        | null            | null
        null     | null       | null         | garbageEtag | null            | null
        null     | null       | null         | null        | receivedLeaseID | null
        null     | null       | null         | null        | null            | "\"foo\" = 'bar'"
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "V2019_12_12")
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
            .setTagsConditions(tags)
        def expression = "SELECT * from BlobStorage"
        BlobQueryOptions optionsIs = new BlobQueryOptions(expression)
            .setRequestConditions(bac)
        BlobQueryOptions optionsOs = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setRequestConditions(bac)

        when:
        bc.openQueryInputStreamWithResponse(optionsIs).getValue() /* Don't need to call read. */

        then:
        thrown(BlobStorageException)

        when:
        bc.queryWithResponse(optionsOs, null, null)

        then:
        thrown(BlobStorageException)

        where:
        modified | unmodified | match       | noneMatch    | leaseID        | tags
        newDate  | null       | null        | null         | null           | null
        null     | oldDate    | null        | null         | null           | null
        null     | null       | garbageEtag | null         | null           | null
        null     | null       | null        | receivedEtag | null           | null
        null     | null       | null        | null         | garbageLeaseID | null
        null     | null       | null        | null        | null            | "\"notfoo\" = 'notbar'"
    }

    class MockProgressConsumer implements Consumer<BlobQueryProgress> {

        List<Long> progressList

        MockProgressConsumer() {
            this.progressList = new ArrayList<>()
        }

        @Override
        void accept(BlobQueryProgress progress) {
            progressList.add(progress.getBytesScanned())
        }
    }

    class MockErrorConsumer implements Consumer<BlobQueryError> {

        String expectedType
        int numErrors

        MockErrorConsumer(String expectedType) {
            this.expectedType = expectedType
            this.numErrors = 0
        }

        @Override
        void accept(BlobQueryError nonFatalError) {
            assert !nonFatalError.isFatal()
            assert nonFatalError.getName() == expectedType
            numErrors++
        }
    }

    class RandomOtherSerialization implements BlobQuerySerialization {

    }
}
