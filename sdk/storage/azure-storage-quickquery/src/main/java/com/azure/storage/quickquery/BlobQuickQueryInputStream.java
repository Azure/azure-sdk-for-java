// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

public class BlobQuickQueryInputStream extends InputStream {

    private final ClientLogger logger;

    private DataFileStream<GenericData> stream; // parser

    private InputStream readBuffer; // keeps track of buffering for user

    private final BlobQuickQueryErrorReceiver nonFatalErrorHandler;
    private final ProgressReceiver progress;

    private boolean endRecordSeen;


    BlobQuickQueryInputStream(InputStream readBuffer, BlobQuickQueryErrorReceiver nonFatalErrorHandler,
        ProgressReceiver progress, ClientLogger logger) {
        this.logger = logger;
        try {
            stream = new DataFileStream<>(readBuffer, new GenericDatumReader<>());
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
        this.readBuffer = readBuffer;
        this.progress = progress;
        this.nonFatalErrorHandler = nonFatalErrorHandler;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        /* Extract records as they come in. */
        while((this.readBuffer == null || this.readBuffer.available() <= 0) && this.stream.hasNext()) {
            GenericRecord record = (GenericRecord) this.stream.next();

            String recordSchema = record.getSchema().getName();

            if(recordSchema.equalsIgnoreCase("resultData")) {

                Object obj = record.get("data");
                if (obj != null) {
                    this.readBuffer = new ByteArrayInputStream(((ByteBuffer)obj).array());
                } else {
                    throw new IOException("Failed to parse result data record from blob query response stream.");
                }

            } else if (recordSchema.equalsIgnoreCase("end")) {

                this.endRecordSeen = true;
                break;

            } else if (record.getSchema().getName().equalsIgnoreCase("progress")) {

                if (progress != null) {
                    Object scanned = record.get("bytesScanned");
                    Object total = record.get("totalBytes");

                    if (scanned != null && total != null) {
                        this.progress.reportProgress((Long) scanned);
                    } else {
                        throw new IOException("Failed to parse progress record from blob query response stream.");
                    }
                }

            } else if(record.getSchema().getName().equalsIgnoreCase("error")) {

                BlobQuickQueryError blobQueryError;
                Object fatal = record.get("fatal");
                Object name = record.get("name");
                Object description = record.get("description");
                Object position = record.get("position");
                if (fatal != null && name != null && description != null && position != null)  {
                    blobQueryError = new BlobQuickQueryError((Boolean) fatal, name.toString(), description.toString(),
                        (Long) position);
                } else {
                    throw new IOException("Failed to parse error record from blob query response stream.");
                }

                if (this.nonFatalErrorHandler != null) {
                    this.nonFatalErrorHandler.reportError(blobQueryError);
                } else {
                    throw new IOException("An error reported during blob query operation processing, but no handler "
                        + "was given.  Error details: " + System.lineSeparator() + blobQueryError.toString());
                }

            } else {
                throw new IOException("Unknown record type " + record.getSchema().getName() + " in blob query "
                    + "response parsing.");
            }
        }

        if (this.readBuffer == null || this.readBuffer.available() == 0) {
            if (!this.endRecordSeen) {
                throw new IOException("No end record was present in the response from the blob query. This may "
                    + "indicate that not all data was returned.");
            }
        } else {
            return this.readBuffer.read(b, off, len);
        }

        return 0;
    }

    @Override
    public int read() throws IOException {
        return this.read(new byte[1], 0, 1);
    }

    @Override
    public void close() throws IOException {
        if(this.stream != null) {
            this.stream.close();
        }
        super.close();
    }

}
