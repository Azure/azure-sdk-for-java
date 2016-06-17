/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Represents an uploader for a single segment of a larger file.
 */
public class SingleSegmentUploader {

    /**
     * The length of the buffers to upload (4MB).
     */
    public static final int BUFFER_LENGTH = 4 * 1024 * 1024;

    /** 4MB is the maximum length of a single extent. So if one record is longer than this,
     * then we will fast fail, since that record will cross extent boundaries.
     */
    public static final int MAX_RECORD_LENGTH = 4 * 1024 * 1024;

    /**
     * During upload retries, this indicates the maximum amount of time, in seconds, that we will wait between retries.
     */
    public static final int MAXIMUM_BACKOFF_WAIT_SECONDS = 32;

    /**
     * The maximum number of times to attempt to upload the buffer.
     */
    public static final int MAX_BUFFER_UPLOAD_ATTEMPT_COUNT = 4;

    private FrontEndAdapter frontEndAdapter;
    private UploadSegmentMetadata segmentMetadata;
    private UploadMetadata metadata;

    /**
     * Creates a new uploader for a single segment.
     *
     * @param segmentNumber The sequence number of the segment.
     * @param uploadMetadata The metadata for the entire upload.
     * @param frontEnd A pointer to the front end.
     */
    public SingleSegmentUploader(int segmentNumber, UploadMetadata uploadMetadata, FrontEndAdapter frontEnd) {
        metadata = uploadMetadata;
        segmentMetadata = uploadMetadata.getSegments()[segmentNumber];
        frontEndAdapter = frontEnd;
        this.useBackOffRetryStrategy = true;
    }

    /**
     * Gets or sets a value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * If set to 'false' every retry is handled immediately; otherwise an amount of time is waited between retries, as a function of power of 2.
     */
    private boolean useBackOffRetryStrategy;

    /**
     *
     * @return A value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * If set to 'false' every retry is handled immediately; otherwise an amount of time is waited between retries, as a function of power of 2.
     */
    public boolean useBackOffRetryStrategy() {
        return  useBackOffRetryStrategy;
    }

    /**
     *
     * @param isEnabled A value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * If set to 'false' every retry is handled immediately; otherwise an amount of time is waited between retries, as a function of power of 2.
     */
    public void setUseBackOffRetryStrategy(boolean isEnabled) {
        useBackOffRetryStrategy = isEnabled;
    }

    /**
     * Uploads the portion of the InputFilePath to the given TargetStreamPath, starting at the given StartOffset.
     * The segment is further divided into equally-sized blocks which are uploaded in sequence.
     * Each such block is attempted a certain number of times; if after that it still cannot be uploaded, the entire segment is aborted (in which case no cleanup is performed on the server).
     *
     * @throws Exception if there is any failure during the upload
     */
    public void upload() throws Exception {
        File fileInfo = new File(metadata.getInputFilePath());
        if (!(fileInfo.exists())) {
            throw new FileNotFoundException("Unable to locate input file: " + metadata.getInputFilePath());
        }

        //open up a reader from the input file, seek to the appropriate offset
        try (RandomAccessFile inputStream = openInputStream()) {
            long endPosition = segmentMetadata.getOffset() + segmentMetadata.getLength();
            if (endPosition > fileInfo.length()) {
                throw new IllegalArgumentException("StartOffset+UploadLength is beyond the end of the input file");
            }

            uploadSegmentContents(inputStream, endPosition);

            verifyUploadedStream();
            //any exceptions are (re)thrown to be handled by the caller; we do not handle retries or other recovery techniques here
        }
    }

    /**
     * Verifies the uploaded stream.
     *
     * @throws Exception if there is any failure validating the stream being uploaded.
     */
    private void verifyUploadedStream() throws Exception {
        //verify that the remote stream has the length we expected.
        int retryCount = 0;
        long remoteLength = -1;
        while (retryCount < MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
            retryCount++;
            try {
                remoteLength = frontEndAdapter.getStreamLength(segmentMetadata.getPath());
                break;
            } catch (Exception ex) {
                if (retryCount >= MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
                    throw ex;
                }

                waitForRetry(retryCount, this.useBackOffRetryStrategy);
            }
        }

        if (segmentMetadata.getLength() != remoteLength) {
            throw new UploadFailedException(MessageFormat.format("Post-upload stream verification failed: target stream has a length of {0}, expected {1}", remoteLength, segmentMetadata.getLength()));
        }
    }

