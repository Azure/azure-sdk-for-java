// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.test.utils.TestUtils;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobQueryArrowField;
import com.azure.storage.blob.models.BlobQueryArrowFieldType;
import com.azure.storage.blob.models.BlobQueryArrowSerialization;
import com.azure.storage.blob.models.BlobQueryDelimitedSerialization;
import com.azure.storage.blob.models.BlobQueryError;
import com.azure.storage.blob.models.BlobQueryJsonSerialization;
import com.azure.storage.blob.models.BlobQueryParquetSerialization;
import com.azure.storage.blob.models.BlobQueryProgress;
import com.azure.storage.blob.models.BlobQuerySerialization;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobBaseApiTests extends BlobTestBase {
    private BlobClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
        bc.upload(new ByteArrayInputStream(new byte[0]), 0);
    }

    /* Quick Query Tests. */

    // Generates and uploads a CSV file
    private void uploadCsv(BlobQueryDelimitedSerialization s, int numCopies) {
        String header = String.join(String.valueOf(s.getColumnSeparator()), "rn1", "rn2", "rn3", "rn4")
            .concat(String.valueOf(s.getRecordSeparator()));
        byte[] headers = header.getBytes();

        String csv = String.join(String.valueOf(s.getColumnSeparator()), "100", "200", "300", "400")
            .concat(String.valueOf(s.getRecordSeparator()))
            .concat(String.join(String.valueOf(s.getColumnSeparator()), "300", "400", "500", "600")
                .concat(String.valueOf(s.getRecordSeparator())));

        byte[] csvData = csv.getBytes();
        int headerLength = s.isHeadersPresent() ? headers.length : 0;
        byte[] data = new byte[headerLength + csvData.length * numCopies];
        if (s.isHeadersPresent()) {
            System.arraycopy(headers, 0, data, 0, headers.length);
        }

        for (int i = 0; i < numCopies; i++) {
            int o = i * csvData.length + headerLength;
            System.arraycopy(csvData, 0, data, o, csvData.length);
        }

        InputStream inputStream = new ByteArrayInputStream(data);
        bc.upload(inputStream, data.length, true);
    }

    private void uploadSmallJson(int numCopies) {
        StringBuilder b = new StringBuilder();
        b.append("{\n");
        for (int i = 0; i < numCopies; i++) {
            String toAppend = "\t\"name%d\": \"owner%d\",\n";
            b.append(String.format(toAppend, i, i));
        }
        b.append('}');

        InputStream inputStream = new ByteArrayInputStream(b.toString().getBytes());
        bc.upload(inputStream, b.length(), true);
    }

    private byte[] readFromInputStream(InputStream stream, int numBytesToRead) throws IOException {
        byte[] queryData = new byte[numBytesToRead];

        int totalRead = 0;
        int bytesRead = 0;
        int length = numBytesToRead;

        while (bytesRead != -1 && totalRead < numBytesToRead) {
            bytesRead = stream.read(queryData, totalRead, length);
            if (bytesRead != -1) {
                totalRead += bytesRead;
                length -= bytesRead;
            }
        }

        stream.close();
        return queryData;
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @ValueSource(ints = {
        1, // 32 bytes
        32,  // 1 KB
        256, // 8 KB
        400, // 12 ish KB
        4000,  // 125 KB
    })
    public void queryMin(int numCopies) {
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);

        liveTestScenarioWithRetry(() -> {
            uploadCsv(ser, numCopies);
            String expression = "SELECT * from BlobStorage";

            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            bc.downloadStream(downloadData);
            byte[] downloadedData = downloadData.toByteArray();

            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStream(expression);
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, downloadedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
            TestUtils.assertArraysEqual(downloadedData, queryData);

            /* Output Stream. */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bc.query(os, expression);
            TestUtils.assertArraysEqual(downloadedData, os.toByteArray());
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("queryCsvSerializationSeparatorSupplier")
    public void queryCsvSerializationSeparator(char recordSeparator, char columnSeparator, boolean headersPresentIn,
        boolean headersPresentOut) {
        BlobQueryDelimitedSerialization serIn = new BlobQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator)
            .setColumnSeparator(columnSeparator)
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(headersPresentIn);
        BlobQueryDelimitedSerialization serOut = new BlobQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator)
            .setColumnSeparator(columnSeparator)
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(headersPresentOut);
        uploadCsv(serIn, 32);
        String expression = "SELECT * from BlobStorage";

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        bc.downloadStream(downloadData);
        byte[] downloadedData = downloadData.toByteArray();

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(new BlobQueryOptions(expression)
                .setInputSerialization(serIn).setOutputSerialization(serOut)).getValue();
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, downloadedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            if (headersPresentIn && !headersPresentOut) {
                /* Account for 16 bytes of header. */
                TestUtils.assertArraysEqual(downloadedData, 16, queryData, 0, downloadedData.length - 16);

                for (int k = downloadedData.length - 16; k < downloadedData.length; k++) {
                    assertEquals((byte) 0, queryData[k]);
                }
            } else {
                TestUtils.assertArraysEqual(downloadedData, queryData);
            }

            /* Output Stream. */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bc.queryWithResponse(new BlobQueryOptions(expression, os)
                .setInputSerialization(serIn).setOutputSerialization(serOut), null, null);
            byte[] osData = os.toByteArray();

            if (headersPresentIn && !headersPresentOut) {
                assertEquals(downloadedData.length - 16, osData.length);

                /* Account for 16 bytes of header. */
                TestUtils.assertArraysEqual(downloadedData, 16, osData, 0, downloadedData.length - 16);
            } else {
                TestUtils.assertArraysEqual(downloadedData, osData);
            }
        });
    }

    private static Stream<Arguments> queryCsvSerializationSeparatorSupplier() {
        return Stream.of(
            Arguments.of('\n', ',', false, false), /* Default. */
            Arguments.of('\n', ',', true, true), /* Headers. */
            Arguments.of('\n', ',', true, false), /* Headers. */
            Arguments.of('\t', ',', false, false), /* Record separator. */
            Arguments.of('\r', ',', false, false),
            Arguments.of('<', ',', false, false),
            Arguments.of('>', ',', false, false),
            Arguments.of('&', ',', false, false),
            Arguments.of('\\', ',', false, false),
            Arguments.of(',', '.', false, false), /* Column separator. */
//            Arguments.of(',', '\n', false, false), /* Keep getting a qq error: Field delimiter and record delimiter must be different characters. */
            Arguments.of(',', ';', false, false),
            Arguments.of('\n', '\t', false, false), /* Record separator. */
//            Arguments.of('\n', '\r', false, false), /* Keep getting a qq error: Field delimiter and record delimiter must be different characters. */
            Arguments.of('\n', '<', false, false),
            Arguments.of('\n', '>', false, false),
            Arguments.of('\n', '&', false, false),
            Arguments.of('\n', '\\', false, false)
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryCsvSerializationEscapeAndFieldQuote() {
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\\') /* Escape set here. */
            .setFieldQuote('"')  /* Field quote set here*/
            .setHeadersPresent(false);
        uploadCsv(ser, 32);
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            bc.downloadStream(downloadData);
            byte[] downloadedData = downloadData.toByteArray();

            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(new BlobQueryOptions(expression)
                .setInputSerialization(ser).setOutputSerialization(ser)).getValue();
            byte[] queryData = new byte[0];
            try {
                queryData = readFromInputStream(qqStream, downloadedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            TestUtils.assertArraysEqual(downloadedData, queryData);

            /* Output Stream. */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bc.queryWithResponse(new BlobQueryOptions(expression, os)
                .setInputSerialization(ser).setOutputSerialization(ser), null, null);
            byte[] osData = os.toByteArray();

            TestUtils.assertArraysEqual(downloadedData, osData);
        });
    }

    /* Note: Input delimited tested everywhere */
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, 1000})
    public void queryInputJson(int numCopies) {
        BlobQueryJsonSerialization ser = new BlobQueryJsonSerialization().setRecordSeparator('\n');
        uploadSmallJson(numCopies);
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
            bc.downloadStream(downloadData);
            downloadData.write(10); /* writing extra new line */
            byte[] downloadedData = downloadData.toByteArray();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BlobQueryOptions options = new BlobQueryOptions(expression, os)
                .setInputSerialization(ser).setOutputSerialization(ser);

            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, downloadedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
            TestUtils.assertArraysEqual(downloadedData, queryData);

            /* Output Stream. */
            bc.queryWithResponse(options, null, null);
            byte[] osData = os.toByteArray();

            TestUtils.assertArraysEqual(downloadedData, osData);
        });

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void queryInputParquet() {
        String fileName = "parquet.parquet";
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
        BlobQueryParquetSerialization ser = new BlobQueryParquetSerialization();
        bc.uploadFromFile(f.getAbsolutePath(), true);
        byte[] expectedData = "0,mdifjt55.ea3,mdifjt55.ea3\n".getBytes();

        String expression = "select * from blobstorage where id < 1;";

        BlobQueryOptions optionsIs = new BlobQueryOptions(expression).setInputSerialization(ser);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BlobQueryOptions optionsOs = new BlobQueryOptions(expression, os).setInputSerialization(ser);

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(optionsIs).getValue();
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, expectedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
            TestUtils.assertArraysEqual(queryData, expectedData);
            /* Output Stream. */
            bc.queryWithResponse(optionsOs, null, null);
            byte[] osData = os.toByteArray();
            TestUtils.assertArraysEqual(osData, expectedData);
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputCsvOutputJson() {
        BlobQueryDelimitedSerialization inSer = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(inSer, 1);
        BlobQueryJsonSerialization outSer = new BlobQueryJsonSerialization().setRecordSeparator('\n');
        String expression = "SELECT * from BlobStorage";
        byte[] expectedData = "{\"_1\":\"100\",\"_2\":\"200\",\"_3\":\"300\",\"_4\":\"400\"}".getBytes();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BlobQueryOptions options = new BlobQueryOptions(expression, os).setInputSerialization(inSer)
            .setOutputSerialization(outSer);

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, expectedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            TestUtils.assertArraysEqual(expectedData, 0, queryData, 0, expectedData.length);

            /* Output Stream. */
            bc.queryWithResponse(options, null, null);
            byte[] osData = os.toByteArray();

            TestUtils.assertArraysEqual(expectedData, 0, osData, 0, expectedData.length);
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputJsonOutputCsv() {
        BlobQueryJsonSerialization inSer = new BlobQueryJsonSerialization()
            .setRecordSeparator('\n');
        uploadSmallJson(2);
        BlobQueryDelimitedSerialization outSer = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        String expression = "SELECT * from BlobStorage";
        byte[] expectedData = "owner0,owner1\n".getBytes();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BlobQueryOptions options = new BlobQueryOptions(expression, os).setInputSerialization(inSer)
            .setOutputSerialization(outSer);

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, expectedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            TestUtils.assertArraysEqual(expectedData, 0, queryData, 0, expectedData.length);

            /* Output Stream. */
            bc.queryWithResponse(options, null, null);
            byte[] osData = os.toByteArray();

            TestUtils.assertArraysEqual(expectedData, 0, osData, 0, expectedData.length);
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputCsvOutputArrow() {
        BlobQueryDelimitedSerialization inSer = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(inSer, 32);

        liveTestScenarioWithRetry(() -> {
            List<BlobQueryArrowField> schema = new ArrayList<>();
            schema.add(new BlobQueryArrowField(BlobQueryArrowFieldType.DECIMAL).setName("Name").setPrecision(4)
                .setScale(2));
            BlobQueryArrowSerialization outSer = new BlobQueryArrowSerialization().setSchema(schema);
            String expression = "SELECT _2 from BlobStorage WHERE _1 > 250;";
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BlobQueryOptions options = new BlobQueryOptions(expression, os).setOutputSerialization(outSer);

            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();
            try {
                readFromInputStream(qqStream, 920);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            /* Output Stream. */
            assertDoesNotThrow(() -> bc.queryWithResponse(options, null, null));
        });

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryNonFatalError() {
        BlobQueryDelimitedSerialization base = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(base.setColumnSeparator('.'), 32);
        final MockErrorConsumer receiver = new MockErrorConsumer("InvalidColumnOrdinal");
        String expression = "SELECT _1 from BlobStorage WHERE _2 > 250";
        final BlobQueryOptions options = new BlobQueryOptions(expression)
            .setInputSerialization(base.setColumnSeparator(','))
            .setOutputSerialization(base.setColumnSeparator(','))
            .setErrorConsumer(receiver);

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();
            try {
                readFromInputStream(qqStream, Constants.KB);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            assertDoesNotThrow(() -> receiver.numErrors > 0);

            /* Output Stream. */
            MockErrorConsumer receiver2 = new MockErrorConsumer("InvalidColumnOrdinal");
            BlobQueryOptions options2 = new BlobQueryOptions(expression, new ByteArrayOutputStream())
                .setInputSerialization(base.setColumnSeparator(','))
                .setOutputSerialization(base.setColumnSeparator(','))
                .setErrorConsumer(receiver2);
            bc.queryWithResponse(options2, null, null);

            assertDoesNotThrow(() -> receiver2.numErrors > 0);
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryFatalError() {
        BlobQueryDelimitedSerialization base = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(true);
        uploadCsv(base.setColumnSeparator('.'), 32);
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(new BlobQueryJsonSerialization());

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();
            assertThrows(Throwable.class, () -> readFromInputStream(qqStream, Constants.KB));

            /* Output Stream. */
            //Exceptions.ReactiveException.class
            assertThrows(Throwable.class, () -> bc.queryWithResponse(options, null, null));
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryProgressReceiver() {
        BlobQueryDelimitedSerialization base = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);

        uploadCsv(base.setColumnSeparator('.'), 32);

        final MockProgressConsumer mockReceiver = new MockProgressConsumer();
        long sizeofBlobToRead = bc.getProperties().getBlobSize();
        String expression = "SELECT * from BlobStorage";
        final BlobQueryOptions options = new BlobQueryOptions(expression)
            .setProgressConsumer(mockReceiver);

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();

            /* The QQ Avro stream has the following pattern n * (data record -> progress record) -> end record */
            // 1KB of data will only come back as a single data record.
            /* Pretend to read more data because the input stream will not parse records following the data record if it
            doesn't need to. */
            try {
                readFromInputStream(qqStream, Constants.MB);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            // At least the size of blob to read will be in the progress list
            assertTrue(mockReceiver.progressList.contains(sizeofBlobToRead));

            /* Output Stream. */
            MockProgressConsumer mockReceiver2 = new MockProgressConsumer();
            BlobQueryOptions options2 = new BlobQueryOptions(expression, new ByteArrayOutputStream())
                .setProgressConsumer(mockReceiver2);
            bc.queryWithResponse(options2, null, null);

            assertTrue(mockReceiver2.progressList.contains(sizeofBlobToRead));
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @LiveOnly // Large amount of data.
    @Test
    public void queryMultipleRecordsWithProgressReceiver() {
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(ser, 512000);

        MockProgressConsumer mockReceiver = new MockProgressConsumer();
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setProgressConsumer(mockReceiver);

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = bc.openQueryInputStreamWithResponse(options).getValue();

        /* The Avro stream has the following pattern
           n * (data record -> progress record) -> end record */
            // 1KB of data will only come back as a single data record.
        /* Pretend to read more data because the input stream will not parse records following the data record if it
         doesn't need to. */
            try {
                readFromInputStream(qqStream, 16 * Constants.MB);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            long temp = 0;
            // Make sure theyre all increasingly bigger
            for (long progress : mockReceiver.progressList) {
                assertTrue(progress >= temp);
                temp = progress;
            }

            /* Output Stream. */
            MockProgressConsumer mockReceiver2 = new MockProgressConsumer();
            temp = 0;
            BlobQueryOptions options2 = new BlobQueryOptions(expression, new ByteArrayOutputStream())
                .setProgressConsumer(mockReceiver2);
            bc.queryWithResponse(options2, null, null);

            // Make sure theyre all increasingly bigger
            for (long progress : mockReceiver2.progressList) {
                assertTrue(progress >= temp);
                temp = progress;
            }
        });

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void querySnapshot() {
        BlobQueryDelimitedSerialization ser = new BlobQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(ser, 32);
        String expression = "SELECT * from BlobStorage";

        /* Create snapshot of blob. */
        BlobClientBase snapshotClient = bc.createSnapshot();
        bc.upload(new ByteArrayInputStream(new byte[0]), 0, true); /* Make the blob empty. */

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        snapshotClient.download(downloadData);
        byte[] downloadedData = downloadData.toByteArray();

        liveTestScenarioWithRetry(() -> {
            /* Input Stream. */
            InputStream qqStream = snapshotClient.openQueryInputStream(expression);
            byte[] queryData;
            try {
                queryData = readFromInputStream(qqStream, downloadedData.length);
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }

            TestUtils.assertArraysEqual(queryData, downloadedData);

            /* Output Stream. */
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            snapshotClient.query(os, expression);
            byte[] osData = os.toByteArray();

            TestUtils.assertArraysEqual(osData, downloadedData);
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @CsvSource({ "true, false", "false, true" })
    public void queryInputOutputIA(boolean input, boolean output) {
        /* Mock random impl of QQ Serialization*/
        BlobQuerySerialization ser = new RandomOtherSerialization();
        BlobQuerySerialization inSer = input ? ser : null;
        BlobQuerySerialization outSer = output ? ser : null;
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions options = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(inSer)
            .setOutputSerialization(outSer);

        liveTestScenarioWithRetry(() -> {
            assertThrows(IllegalArgumentException.class,
                () -> bc.openQueryInputStreamWithResponse(options).getValue()); /* Don't need to call read. */
            assertThrows(IllegalArgumentException.class, () -> bc.queryWithResponse(options, null, null));
        });
    }

    private static class RandomOtherSerialization implements BlobQuerySerialization {

    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryArrowInputIA() {
        BlobQueryArrowSerialization inSer = new BlobQueryArrowSerialization();
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setInputSerialization(inSer);

        liveTestScenarioWithRetry(() -> {
            assertThrows(IllegalArgumentException.class,
                () -> bc.openQueryInputStreamWithResponse(options).getValue()  /* Don't need to call read. */);
            BlobQueryOptions options2 = new BlobQueryOptions(expression, new ByteArrayOutputStream())
                .setInputSerialization(inSer);
            assertThrows(IllegalArgumentException.class, () -> bc.queryWithResponse(options2, null, null));
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void queryParquetOutputIA() {
        BlobQueryParquetSerialization outSer = new BlobQueryParquetSerialization();
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions options = new BlobQueryOptions(expression)
            .setOutputSerialization(outSer);

        liveTestScenarioWithRetry(() -> {
            assertThrows(IllegalArgumentException.class,
                () -> bc.openQueryInputStreamWithResponse(options).getValue()  /* Don't need to call read. */);

            BlobQueryOptions options2 = new BlobQueryOptions(expression, new ByteArrayOutputStream())
                .setOutputSerialization(outSer);
            assertThrows(IllegalArgumentException.class, () -> bc.queryWithResponse(options2, null, null));
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryError() {
        liveTestScenarioWithRetry(() -> {
            bc = cc.getBlobClient(generateBlobName());
            assertThrows(BlobStorageException.class,
                () -> bc.openQueryInputStream("SELECT * from BlobStorage") /* Don't need to call read. */);
            assertThrows(BlobStorageException.class,
                () -> bc.query(new ByteArrayOutputStream(), "SELECT * from BlobStorage"));
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("queryACSupplier")
    public void queryAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions optionsIs = new BlobQueryOptions(expression)
            .setRequestConditions(bac);
        BlobQueryOptions optionsOs = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setRequestConditions(bac);

        liveTestScenarioWithRetry(() -> {
            InputStream stream = bc.openQueryInputStreamWithResponse(optionsIs).getValue();
            try {
                stream.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            assertDoesNotThrow(stream::close);
            assertDoesNotThrow(() -> bc.queryWithResponse(optionsOs, null, null));
        });
    }

    private static Stream<Arguments> queryACSupplier() {
        return Stream.of(Arguments.of(null, null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID, null),
            Arguments.of(null, null, null, null, null, "\"foo\" = 'bar'")
            );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void queryACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);
        String expression = "SELECT * from BlobStorage";
        BlobQueryOptions optionsIs = new BlobQueryOptions(expression)
            .setRequestConditions(bac);
        BlobQueryOptions optionsOs = new BlobQueryOptions(expression, new ByteArrayOutputStream())
            .setRequestConditions(bac);

        assertThrows(BlobStorageException.class,
            () -> bc.openQueryInputStreamWithResponse(optionsIs).getValue() /* Don't need to call read. */);

        assertThrows(BlobStorageException.class,
            () -> bc.queryWithResponse(optionsOs, null, null));
    }

    /*@RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @Test
    public void copyFromURLSourceErrorAndStatusCode() {
        BlockBlobClient destBlob = cc.getBlobClient(generateBlobName()).getBlockBlobClient();

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> destBlob.copyFromUrl(bc.getBlobUrl()));

        assertTrue(e.getStatusCode() == 409);
        assertTrue(e.getServiceMessage().contains("PublicAccessNotPermitted"));
        assertTrue(e.getServiceMessage().contains("Public access is not permitted on this storage account."));
    }*/

    static class MockProgressConsumer implements Consumer<BlobQueryProgress> {

        List<Long> progressList;

        MockProgressConsumer() {
            this.progressList = new ArrayList<>();
        }

        @Override
        public void accept(BlobQueryProgress progress) {
            progressList.add(progress.getBytesScanned());
        }
    }

    static class MockErrorConsumer implements Consumer<BlobQueryError> {

        String expectedType;
        int numErrors;

        MockErrorConsumer(String expectedType) {
            this.expectedType = expectedType;
            this.numErrors = 0;
        }

        @Override
        public void accept(BlobQueryError nonFatalError) {
            assertFalse(nonFatalError.isFatal());
            assertEquals(nonFatalError.getName(), expectedType);
            numErrors++;
        }
    }
}
