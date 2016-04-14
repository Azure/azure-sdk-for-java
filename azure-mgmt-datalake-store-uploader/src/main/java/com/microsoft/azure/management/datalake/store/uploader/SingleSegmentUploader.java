/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.CloudException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Created by begoldsm on 4/12/2016.
 */
public class SingleSegmentUploader {
    public static final int BufferLength = 4 * 1024 * 1024;

    // 4MB is the maximum length of a single extent. So if one record is longer than this,
    // then we will fast fail, since that record will cross extent boundaries.
    public static final int MaxRecordLength = 4 * 1024 * 1024;
    public static final int MaximumBackoffWaitSeconds = 32;
    public static final int MaxBufferUploadAttemptCount = 4;

    private FrontEndAdapter _frontEnd;
    // private readonly IProgress<SegmentUploadProgress> _progressTracker;
    // private readonly CancellationToken _token;
    private UploadSegmentMetadata _segmentMetadata;
    private UploadMetadata _metadata;

    /// <summary>
    /// Creates a new uploader for a single segment.
    /// </summary>
    /// <param name="segmentNumber">The sequence number of the segment.</param>
    /// <param name="uploadMetadata">The metadata for the entire upload.</param>
    /// <param name="frontEnd">A pointer to the front end.</param>
    /// <param name="token">The cancellation token to use</param>
    /// <param name="progressTracker">(Optional) A tracker to report progress on this segment.</param>
    public SingleSegmentUploader(int segmentNumber, UploadMetadata uploadMetadata, FrontEndAdapter frontEnd) {
        _metadata = uploadMetadata;
        _segmentMetadata = uploadMetadata.Segments[segmentNumber];
        _frontEnd = frontEnd;
        this.UseBackOffRetryStrategy = true;
    }

    /// <summary>
    /// Gets or sets a value indicating whether to use a back-off (exponenential) in case of individual block failures.
    /// If set to 'false' every retry is handled immediately; otherwise an amount of time is waited between retries, as a function of power of 2.
    /// </summary>
    public boolean UseBackOffRetryStrategy;

    /// <summary>
    /// Uploads the portion of the InputFilePath to the given TargetStreamPath, starting at the given StartOffset.
    /// The segment is further divided into equally-sized blocks which are uploaded in sequence.
    /// Each such block is attempted a certain number of times; if after that it still cannot be uploaded, the entire segment is aborted (in which case no cleanup is performed on the server).
    /// </summary>
    /// <returns></returns>
    public void Upload() throws IOException, InterruptedException, CloudException, UploadFailedException {
        File fileInfo = new File(_metadata.InputFilePath);
        if (!(fileInfo.exists())) {
            throw new FileNotFoundException("Unable to locate input file: " + _metadata.InputFilePath);
        }

        //open up a reader from the input file, seek to the appropriate offset
        try (EnhancedFileInputStream inputStream = OpenInputStream()) {
            long endPosition = _segmentMetadata.Offset + _segmentMetadata.Length;
            if (endPosition > fileInfo.length()) {
                throw new IllegalArgumentException("StartOffset+UploadLength is beyond the end of the input file");
            }

            UploadSegmentContents(inputStream, endPosition);

            VerifyUploadedStream();
            //any exceptions are (re)thrown to be handled by the caller; we do not handle retries or other recovery techniques here
        }
    }

    /// <summary>
    /// Verifies the uploaded stream.
    /// </summary>
    /// <exception cref="UploadFailedException"></exception>
    private void VerifyUploadedStream() throws UploadFailedException, IOException, CloudException, InterruptedException {
        //verify that the remote stream has the length we expected.
        int retryCount = 0;
        long remoteLength = -1;
        while (retryCount < MaxBufferUploadAttemptCount) {
            retryCount++;
            try {
                remoteLength = _frontEnd.GetStreamLength(_segmentMetadata.Path);
                break;
            } catch (Exception ex) {
                if (retryCount >= MaxBufferUploadAttemptCount) {
                    throw ex;
                }

                WaitForRetry(retryCount, this.UseBackOffRetryStrategy);
            }
        }

        if (_segmentMetadata.Length != remoteLength) {
            throw new UploadFailedException(MessageFormat.format("Post-upload stream verification failed: target stream has a length of {0}, expected {1}", remoteLength, _segmentMetadata.Length));
        }
    }

    /// <summary>
    /// Uploads the segment contents.
    /// </summary>
    /// <param name="inputStream">The input stream.</param>
    /// <param name="endPosition">The end position.</param>
    private void UploadSegmentContents(EnhancedFileInputStream inputStream, long endPosition) throws InterruptedException, CloudException, IOException, UploadFailedException {
        long bytesCopiedSoFar = 0; // we start off with a fresh stream

        byte[] buffer = new byte[BufferLength];
        int residualBufferLength = 0; //the number of bytes that remained in the buffer from the last upload (bytes which were not uploaded)

        while (inputStream.getPosition() < endPosition) {
            //read a block of data, and keep track of how many bytes are actually read
            int bytesRead = ReadIntoBuffer(inputStream, buffer, residualBufferLength, endPosition);
            int bufferDataLength = residualBufferLength + bytesRead;

            //determine the cutoff offset for upload - everything before will be uploaded, everything after is residual; (the position of the last record in this buffer)
            int uploadCutoff = bufferDataLength;
            if (!_metadata.IsBinary) {
                uploadCutoff = DetermineUploadCutoffForTextFile(buffer, bufferDataLength, inputStream);
            }

            bytesCopiedSoFar = UploadBuffer(buffer, uploadCutoff, bytesCopiedSoFar);

            residualBufferLength = bufferDataLength - uploadCutoff;
            if (residualBufferLength > 0) {
                //move the remainder of the buffer to the front
                System.arraycopy(buffer, uploadCutoff, buffer, 0, residualBufferLength);
            }
        }

        //make sure we don't leave anything behind
        if (residualBufferLength > 0) {
            UploadBuffer(buffer, residualBufferLength, bytesCopiedSoFar);
        }

        buffer = null;
    }

