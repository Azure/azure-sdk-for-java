// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class BlobQuickQueryInputStream extends InputStream {

    private DataFileStream<GenericRecord> parsedStream;
    private ByteArrayInputStream userBuffer;
    private boolean endRecordSeen;
    private boolean firstRead;
    private InputStream networkStream;

    private final BlobQuickQueryErrorReceiver nonFatalErrorHandler;
    private final ProgressReceiver progress;

    private final ClientLogger logger;


    BlobQuickQueryInputStream(InputStream networkStream, BlobQuickQueryErrorReceiver nonFatalErrorHandler,
        ProgressReceiver progress, ClientLogger logger) {
        this.networkStream = networkStream;
        this.nonFatalErrorHandler = nonFatalErrorHandler;
        this.progress = progress;
        this.endRecordSeen = false;
        this.firstRead = true;
        this.logger = logger;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (firstRead) {
            firstRead = false;
            parsedStream = new DataFileStream<>(networkStream, new GenericDatumReader<>());
            System.out.println("READ SCHEMA");
            return 0;
        }

        // Data records were found
        if (this.userBuffer != null) {

            if (this.userBuffer.available() > 0) {
                return this.userBuffer.read(b, off, len);
            } else if (this.userBuffer.available() == 0) {
                // End record was seen, there is no more data available to be read from the stream. Return -1.
                if (this.endRecordSeen) {
                    System.out.println("END RECORD SEEN");
                    return -1;
                }
                // Request more data.
                if (this.parsedStream.hasNext()) {
                    parseRecord(this.parsedStream.next());
                }
            }
        } else {
            if (this.parsedStream.hasNext()) {
                parseRecord(this.parsedStream.next());
                return 0;
            }
            // No data records were seen in the stream
            if (!this.endRecordSeen) {
                throw logger.logExceptionAsError(new UncheckedIOException(new IOException("No end record was "
                    + "present in the response from the blob query. This may indicate that not all data was "
                    + "returned.")));
            }
            return -1;
        }
        return 0;
    }

    @Override
    public int read() throws IOException {
        return this.read(new byte[1], 0, 1);
    }

    @Override
    public void close() throws IOException {
        if(this.parsedStream != null) {
            this.parsedStream.close();
        }
        super.close();
    }

    private boolean validParameters(String record, Object ... data) {
        for (Object o : data) {
            if (o == null) {
                throw logger.logExceptionAsError(new IllegalStateException("Failed to parse " + record + " record from blob query response stream."));
            }
        }
        return true;
    }

    private void parseRecord(GenericRecord record) {
        String schemaName = record.getSchema().getName();

        switch (schemaName) {
            case "resultData": {
                Object data = record.get("data");
                if (validParameters("result data", data)) {
                    this.userBuffer = new ByteArrayInputStream(((ByteBuffer) data).array());
                }
                break;
            } case "end": {
                this.endRecordSeen = true;
                break;
            } case "progress": {
                if(this.progress != null) {
                    Object scanned = record.get("bytesScanned");
                    Object total = record.get("totalBytes");
                    if(validParameters("progress", scanned, total)) {
                        this.progress.reportProgress((Long) scanned);
                    }
                }
                break;
            } case "error": {
                BlobQuickQueryError blobQueryError = null;
                Object fatal = record.get("fatal");
                Object name = record.get("name");
                Object description = record.get("description");
                Object position = record.get("position");

                if (validParameters("error", fatal, name, description, position)) {
                    blobQueryError = new BlobQuickQueryError((Boolean) fatal, name.toString(),
                        description.toString(), (Long) position);
                }

                if (this.nonFatalErrorHandler != null) {
                    this.nonFatalErrorHandler.reportError(blobQueryError);
                } else {
                    throw logger.logExceptionAsError(new UncheckedIOException(new IOException("An error reported during blob query operation processing, but no "
                        + "handler was given. Error details: " + System.lineSeparator() +
                        blobQueryError.toString())));
                }
                break;
            }
            default:
                throw logger.logExceptionAsError(new UncheckedIOException(new IOException("Unknown record type " + schemaName + " in blob query response parsing.")));
        }
    }

}
