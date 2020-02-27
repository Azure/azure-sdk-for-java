// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery.models.implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.quickquery.models.BlobQuickQueryError;
import com.azure.storage.quickquery.models.BlobQuickQueryErrorReceiver;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.SeekableInput;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;

class SeekableStream implements SeekableInput
{
    InputStream stream;
    long offset=0;
    public SeekableStream(InputStream stream) {
        this.stream = stream;
    }
    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    @Override
    public void seek(long p) throws IOException {
        throw new IOException("Method not implemented.");
    }

    @Override
    public long tell() throws IOException {
        return offset;
    }

    @Override
    public long length() throws IOException {
        throw new IOException("Method not implemented.");
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readBytes = this.stream.read(b, off, len);
        offset += readBytes;
        return readBytes;
    }

}

public class BlobQueryResultStream extends InputStream {

    DataFileReader<GenericRecord> reader;
    DatumReader<GenericRecord> dataumReader;
    BlobQuickQueryErrorReceiver errorHandler;
    ProgressReceiver progress;
    ByteArrayInputStream readBuffer;
    ByteArrayOutputStream writeBuffer;
    boolean endRecordSeen = false;
    InputStream responseStream;

    public BlobQueryResultStream(InputStream stream, BlobQuickQueryErrorReceiver errorHandler, ProgressReceiver progress) throws IOException
    {
        this.responseStream = stream;
        reader = new DataFileReader<>(
            new SeekableStream(this.responseStream),
            new GenericDatumReader<>());

        this.errorHandler = errorHandler;
        this.progress = progress;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        while((this.readBuffer == null || this.readBuffer.available() <= 0) && this.reader.hasNext())
        {
            GenericRecord record = this.reader.next();

            if(record.getSchema().getName().equalsIgnoreCase("resultData"))
            {
                Object obj = record.get("data");
                if (obj != null)
                {
                    this.readBuffer = new ByteArrayInputStream(((ByteBuffer)obj).array());
                }
                else
                {
                    throw new IOException("Failed to parse result data record from blob query response stream.");
                }
            }
            else if(record.getSchema().getName().equalsIgnoreCase("end"))
            {
                this.endRecordSeen = true;
                break;
            }
            else if(record.getSchema().getName().equalsIgnoreCase("progress"))
            {
                if(this.progress != null)
                {
                    Object scanned = record.get("bytesScanned");
                    Object total = record.get("totalBytes");
                    if(scanned != null && total != null)
                    {
                        this.progress.reportProgress((Long) scanned);
                    }
                    else
                    {
                        throw new IOException("Failed to parse progress record from blob query response stream.");
                    }
                }
            }
            else if(record.getSchema().getName().equalsIgnoreCase("error"))
            {
                BlobQuickQueryError blobQueryError;
                Object fatal = record.get("fatal");
                Object name = record.get("name");
                Object description = record.get("description");
                Object position = record.get("position");
                if(fatal !=null && name != null && description != null && position != null)
                {
                    blobQueryError = new BlobQuickQueryError((Boolean)fatal, (String)name, (String)description, (Long)position);
                }
                else
                {
                    throw new IOException("Failed to parse error record from blob query response stream.");
                }

                if (this.errorHandler != null)
                {
                    this.errorHandler.reportError(blobQueryError);
                }
                else
                {
                    throw new IOException("An error reported during blob query operation processing, but no handler was given.  Error details: " + System.lineSeparator() + blobQueryError.ToString());
                }
            }
            else
            {
                throw new IOException("Unknown record type " + record.getSchema().getName() + " in blob query response parsing.");
            }
        }

        if(this.readBuffer == null || this.readBuffer.available() == 0)
        {
            if (!this.endRecordSeen)
            {
                throw new IOException("No end record was present in the response from the blob query. This may indicate that not all data was returned.");
            }
        }
        else
        {
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
        if(this.reader != null)
        {
            this.reader.close();
        }

        super.close();
    }
}
