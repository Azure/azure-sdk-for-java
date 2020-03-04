// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ErrorReceiver;
import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BlobQuickQueryInputStream extends InputStream {

    private DataFileStream<GenericRecord> parsedStream;
    private ByteArrayInputStream userBuffer;
    private boolean endRecordSeen;
    private boolean firstRead;
    private InputStream networkStream;

    private final ErrorReceiver<BlobQuickQueryError> nonFatalErrorHandler;
    private final ProgressReceiver progressReceiver;

    /**
     * Creates a new {@code BlobQuickQueryInputStream}
     * @param networkStream {@link InputStream network stream}
     * @param nonFatalErrorHandler {@link ErrorReceiver<BlobQuickQueryError>}
     * @param progressReceiver {@link ProgressReceiver}
     */
    BlobQuickQueryInputStream(InputStream networkStream, ErrorReceiver<BlobQuickQueryError> nonFatalErrorHandler,
        ProgressReceiver progressReceiver) {
        this.networkStream = networkStream;
        this.nonFatalErrorHandler = nonFatalErrorHandler;
        this.progressReceiver = progressReceiver;
        this.endRecordSeen = false;
        this.firstRead = true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        validateParameters(b, off, len);

        /* If len is 0, then no bytes are read and 0 is returned. */
        if (len == 0) {
            return 0;
        }
        /* Attempt to read at least one byte. If no byte is available because the stream is at end of file,
           the value -1 is returned; otherwise, at least one byte is read and stored into b. */

        /* First read? */
        if (firstRead) {
            firstRead = false;
            /* Reads from the network to determine schema. */
            parsedStream = new DataFileStream<>(networkStream, new GenericDatumReader<>());
            this.userBuffer = new ByteArrayInputStream(new byte[0]);
        }

        /* Now we are guaranteed that buffer is SOMETHING. */
        /* No data is available in the buffer.  */
        if (this.userBuffer.available() == 0) {
            /* Try to get more data */
            while (this.userBuffer.available() == 0 && this.parsedStream.hasNext()) {
                parseRecord(this.parsedStream.next());
            }
        }
        /* Data is now available in the buffer. */
        if (this.userBuffer.available() > 0) {
            return this.userBuffer.read(b, off, len);
        }

        /* End record was seen, there is no more data available to be read from the stream. Return -1. */
        if (this.endRecordSeen) {
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

    /**
     * Validates that all parameters are non-null. Throws IOException if any of them are.
     */
    private boolean checkParametersNotNull(String record, Object... data) throws IOException {
        for (Object o : data) {
            if (o == null) {
                throw new IOException("Failed to parse " + record + " record from blob query response stream.");
            }
        }
        return true;
    }

    /**
     * Parses a generic avro record to known data types.
     * @param record {@link GenericRecord}
     * @throws IOException on failed parsing.
     */
    private void parseRecord(GenericRecord record) throws IOException {
        String schemaName = record.getSchema().getName();

        switch (schemaName) {
            case "resultData": {
                Object data = record.get("data");

                if (checkParametersNotNull("result data", data)) {
                    this.userBuffer = new ByteArrayInputStream(((ByteBuffer) data).array());
                }
                break;
            } case "end": {
                this.endRecordSeen = true;
                break;
            } case "progress": {
                if (this.progressReceiver != null) {
                    Object scanned = record.get("bytesScanned");
                    Object total = record.get("totalBytes");

                    if (checkParametersNotNull("progress", scanned, total)) {
                        this.progressReceiver.reportProgress((Long) scanned);
                    }
                }
                break;
            } case "error": {
                Object fatal = record.get("fatal");
                Object name = record.get("name");
                Object description = record.get("description");
                Object position = record.get("position");

                if (checkParametersNotNull("error", fatal, name, description, position)) {
                    BlobQuickQueryError error = new BlobQuickQueryError((Boolean) fatal, name.toString(),
                        description.toString(), (Long) position);

                    if (this.nonFatalErrorHandler != null && !error.isFatal()) {
                        this.nonFatalErrorHandler.reportError(error);
                    } else {
                        throw new IOException("An error was reported during blob quick query response processing, "
                            + System.lineSeparator() + error.toString());
                    }
                }
                break;
            }
            default:
                throw new IOException("Unknown record type " + schemaName + " during blob quick query response "
                    + "parsing.");
        }
    }

    /**
     * Validates parameters according to {@link InputStream#read(byte[], int, int)} spec.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b at which the data is written.
     * @param len the maximum number of bytes to read.
     */
    private void validateParameters(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException("'b' cannot be null");
        }
        if (off < -1) {
            throw new IndexOutOfBoundsException("'off' cannot be less than -1");
        }
        if (len < -1) {
            throw new IndexOutOfBoundsException("'len' cannot be less than -1");
        }
        if (len > (b.length - off)) {
            throw new IndexOutOfBoundsException("'len' cannot be greater than 'b'.length - 'off'");
        }
    }

}