    /**
     * Uploads the segment contents.
     *
     * @param inputStream The input stream.
     * @param endPosition The end position.
     * @throws Exception if there is any failure attempting to upload the contents of a single segment.
     */
    private void uploadSegmentContents(RandomAccessFile inputStream, long endPosition) throws Exception {
        long bytesCopiedSoFar = 0; // we start off with a fresh stream

        byte[] buffer = new byte[BUFFER_LENGTH];
        int residualBufferLength = 0; //the number of bytes that remained in the buffer from the last upload (bytes which were not uploaded)

        while (inputStream.getFilePointer() < endPosition) {
            //read a block of data, and keep track of how many bytes are actually read
            int bytesRead = readIntoBuffer(inputStream, buffer, residualBufferLength, endPosition);
            int bufferDataLength = residualBufferLength + bytesRead;

            //determine the cutoff offset for upload - everything before will be uploaded, everything after is residual; (the position of the last record in this buffer)
            int uploadCutoff = bufferDataLength;
            if (!metadata.isBinary()) {
                uploadCutoff = determineUploadCutoffForTextFile(buffer, bufferDataLength, inputStream);
            }

            bytesCopiedSoFar = uploadBuffer(buffer, uploadCutoff, bytesCopiedSoFar);

            residualBufferLength = bufferDataLength - uploadCutoff;
            if (residualBufferLength > 0) {
                //move the remainder of the buffer to the front
                System.arraycopy(buffer, uploadCutoff, buffer, 0, residualBufferLength);
            }
        }

        //make sure we don't leave anything behind
        if (residualBufferLength > 0) {
            uploadBuffer(buffer, residualBufferLength, bytesCopiedSoFar);
        }

        buffer = null;
    }

    /**
     * Determines the upload cutoff for text file.
     *
     * @param buffer The buffer.
     * @param bufferDataLength length of the buffer data.
     * @param inputStream The input stream.
     * @return The index within the buffer which indicates a record boundary cutoff for a single append request for a text file.
     * @throws UploadFailedException indicates that the upload failed for the specified reason.
     * @throws IOException indicates the path is inaccessible or does not exist.
     */
    private int determineUploadCutoffForTextFile(byte[] buffer, int bufferDataLength, RandomAccessFile inputStream) throws UploadFailedException, IOException {
        Charset encoding = Charset.forName(metadata.getEncodingName());
        //NOTE: we return an offset, but everywhere else below we treat it as a byte count; in order for that to work, we need to add 1 to the result of FindNewLine.
        int uploadCutoff = StringExtensions.findNewline(buffer, bufferDataLength - 1, bufferDataLength, true, encoding, metadata.getDelimiter()) + 1;
        if (uploadCutoff <= 0 && (metadata.getSegmentCount() > 1 || bufferDataLength >= MAX_RECORD_LENGTH)) {
            throw new UploadFailedException(MessageFormat.format("Found a record that exceeds the maximum allowed record length around offset {0}", inputStream.getFilePointer()));
        }

        //a corner case here is when the newline is 2 chars long, and the first of those lands on the last byte of the buffer. If so, let's try to find another
        //newline inside the buffer, because we might be splitting this wrongly.
        if ((metadata.getDelimiter() == null || StringUtils.isEmpty(metadata.getDelimiter())) && uploadCutoff == buffer.length && buffer[buffer.length - 1] == (byte) '\r') {
            int newCutoff = StringExtensions.findNewline(buffer, bufferDataLength - 2, bufferDataLength - 1, true, encoding, metadata.getDelimiter()) + 1;
            if (newCutoff > 0) {
                uploadCutoff = newCutoff;
            }
        }

        return uploadCutoff;
    }