    /// <summary>
    /// Determines the upload cutoff for text file.
    /// </summary>
    /// <param name="buffer">The buffer.</param>
    /// <param name="bufferDataLength">Length of the buffer data.</param>
    /// <param name="inputStream">The input stream.</param>
    /// <returns></returns>
    /// <exception cref="UploadFailedException"></exception>
    private int DetermineUploadCutoffForTextFile(byte[] buffer, int bufferDataLength, EnhancedFileInputStream inputStream) throws UploadFailedException {
        Charset encoding = Charset.forName(_metadata.EncodingCodePage);
        //NOTE: we return an offset, but everywhere else below we treat it as a byte count; in order for that to work, we need to add 1 to the result of FindNewLine.
        int uploadCutoff = StringExtensions.FindNewline(buffer, bufferDataLength - 1, bufferDataLength, true, encoding, _metadata.Delimiter) + 1;
        if (uploadCutoff <= 0 && (_metadata.SegmentCount > 1 || bufferDataLength >= MaxRecordLength)) {
            throw new UploadFailedException(MessageFormat.format("Found a record that exceeds the maximum allowed record length around offset {0}", inputStream.getPosition()));
        }

        //a corner case here is when the newline is 2 chars long, and the first of those lands on the last byte of the buffer. If so, let's try to find another
        //newline inside the buffer, because we might be splitting this wrongly.
        if ((_metadata.Delimiter == null || StringUtils.isEmpty(_metadata.Delimiter)) && uploadCutoff == buffer.length && buffer[buffer.length - 1] == (byte) '\r') {
            int newCutoff = StringExtensions.FindNewline(buffer, bufferDataLength - 2, bufferDataLength - 1, true, encoding, _metadata.Delimiter) + 1;
            if (newCutoff > 0) {
                uploadCutoff = newCutoff;
            }
        }

        return uploadCutoff;
    }

    /// <summary>
    /// Uploads the buffer.
    /// </summary>
    /// <param name="buffer">The buffer.</param>
    /// <param name="bytesToCopy">The bytes to copy.</param>
    /// <param name="targetStreamOffset">The target stream offset.</param>
    /// <returns></returns>
    private long UploadBuffer(byte[] buffer, int bytesToCopy, long targetStreamOffset) throws IOException, CloudException, InterruptedException {
        //append it to the remote stream
        int attemptCount = 0;
        boolean uploadCompleted = false;
        while (!uploadCompleted && attemptCount < MaxBufferUploadAttemptCount) {
            attemptCount++;
            try {
                if (targetStreamOffset == 0) {
                    _frontEnd.CreateStream(_segmentMetadata.Path, true, buffer, bytesToCopy);
                } else {
                    _frontEnd.AppendToStream(_segmentMetadata.Path, buffer, targetStreamOffset, bytesToCopy);

                }

                uploadCompleted = true;
                targetStreamOffset += bytesToCopy;
            } catch (Exception ex) {
                //if we tried more than the number of times we were allowed to, give up and throw the exception
                if (attemptCount >= MaxBufferUploadAttemptCount) {
                    throw ex;
                } else {
                    WaitForRetry(attemptCount, this.UseBackOffRetryStrategy);
                }
            }
        }

        return targetStreamOffset;
    }

    /// <summary>
    /// Reads the into buffer.
    /// </summary>
    /// <param name="inputStream">The input stream.</param>
    /// <param name="buffer">The buffer.</param>
    /// <param name="bufferOffset">The buffer offset.</param>
    /// <param name="streamEndPosition">The stream end position.</param>
    /// <returns></returns>
    private int ReadIntoBuffer(EnhancedFileInputStream inputStream, byte[] buffer, int bufferOffset, long streamEndPosition) throws IOException {
        //read a block of data
        int bytesToRead = buffer.length - bufferOffset;
        if (bytesToRead > streamEndPosition - inputStream.getPosition()) {
            //last read may be smaller than previous reads; readjust # of bytes to read accordingly
            bytesToRead = (int) (streamEndPosition - inputStream.getPosition());
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

    /// <summary>
    /// Waits for retry.
    /// </summary>
    /// <param name="attemptCount">The attempt count.</param>
    public static void WaitForRetry(int attemptCount, boolean useBackOffRetryStrategy) throws InterruptedException {
        if (!useBackOffRetryStrategy) {
            //no need to wait
            return;
        }

        int intervalSeconds = Math.max(MaximumBackoffWaitSeconds, (int) Math.pow(2, attemptCount));
        Thread.sleep(intervalSeconds * 1000);
    }

    /// <summary>
    /// Opens the input stream.
    /// </summary>
    /// <returns></returns>
    /// <exception cref="System.ArgumentException">StartOffset is beyond the end of the input file;StartOffset</exception>
    private EnhancedFileInputStream OpenInputStream() throws IOException {
        EnhancedFileInputStream stream = new EnhancedFileInputStream(_metadata.InputFilePath);

        if (_segmentMetadata.Offset >= stream.getLength()) {
            throw new IllegalArgumentException("StartOffset is beyond the end of the input file");
        }

        stream.skip(_segmentMetadata.Offset);
        return stream;
    }
}
