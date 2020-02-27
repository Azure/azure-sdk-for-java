// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobRequestConditions;

import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryResponse;
import com.azure.storage.quickquery.models.BlobQuickQuerySerialization;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableByteArrayInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Duration;

import static com.azure.storage.common.implementation.StorageImplUtils.blockWithOptionalTimeout;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob quick query.
 *
 * <p>This client offers the ability to query contents of a structured blob. </p>
 *
 * @see BlobQuickQueryClientBuilder
 */
@ServiceClient(builder = BlobQuickQueryClientBuilder.class)
public class BlobQuickQueryClient {
    private final ClientLogger logger = new ClientLogger(BlobQuickQueryClient.class);

    private BlobQuickQueryAsyncClient client;

    /**
     * Package-private constructor for use by {@link BlobQuickQueryClientBuilder}.
     * @param client the async blob quick query client
     */
    BlobQuickQueryClient(BlobQuickQueryAsyncClient client) {
        this.client = client;
    }

    /**
     * Queries an entire blob into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryClient.query#OutputStream-String}
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param expression The query expression.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    public void query(OutputStream stream, String expression) {
        queryWithResponse(stream, expression, null, null, null, null, Context.NONE);
    }

    /**
     * Queries an entire blob into an output stream.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.storage.quickquery.BlobQuickQueryClient.queryWithResponse#OutputStream-String-BlobQuickQuerySerialization-BlobQuickQuerySerialization-BlobRequestConditions-Duration-Context}
     *
     * @param stream A non-null {@link OutputStream} instance where the downloaded data will be written.
     * @param expression The query expression.
     * @param input {@link BlobQuickQuerySerialization Serialization input}.
     * @param output {@link BlobQuickQuerySerialization Serialization output}.
     * @param requestConditions {@link BlobRequestConditions}
     * @param timeout An optional timeout value beyond which a {@link RuntimeException} will be raised.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws UncheckedIOException If an I/O error occurs.
     * @throws NullPointerException if {@code stream} is null.
     */
    public BlobQuickQueryResponse queryWithResponse(OutputStream stream, String expression,
        BlobQuickQuerySerialization input, BlobQuickQuerySerialization output, BlobRequestConditions requestConditions,
        Duration timeout, Context context) {
        StorageImplUtils.assertNotNull("stream", stream);
        Mono<BlobQuickQueryResponse> download = client
            .queryWithResponse(expression, input, output, requestConditions, context)
            .flatMap(response -> response.getValue().reduce(stream, (outputStream, buffer) -> {
                try {
                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
                    return outputStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new BlobQuickQueryResponse(response)));

        return blockWithOptionalTimeout(download, timeout);
    }

    public BlobQuickQueryResponse parsedQueryWithResponseSample(OutputStream stream, String expression,
        BlobQuickQuerySerialization input, BlobQuickQuerySerialization output,
        BlobQuickQueryErrorReceiver nonFatalErrorReceiver, BlobRequestConditions requestConditions,
        ProgressReceiver progressReceiver, Duration timeout, Context context) throws RuntimeException {
        StorageImplUtils.assertNotNull("stream", stream);
        ByteArrayOutputStream avroStream = new ByteArrayOutputStream();
        Mono<BlobQuickQueryResponse> download = client
            .queryWithResponse(expression, input, output, requestConditions, context)
            .flatMap(response -> response.getValue().reduce(avroStream, (aStream, buffer) -> {
                try {
                    aStream.write(FluxUtil.byteBufferToArray(buffer));
                    return aStream;
                } catch (IOException ex) {
                    throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                }
            }).thenReturn(new BlobQuickQueryResponse(response)));

        DataFileReader<GenericRecord> reader;
        try {
            reader = new DataFileReader<>(
                new SeekableByteArrayInput(avroStream.toByteArray()),
                new GenericDatumReader<>());
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
        }
        long totalBytes = 0;

        endOfStream:
        while(reader.hasNext())
        {
            GenericRecord record = reader.next();

            switch (record.getSchema().getName()) {
                case "resultData":
                    try {
                        stream.write(((ByteBuffer) record.get("data")).array());
                    } catch (IOException ex) {
                        throw logger.logExceptionAsError(Exceptions.propagate(new UncheckedIOException(ex)));
                    }
                    break;
                case "end":
                    if (progressReceiver != null) {
                        progressReceiver.reportProgress(totalBytes);
                    }
                    break endOfStream;
                case "progress":
                    if (progressReceiver != null) {
                        progressReceiver.reportProgress((long) record.get("bytesScanned"));
                    }
                    if (totalBytes == 0) {
                        /* This is for the end record to update progress receiver. */
                        totalBytes = (long) record.get("totalBytes");
                    }
                    break;
                case "error":
                    if (nonFatalErrorReceiver != null) {
                        String description = record.get("description").toString();
                        Boolean fatal = (Boolean) record.get("fatal");
                        if (fatal) {
                            nonFatalErrorReceiver.reportError(
                                new BlobQuickQueryError(fatal, record.get("name").toString(),
                                    description, (Long) record.get("position")));
                        } else {
                            throw logger.logExceptionAsError(new UncheckedIOException(new IOException("Failed to "
                                + "parse error record from blob query response stream.")));
                        }
                    }
                    break;
                default:
                    throw logger.logExceptionAsError(new UncheckedIOException(new IOException("Unknown record type "
                        + record.getSchema().getName() + " in blob query response parsing.")));
            }
        }

        return blockWithOptionalTimeout(download, timeout);
    }

//    public static void main(String[] args) throws IOException {
//        String accountName = "0cpxscnapsat09prde01f";
//        String accountKey = "EDy+1m2BEi7arksSm5UBabPgNfpkv8/nxWdKbi2geqPeTAxEw5eqtViNPNLQmXqY26RFlZDspyMQF1aDGblRrw==";
//        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
//        NettyAsyncHttpClientBuilder builder = new NettyAsyncHttpClientBuilder()
//            .wiretap(true)
//            .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)));
//
//        BlobServiceClient sc = new BlobServiceClientBuilder()
//            .endpoint("https://" + accountName + ".blob.preprod.core.windows.net")
//            .credential(credential)
//            .httpClient(builder.build())
//            .serviceVersion(BlobServiceVersion.V2019_12_12)
//            .buildClient();
//
//        BlobContainerClient cc = sc.getBlobContainerClient("myquickquerycontainer");
//
//        BlobClient bc = cc.getBlobClient("myquickqueryblob.csv");
//        byte[] headers = "one,two,three,four\n".getBytes();
//
//        byte[] csvData = "100,200,300,400\n300,400,500,600\n".getBytes();
//
//        byte[] data = new byte[1024 + headers.length];
//        System.arraycopy(headers, 0, data, 0, headers.length);
//
//        for (int i = 0; i < 32; i++) {
//            int o = i * csvData.length + headers.length;
//            System.arraycopy(csvData, 0, data, o, csvData.length);
//        }
//
//        InputStream inputStream = new ByteArrayInputStream(data);
//
//        bc.upload(inputStream, data.length, true);
//
//        String expression = "SELECT _2 from BlobStorage WHERE _1 > 250;";
//
//        BlobQuickQueryDelimitedSerialization input = new BlobQuickQueryDelimitedSerialization()
//            .setEscapeChar('\0')
//            .setColumnSeparator(',')
//            .setRecordSeparator('\n')
//            .setFieldQuote('\'')
//            .setHeadersPresent(true);
//
//        BlobQuickQueryDelimitedSerialization output = new BlobQuickQueryDelimitedSerialization()
//            .setEscapeChar('\0')
//            .setColumnSeparator(',')
//            .setRecordSeparator('.')
//            .setFieldQuote('\'')
//            .setHeadersPresent(true);
//
//        BlobQuickQueryClient qqc = new BlobQuickQueryClientBuilder(bc).buildClient();
//
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        qqc.queryWithResponse(os, expression, input, output, null, null, null);
//
//        ByteArrayOutputStream realOutputStream = new ByteArrayOutputStream();
//
//        DataFileReader<GenericRecord> reader = new DataFileReader<>(
//            new SeekableByteArrayInput(os.toByteArray()),
//            new GenericDatumReader<>());
//
//        while(reader.hasNext())
//        {
//            GenericRecord record = reader.next();
//
//            if (record.getSchema().getName().equals("resultData")) {
//                realOutputStream.write(((ByteBuffer) record.get("data")).array());
//            } else if (record.getSchema().getName().equals("end")) {
//                System.out.println("end");
//                break;
//            } else if (record.getSchema().getName().equals("progress")) {
//                System.out.println("progress: " + record.get("bytesScanned") + "/" + record.get("totalBytes"));
//            }  else if (record.getSchema().getName().equals("error")) {
//                BlobQuickQueryError error =
//                    new BlobQuickQueryError((Boolean) record.get("fatal"), record.get("name").toString(), record.get("description").toString(), (Long) record.get("position"));
//                System.out.println("error: " + error.isFatal() + " " + error.getName() + " " + error.getDescription() + " " + error.getPosition());
//            } else {
//                System.out.println("error parsing stream");
//            }
//        }
//        System.out.println("real output");
//        System.out.println(new String(realOutputStream.toByteArray()));
//        realOutputStream.close();
//        inputStream.close();
//        os.close();
//
//    }

}