    /**
     * Uploads the buffer.
     *
     * @param buffer The buffer.
     * @param bytesToCopy The bytes to copy.
     * @param targetStreamOffset The target stream offset.
     * @return The current index within the target stream after uploading the buffer.
     * @throws Exception Thrown if there is a failure uploading the current buffer.
     */
    private long uploadBuffer(byte[] buffer, int bytesToCopy, long targetStreamOffset) throws Exception {
        //append it to the remote stream
        int attemptCount = 0;
        boolean uploadCompleted = false;
        while (!uploadCompleted && attemptCount < MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
            attemptCount++;
            try {
                if (targetStreamOffset == 0) {
                    frontEndAdapter.createStream(segmentMetadata.getPath(), true, buffer, bytesToCopy);
                } else {
                    frontEndAdapter.appendToStream(segmentMetadata.getPath(), buffer, targetStreamOffset, bytesToCopy);

                }

                uploadCompleted = true;
                targetStreamOffset += bytesToCopy;
            } catch (Exception ex) {
                //if we tried more than the number of times we were allowed to, give up and throw the exception
                if (attemptCount >= MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
                    throw ex;
                } else {
                    waitForRetry(attemptCount, this.useBackOffRetryStrategy);
                }
            }
        }

        return targetStreamOffset;
    }

    /**
     * Reads the data into the buffer.
     *
     * @param inputStream The stream to read data from.
     * @param buffer The buffer to read data into
     * @param bufferOffset The offset in the buffer to begin pushing data
     * @param streamEndPosition The last point in the stream to read.
     * @return The number of bytes read into the buffer.
     * @throws IOException Thrown if there is an issue accessing the stream or the pointer to the file.
     */
    private int readIntoBuffer(RandomAccessFile inputStream, byte[] buffer, int bufferOffset, long streamEndPosition) throws IOException {
        //read a block of data
        int bytesToRead = buffer.length - bufferOffset;
        if (bytesToRead > streamEndPosition - inputStream.getFilePointer()) {
            //last read may be smaller than previous reads; readjust # of bytes to read accordingly
            bytesToRead = (int) (streamEndPosition - inputStream.getFilePointer());
        }

        int remainingBytes = bytesToRead;

        while (remainingBytes > 0) {
            //Stream.Read may not read all the bytes we requested, so we need to retry until we filled up the entire buffer
            int bytesRead = inputStream.read(buffer, bufferOffset, remainingBytes);
            bufferOffset += bytesRead;
            remainingBytes = bytesToRead - bufferOffset;
        }

        return bytesToRead;
    }

    /**
     * Enables use of a back off retry strategy, allowing a caller to wait before attempting an action again.
     *
     * @param attemptCount The number of attempts that have already been done
     * @param useBackOffRetryStrategy whether to use the back off strategy or not.
     * @throws InterruptedException Thrown if there is an interrupt during the sleep.
     */
    public static void waitForRetry(int attemptCount, boolean useBackOffRetryStrategy) throws InterruptedException {
        if (!useBackOffRetryStrategy) {
            //no need to wait
            return;
        }

        int intervalSeconds = Math.max(MAXIMUM_BACKOFF_WAIT_SECONDS, (int) Math.pow(2, attemptCount));
        Thread.sleep(intervalSeconds * 1000);
    }

    /**
     * Opens the input stream.
     * @return A {@link RandomAccessFile} stream of the file being uploaded.
     * @throws IOException Thrown if the input stream cannot be opened due to file accessibility or existence.
     */
    private RandomAccessFile openInputStream() throws IOException {
        RandomAccessFile stream = new RandomAccessFile(metadata.getInputFilePath(), "r");

        if (segmentMetadata.getOffset() >= stream.length()) {
            throw new IllegalArgumentException("StartOffset is beyond the end of the input file");
        }

        // always seek from the beginning of the file
        stream.seek(0);
        stream.seek(segmentMetadata.getOffset());
        return stream;
    }
}
