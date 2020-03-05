// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

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

/**
 * An implementation of {@link InputStream} that allows users to read blob quick query responses.
 */
public class BlobQuickQueryInputStream extends InputStream {

    // The stream that returns parsed avro
    private DataFileStream<GenericRecord> parsedStream;

    // The internal user buffer used to hold parsed data.
    private ByteArrayInputStream buffer;

    // Whether or not the end record has been seen or not.
    private boolean endRecordSeen;

    // The raw avro stream coming from the network.
    private InputStream networkStream;

    // User provided error and progress receivers.
    private final ErrorReceiver<BlobQuickQueryError> errorReceiver;
    private final ProgressReceiver progressReceiver;

    /**
     * Creates a new {@code BlobQuickQueryInputStream}
     * @param networkStream {@link InputStream network stream}
     * @param errorReceiver {@link ErrorReceiver<BlobQuickQueryError>}
     * @param progressReceiver {@link ProgressReceiver}
     */
    BlobQuickQueryInputStream(InputStream networkStream, ErrorReceiver<BlobQuickQueryError> errorReceiver,
        ProgressReceiver progressReceiver) {
        this.networkStream = networkStream;
        this.errorReceiver = errorReceiver;
        this.progressReceiver = progressReceiver;
        this.endRecordSeen = false;
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
        if (this.parsedStream == null) {
            /* Reads from the network to determine schema. */
            parsedStream = new DataFileStream<>(networkStream, new GenericDatumReader<>());
            this.buffer = new ByteArrayInputStream(new byte[0]);
        }

        /* Now we are guaranteed that buffer is SOMETHING. */
        /* No data is available in the buffer.  */
        if (this.buffer.available() == 0) {
            /* Try to get more data. Note: not every avro record contains data, so we need to keep
                parsing records until a data record is found. */
            while (this.buffer.available() == 0 && this.parsedStream.hasNext()) {
                parseRecord(this.parsedStream.next());
            }
        }
        /* Now either data is available in the buffer or the end record was hit. */

        /* Data is now available in the buffer. */
        if (this.buffer.available() > 0) {
            return this.buffer.read(b, off, len);
        }

        /* End record was seen, there is no more data available to be read from the stream. Return -1. */
        if (this.endRecordSeen) {
            return -1;
        } else {
            throw new IOException("Error parsing blob quick query stream. No end record was present. This may indicate "
                + "that not all data was returned.");
        }
    }

    @Override
    public int read() throws IOException {
        return this.read(new byte[1], 0, 1);
    }

    @Override
    public void close() throws IOException {
        if (this.parsedStream != null) {
            this.parsedStream.close();
        }
        if (this.buffer != null) {
            buffer.close();
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
                    this.buffer = new ByteArrayInputStream(((ByteBuffer) data).array());
                }
                break;
            } case "end": {
                if (this.progressReceiver != null) {
                    Object total = record.get("totalBytes");

                    if (checkParametersNotNull("end", total)) {
                        this.progressReceiver.reportProgress((Long) total);
                    }
                }
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

                    if (this.errorReceiver != null) {
                        this.errorReceiver.reportError(error);
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
