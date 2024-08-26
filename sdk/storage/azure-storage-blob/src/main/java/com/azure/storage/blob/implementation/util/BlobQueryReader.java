// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.models.ArrowConfiguration;
import com.azure.storage.blob.implementation.models.ArrowField;
import com.azure.storage.blob.implementation.models.DelimitedTextConfiguration;
import com.azure.storage.blob.implementation.models.JsonTextConfiguration;
import com.azure.storage.blob.implementation.models.QueryFormat;
import com.azure.storage.blob.implementation.models.QueryFormatType;
import com.azure.storage.blob.implementation.models.QuerySerialization;
import com.azure.storage.blob.models.BlobQueryArrowField;
import com.azure.storage.blob.models.BlobQueryArrowSerialization;
import com.azure.storage.blob.models.BlobQueryDelimitedSerialization;
import com.azure.storage.blob.models.BlobQueryError;
import com.azure.storage.blob.models.BlobQueryJsonSerialization;
import com.azure.storage.blob.models.BlobQueryParquetSerialization;
import com.azure.storage.blob.models.BlobQueryProgress;
import com.azure.storage.blob.models.BlobQuerySerialization;
import com.azure.storage.common.implementation.FluxInputStream;
import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.azure.storage.internal.avro.implementation.AvroObject;
import com.azure.storage.internal.avro.implementation.AvroReaderFactory;
import com.azure.storage.internal.avro.implementation.AvroReaderSyncFactory;
import com.azure.storage.internal.avro.implementation.schema.AvroSchema;
import com.azure.storage.internal.avro.implementation.schema.primitive.AvroNullSchema;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class provides helper methods for blob query functions.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public class BlobQueryReader {

    private final ClientLogger LOGGER = new ClientLogger(BlobQueryReader.class);
    private final Flux<ByteBuffer> avro;
    private final Consumer<BlobQueryProgress> progressConsumer;
    private final Consumer<BlobQueryError> errorConsumer;

    /**
     * Creates a new BlobQueryReader.
     *
     * @param avro The reactive avro stream.
     * @param progressConsumer The progress consumer.
     * @param errorConsumer The error consumer.
     */
    public BlobQueryReader(Flux<ByteBuffer> avro, Consumer<BlobQueryProgress> progressConsumer,
        Consumer<BlobQueryError> errorConsumer) {
        this.avro = avro;
        this.progressConsumer = progressConsumer;
        this.errorConsumer = errorConsumer;
    }

    /**
     * Avro parses a query reactive stream.
     * <p>
     * The Avro stream is formatted as the Avro Header (that specifies the schema) and the Avro Body (that contains
     * a series of blocks of data). The Query Avro schema indicates that the objects being emitted from the parser can
     * either be a result data record, an end record, a progress record or an error record.
     *
     * @return The parsed query reactive stream.
     */
    public Flux<ByteBuffer> read() {
        return new AvroReaderFactory().getAvroReader(avro).read()
            .map(AvroObject::getObject)
            .concatMap(this::parseRecord);
    }

    /**
     * Avro parses a query reactive stream.
     *
     * The Avro stream is formatted as the Avro Header (that specifies the schema) and the Avro Body (that contains
     * a series of blocks of data). The Query Avro schema indicates that the objects being emitted from the parser can
     * either be a result data record, an end record, a progress record or an error record.
     *
     * @param inputStream The input stream to read from.
     * @return The parsed query reactive stream.
     * @throws IOException If an I/O error occurs.
     */
    public InputStream readInputStream(InputStream inputStream) {
        // Convert InputStream to Flux<ByteBuffer>
        Flux<ByteBuffer> avroFlux = FluxUtil.toFluxByteBuffer(inputStream);

        // Use existing read method to process the data
        Flux<ByteBuffer> processedData = new AvroReaderFactory().getAvroReader(avroFlux).read()
            .map(AvroObject::getObject)
            .concatMap(this::parseRecord);

        return new FluxInputStream(processedData);
    }

    /**
     * Avro parses a query reactive stream.
     *
     * The Avro stream is formatted as the Avro Header (that specifies the schema) and the Avro Body (that contains
     * a series of blocks of data). The Query Avro schema indicates that the objects being emitted from the parser can
     * either be a result data record, an end record, a progress record or an error record.
     *
     * @param inputStream The input stream to read from.
     * @return The parsed query reactive stream.
     * @throws IOException If an I/O error occurs.
     */
    public InputStream readInputStream2(InputStream inputStream) throws IOException {
        AvroReaderSyncFactory avroReaderSyncFactory = new AvroReaderSyncFactory();
        ByteBuffer fullBuffer = convertInputStreamToByteBuffer(inputStream);

        Iterable<AvroObject> avroObjects = avroReaderSyncFactory.getAvroReader(fullBuffer).read();

        // serialize AvroObject back to bytes
        byte[] processedData = serializeAvroObjectsToBytes(avroObjects);
        return new ByteArrayInputStream(processedData);
    }

    private ByteBuffer convertInputStreamToByteBuffer(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; // Temporary buffer size
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        byte[] data = outputStream.toByteArray(); // Get all data read from the stream

        // Convert the byte array to ByteBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length);
        byteBuffer.put(data);
        byteBuffer.flip(); // Prepare the buffer for reading
        return byteBuffer;
    }

    private byte[] serializeAvroObjectsToBytes(Iterable<AvroObject> avroObjects) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (AvroObject avroObject : avroObjects) {
            try {
                Object potentialMap = avroObject.getObject();
                if (!(potentialMap instanceof Map)) {
                    throw new IllegalArgumentException("Expected object to be of type Map");
                }
                Map<?, ?> recordMap = (Map<?, ?>) potentialMap; // Safely cast it to a Map
                ByteBuffer buffer = parseSyncRecord(recordMap); // Use the Map directly

                if (buffer != null) {
                    if (buffer.hasArray()) {
                        outputStream.write(buffer.array(), buffer.position(), buffer.remaining());
                    } else {
                        // If the buffer does not have an accessible array, manually copy
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        outputStream.write(data);
                    }
                    buffer.clear(); // Prepare buffer for next iteration if reusing
                }
            } catch (IOException e) {
                System.out.println("Error occurred while serializing AvroObjects to bytes: " + e.getMessage());
                throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
            }
        }
        return outputStream.toByteArray();
    }

    /**
     * Parses a query record.
     *
     * @param quickQueryRecord The query record.
     * @return The optional data in the record.
     */
    private Mono<ByteBuffer> parseRecord(Object quickQueryRecord) {
        if (!(quickQueryRecord instanceof Map)) {
            return Mono.error(new IllegalArgumentException("Expected object to be of type Map"));
        }
        Map<?, ?> record = (Map<?, ?>) quickQueryRecord;
        Object recordSchema = record.get(AvroConstants.RECORD);

        switch (recordSchema.toString()) {
            case "resultData":
                return parseResultData(record);
            case "end":
                return parseEnd(record);
            case "progress":
                return parseProgress(record);
            case "error":
                return parseError(record);
            default:
                return Mono.error(new IllegalStateException(String.format("Unknown record type %s "
                    + "while parsing query response. ", recordSchema.toString())));
        }
    }

    /**
     * Parses a query record.
     *
     * @param quickQueryRecord The query record.
     * @return The optional data in the record.
     */
    private ByteBuffer parseSyncRecord(Object quickQueryRecord) {
        if (!(quickQueryRecord instanceof Map)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Expected object to be of type Map"));
        }
        Map<?, ?> record = (Map<?, ?>) quickQueryRecord;
        Object recordSchema = record.get(AvroConstants.RECORD);

        switch (recordSchema.toString()) {
            case "resultData":
                return parseSyncResultData(record);
            case "end":
                return parseSyncEnd(record);
            case "progress":
                return parseSyncProgress(record);
            case "error":
                return parseSyncError(record);
            default:
                throw LOGGER.logExceptionAsError(new IllegalStateException(String.format("Unknown record type %s "
                    + "while parsing query response. ", recordSchema.toString())));
        }
    }

    /**
     * Parses a query result data record.
     * @param dataRecord The query result data record.
     * @return The data in the record.
     */
    private Mono<ByteBuffer> parseResultData(Map<?, ?> dataRecord) {
        Object data = dataRecord.get("data");

        if (checkParametersNotNull(data)) {
            AvroSchema.checkType("data", data, List.class);
            return Mono.just(ByteBuffer.wrap(AvroSchema.getBytes((List<?>) data)));
        } else {
            return Mono.error(new IllegalArgumentException("Failed to parse result data record from "
                + "query response stream."));
        }
    }

    /**
     * Parses a query result data record.
     * @param dataRecord The query result data record.
     * @return The data in the record.
     */
    private ByteBuffer parseSyncResultData(Map<?, ?> dataRecord) {
        Object data = dataRecord.get("data");

        if (checkParametersNotNull(data)) {
            AvroSchema.checkType("data", data, List.class);
            return ByteBuffer.wrap(AvroSchema.getBytes((List<?>) data));
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Failed to parse result data record from "
                + "query response stream."));
        }
    }

    /**
     * Parses a query end record.
     * @param endRecord The query end record.
     * @return Mono.empty or Mono.error
     */
    private Mono<ByteBuffer> parseEnd(Map<?, ?> endRecord) {
        if (progressConsumer != null) {
            Object totalBytes = endRecord.get("totalBytes");

            if (checkParametersNotNull(totalBytes)) {
                AvroSchema.checkType("totalBytes", totalBytes, Long.class);
                progressConsumer.accept(new BlobQueryProgress((long) totalBytes, (long) totalBytes));
            } else {
                return Mono.error(new IllegalArgumentException("Failed to parse end record from query "
                    + "response stream."));
            }
        }
        return Mono.empty();
    }

    /**
     * Parses a query end record.
     * @param endRecord The query end record.
     * @return Mono.empty or Mono.error
     */
    private ByteBuffer parseSyncEnd(Map<?, ?> endRecord) {
        if (progressConsumer != null) {
            Object totalBytes = endRecord.get("totalBytes");

            if (checkParametersNotNull(totalBytes)) {
                AvroSchema.checkType("totalBytes", totalBytes, Long.class);
                progressConsumer.accept(new BlobQueryProgress((long) totalBytes, (long) totalBytes));
            } else {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Failed to parse end record from query "
                    + "response stream."));
            }
        }
        return null;
    }

    /**
     * Parses a query progress record.
     * @param progressRecord The query progress record.
     * @return Mono.empty or Mono.error
     */
    private Mono<ByteBuffer> parseProgress(Map<?, ?> progressRecord) {
        if (progressConsumer != null) {
            Object bytesScanned = progressRecord.get("bytesScanned");
            Object totalBytes = progressRecord.get("totalBytes");

            if (checkParametersNotNull(bytesScanned, totalBytes)) {
                AvroSchema.checkType("bytesScanned", bytesScanned, Long.class);
                AvroSchema.checkType("totalBytes", totalBytes, Long.class);
                progressConsumer.accept(new BlobQueryProgress((long) bytesScanned, (long) totalBytes));
            } else {
                return Mono.error(new IllegalArgumentException("Failed to parse progress record from "
                    + "query response stream."));
            }
        }
        return Mono.empty();
    }

    /**
     * Parses a query progress record.
     * @param progressRecord The query progress record.
     * @return Mono.empty or Mono.error
     */
    private ByteBuffer parseSyncProgress(Map<?, ?> progressRecord) {
        if (progressConsumer != null) {
            Object bytesScanned = progressRecord.get("bytesScanned");
            Object totalBytes = progressRecord.get("totalBytes");

            if (checkParametersNotNull(bytesScanned, totalBytes)) {
                AvroSchema.checkType("bytesScanned", bytesScanned, Long.class);
                AvroSchema.checkType("totalBytes", totalBytes, Long.class);
                progressConsumer.accept(new BlobQueryProgress((long) bytesScanned, (long) totalBytes));
            } else {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Failed to parse progress record from "
                    + "query response stream."));
            }
        }
        return null;
    }

    /**
     * Parses a query error record.
     * @param errorRecord The query error record.
     * @return Mono.empty or Mono.error
     */
    private Mono<ByteBuffer> parseError(Map<?, ?> errorRecord) {
        Object fatal = errorRecord.get("fatal");
        Object name = errorRecord.get("name");
        Object description = errorRecord.get("description");
        Object position = errorRecord.get("position");

        if (checkParametersNotNull(fatal, name, description, position)) {
            AvroSchema.checkType("fatal", fatal, Boolean.class);
            AvroSchema.checkType("name", name, String.class);
            AvroSchema.checkType("description", description, String.class);
            AvroSchema.checkType("position", position, Long.class);

            BlobQueryError error = new BlobQueryError((Boolean) fatal, (String) name,
                (String) description, (Long) position);

            if (errorConsumer != null) {
                errorConsumer.accept(error);
            } else {
                return Mono.error(new IOException("An error was reported during query response processing, "
                    + System.lineSeparator() + error.toString()));
            }
        } else {
            return Mono.error(new IllegalArgumentException("Failed to parse error record from "
                + "query response stream."));
        }
        return Mono.empty();
    }

    /**
     * Parses a query error record.
     * @param errorRecord The query error record.
     * @return Mono.empty or Mono.error
     */
    private ByteBuffer parseSyncError(Map<?, ?> errorRecord) {
        Object fatal = errorRecord.get("fatal");
        Object name = errorRecord.get("name");
        Object description = errorRecord.get("description");
        Object position = errorRecord.get("position");

        if (checkParametersNotNull(fatal, name, description, position)) {
            AvroSchema.checkType("fatal", fatal, Boolean.class);
            AvroSchema.checkType("name", name, String.class);
            AvroSchema.checkType("description", description, String.class);
            AvroSchema.checkType("position", position, Long.class);

            BlobQueryError error = new BlobQueryError((Boolean) fatal, (String) name,
                (String) description, (Long) position);

            if (errorConsumer != null) {
                errorConsumer.accept(error);
            } else {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(new IOException("An error was reported during query response processing, "
                    + System.lineSeparator() + error.toString())));
            }
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Failed to parse error record from "
                + "query response stream."));
        }
        return null;
    }

    /**
     * Checks whether or not all parameters are non-null.
     */
    private static boolean checkParametersNotNull(Object... data) {
        for (Object o : data) {
            if (o == null || o instanceof AvroNullSchema.Null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Transforms a generic input BlobQuerySerialization into a QuerySerialization.
     * @param userSerialization {@link BlobQuerySerialization}
     * @param logger {@link ClientLogger}
     * @return {@link QuerySerialization}
     */
    public static QuerySerialization transformInputSerialization(BlobQuerySerialization userSerialization,
        ClientLogger logger) {
        if (userSerialization == null) {
            return null;
        }

        QueryFormat generatedFormat = new QueryFormat();
        if (userSerialization instanceof BlobQueryDelimitedSerialization) {

            generatedFormat.setType(QueryFormatType.DELIMITED);
            generatedFormat.setDelimitedTextConfiguration(transformDelimited(
                (BlobQueryDelimitedSerialization) userSerialization));

        } else if (userSerialization instanceof BlobQueryJsonSerialization) {

            generatedFormat.setType(QueryFormatType.JSON);
            generatedFormat.setJsonTextConfiguration(transformJson(
                (BlobQueryJsonSerialization) userSerialization));

        } else if (userSerialization instanceof BlobQueryParquetSerialization) {

            generatedFormat.setType(QueryFormatType.PARQUET);
            generatedFormat.setParquetTextConfiguration(transformParquet(
                (BlobQueryParquetSerialization) userSerialization));

        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Please see values of valid input serialization in the documentation "
                    + "(https://docs.microsoft.com/rest/api/storageservices/query-blob-contents#request-body)."));
        }
        return new QuerySerialization().setFormat(generatedFormat);
    }

    /**
     * Transforms a generic input BlobQuerySerialization into a QuerySerialization.
     * @param userSerialization {@link BlobQuerySerialization}
     * @param logger {@link ClientLogger}
     * @return {@link QuerySerialization}
     */
    public static QuerySerialization transformOutputSerialization(BlobQuerySerialization userSerialization,
        ClientLogger logger) {
        if (userSerialization == null) {
            return null;
        }

        QueryFormat generatedFormat = new QueryFormat();
        if (userSerialization instanceof BlobQueryDelimitedSerialization) {

            generatedFormat.setType(QueryFormatType.DELIMITED);
            generatedFormat.setDelimitedTextConfiguration(transformDelimited(
                (BlobQueryDelimitedSerialization) userSerialization));

        } else if (userSerialization instanceof BlobQueryJsonSerialization) {

            generatedFormat.setType(QueryFormatType.JSON);
            generatedFormat.setJsonTextConfiguration(transformJson(
                (BlobQueryJsonSerialization) userSerialization));

        } else if (userSerialization instanceof BlobQueryArrowSerialization) {

            generatedFormat.setType(QueryFormatType.ARROW);
            generatedFormat.setArrowConfiguration(transformArrow(
                (BlobQueryArrowSerialization) userSerialization));

        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Please see values of valid output serialization in the documentation "
                    + "(https://docs.microsoft.com/rest/api/storageservices/query-blob-contents#request-body)."));
        }
        return new QuerySerialization().setFormat(generatedFormat);
    }

    /**
     * Transforms a BlobQueryDelimitedSerialization into a DelimitedTextConfiguration.
     *
     * @param delimitedSerialization {@link BlobQueryDelimitedSerialization}
     * @return {@link DelimitedTextConfiguration}
     */
    private static DelimitedTextConfiguration transformDelimited(
        BlobQueryDelimitedSerialization delimitedSerialization) {
        if (delimitedSerialization == null) {
            return null;
        }
        return new DelimitedTextConfiguration()
            .setColumnSeparator(charToString(delimitedSerialization.getColumnSeparator()))
            .setEscapeChar(charToString(delimitedSerialization.getEscapeChar()))
            .setFieldQuote(charToString(delimitedSerialization.getFieldQuote()))
            .setHeadersPresent(delimitedSerialization.isHeadersPresent())
            .setRecordSeparator(charToString(delimitedSerialization.getRecordSeparator()));
    }

    /**
     * Transforms a BlobQueryJsonSerialization into a JsonTextConfiguration.
     *
     * @param jsonSerialization {@link BlobQueryJsonSerialization}
     * @return {@link JsonTextConfiguration}
     */
    private static JsonTextConfiguration transformJson(BlobQueryJsonSerialization jsonSerialization) {
        if (jsonSerialization == null) {
            return null;
        }
        return new JsonTextConfiguration()
            .setRecordSeparator(charToString(jsonSerialization.getRecordSeparator()));
    }

    /**
     * Transforms a BlobQueryParquetSerialization into an Object.
     *
     * @param parquetSerialization {@link BlobQueryParquetSerialization}
     * @return {@link JsonTextConfiguration}
     */
    private static Object transformParquet(BlobQueryParquetSerialization parquetSerialization) {
        /* This method returns an Object since the ParquetConfiguration currently accepts no options. This results in
        the generator generating ParquetConfiguration as an Object. */
        if (parquetSerialization == null) {
            return null;
        }
        return new Object();
    }

    /**
     * Transforms a BlobQueryArrowSerialization into a ArrowConfiguration.
     *
     * @param arrowSerialization {@link BlobQueryArrowSerialization}
     * @return {@link ArrowConfiguration}
     */
    private static ArrowConfiguration transformArrow(BlobQueryArrowSerialization arrowSerialization) {
        if (arrowSerialization == null) {
            return null;
        }
        List<ArrowField> schema = arrowSerialization.getSchema() == null ? null
            : new ArrayList<>(arrowSerialization.getSchema().size());
        if (schema != null) {
            for (BlobQueryArrowField field : arrowSerialization.getSchema()) {
                if (field == null) {
                    schema.add(null);
                } else {
                    schema.add(new ArrowField()
                        .setName(field.getName())
                        .setPrecision(field.getPrecision())
                        .setScale(field.getScale())
                        .setType(field.getType().toString())
                    );
                }
            }
        }
        return new ArrowConfiguration().setSchema(schema);
    }

    private static String charToString(char c) {
        return c == '\0' ? "" : Character.toString(c);
    }
}
