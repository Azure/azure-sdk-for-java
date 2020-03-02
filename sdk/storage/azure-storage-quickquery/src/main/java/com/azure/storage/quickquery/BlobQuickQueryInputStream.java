// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver;
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

    private final BlobQuickQueryErrorReceiver nonFatalErrorHandler;
    private final ProgressReceiver progress;


    BlobQuickQueryInputStream(InputStream networkStream, BlobQuickQueryErrorReceiver nonFatalErrorHandler,
        ProgressReceiver progress) {
        this.networkStream = networkStream;
        this.nonFatalErrorHandler = nonFatalErrorHandler;
        this.progress = progress;
        this.endRecordSeen = false;
        this.firstRead = true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (firstRead) {
            parsedStream = new DataFileStream<>(networkStream, new GenericDatumReader<>());
            firstRead = false;
        }

        /* Extract records as they come in. */
        while(!this.endRecordSeen
            || ((this.userBuffer == null || this.userBuffer.available() <= 0) && this.parsedStream.hasNext())) {
            GenericRecord record = this.parsedStream.next();
            String schemaName = record.getSchema().getName();

            switch (schemaName) {
                case "resultData": {
                    Object data = record.get("data");
                    if (validParameters(data)) {
                        this.userBuffer = new ByteArrayInputStream(((ByteBuffer) data).array());
                    } else {
                        throw new IOException("Failed to parse result data record from blob query response stream.");
                    }
                    break;
                } case "end": {
                    this.endRecordSeen = true;
                    break;
                } case "progress": {
                    if(this.progress != null) {
                        Object scanned = record.get("bytesScanned");
                        Object total = record.get("totalBytes");
                        if(validParameters(scanned, total)) {
                            this.progress.reportProgress((Long) scanned);
                        } else {
                            throw new IOException("Failed to parse progress record from blob query response stream.");
                        }
                    }
                    break;
                } case "error": {
                    BlobQuickQueryError blobQueryError;
                    Object fatal = record.get("fatal");
                    Object name = record.get("name");
                    Object description = record.get("description");
                    Object position = record.get("position");

                    if(validParameters(fatal, name, description, position)) {
                        blobQueryError = new BlobQuickQueryError((Boolean) fatal, name.toString(),
                            description.toString(), (Long) position);
                    } else {
                        throw new IOException("Failed to parse error record from blob query response stream.");
                    }

                    if (this.nonFatalErrorHandler != null) {
                        this.nonFatalErrorHandler.reportError(blobQueryError);
                    } else {
                        throw new IOException("An error reported during blob query operation processing, but no "
                            + "handler was given. Error details: " + System.lineSeparator() +
                            blobQueryError.toString());
                    }
                    break;
                }
                default:
                    throw new IOException("Unknown record type " + schemaName + " in blob query response parsing.");
            }
        }

        if (this.userBuffer == null || this.userBuffer.available() == 0) {
            if (!this.endRecordSeen) {
                throw new IOException("No end record was present in the response from the blob query. This may "
                    + "indicate that not all data was returned.");
            }
        } else {
            return this.userBuffer.read(b, off, len);
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

    private boolean validParameters(Object ... data) {
        boolean noNullObject = true;
        for (Object o : data) {
            noNullObject &= (o != null);
        }
        return noNullObject;
    }

}
