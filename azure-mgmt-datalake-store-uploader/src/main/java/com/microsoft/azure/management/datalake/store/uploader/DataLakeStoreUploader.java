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
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.OperationsException;

/**
 * Created by begoldsm on 4/13/2016.
 */
public class DataLakeStoreUploader {
    /// <summary>
    /// The maximum number of parallel threads to allow.
    /// </summary>
    public static final int MaxAllowedThreads = 1024;
    private FrontEndAdapter _frontEnd;
    private String _metadataFilePath;
    private int _previousDefaultConnectionLimit;

    /// <summary>
    /// Creates a new instance of the DataLakeUploader class, by specifying a pointer to the FrontEnd to use for the upload.
    /// </summary>
    /// <param name="uploadParameters">The Upload Parameters to use.</param>
    /// <param name="frontEnd">A pointer to the FrontEnd interface to use for the upload.</param>
    /// <param name="progressTracker">(Optional) A tracker that reports progress on the upload.</param>
    public DataLakeStoreUploader(UploadParameters uploadParameters, FrontEndAdapter frontEnd) throws FileNotFoundException {
        this.Parameters = uploadParameters;
        _frontEnd = frontEnd;

        //ensure that input parameters are correct
        ValidateParameters();
        _metadataFilePath = GetCanonicalMetadataFilePath();
    }

    /// <summary>
    /// Gets the canonical metadata file path.
    /// </summary>
    /// <returns></returns>
    private String GetCanonicalMetadataFilePath() {
        return Paths.get(this.Parameters.getLocalMetadataLocation(), MessageFormat.format("{0}.upload.xml", Paths.get(this.Parameters.getInputFilePath()).getFileName())).toString();
    }

    /// <summary>
    /// Gets the parameters to use for this upload.
    /// </summary>
    public UploadParameters Parameters;

    /// <summary>
    /// Executes the upload as defined by the input parameters.
    /// </summary>
    public void Execute() throws Exception {
        //load up existing metadata or create a fresh one
        UploadMetadata metadata = GetMetadata();

        if (metadata.SegmentCount < this.Parameters.getThreadCount()) {
            // reducing the thread count to make it equal to the segment count
            // if it is larger, since those extra threads will not be used.
            this.Parameters.setThreadCount(metadata.SegmentCount);
        }

        //begin (or resume) uploading the file
        UploadFile(metadata);

        //clean up metadata after a successful upload
        metadata.DeleteFile();
    }

    /// <summary>
    /// Validates the parameters.
    /// </summary>
    /// <exception cref="System.IO.FileNotFoundException">Could not find input file</exception>
    /// <exception cref="System.ArgumentNullException">
    /// TargetStreamPath;Null or empty Target Stream Path
    /// or
    /// AccountName;Null or empty Account Name
    /// </exception>
    /// <exception cref="System.ArgumentException">Invalid TargetStreamPath, a stream path should not end with /</exception>
    /// <exception cref="System.ArgumentOutOfRangeException">ThreadCount</exception>
    private void ValidateParameters() throws FileNotFoundException {
        if (!(new File(this.Parameters.getInputFilePath()).exists())) {
            throw new FileNotFoundException("Could not find input file: " + this.Parameters.getInputFilePath());
        }

        if (this.Parameters.getTargetStreamPath() == null || StringUtils.isEmpty(this.Parameters.getTargetStreamPath())) {
            throw new IllegalArgumentException("Null or empty Target Stream Path");
        }

        if (this.Parameters.getTargetStreamPath().endsWith("/")) {
            throw new IllegalArgumentException("Invalid TargetStreamPath, a stream path should not end with /");
        }

        if (this.Parameters.getAccountName() == null || StringUtils.isEmpty(this.Parameters.getAccountName())) {
            throw new IllegalArgumentException("Null or empty Account Name");
        }

        if (this.Parameters.getThreadCount() < 1 || this.Parameters.getThreadCount() > MaxAllowedThreads) {
            throw new IllegalArgumentException(MessageFormat.format("ThreadCount must be at least 1 and at most {0}", MaxAllowedThreads));
        }
    }

    /// <summary>
    /// Gets the metadata.
    /// </summary>
    /// <returns></returns>
    private UploadMetadata GetMetadata() throws IOException, InvalidMetadataException, UploadFailedException {
        UploadMetadataGenerator metadataGenerator = new UploadMetadataGenerator(this.Parameters);
        if (this.Parameters.isResume()) {
            return metadataGenerator.GetExistingMetadata(_metadataFilePath);
        } else {
            return metadataGenerator.CreateNewMetadata(_metadataFilePath);
        }
    }

    /// <summary>
    /// Deletes the metadata file from disk.
    /// </summary>
    public void DeleteMetadataFile() {
        File toDelete = new File(_metadataFilePath);
        if (toDelete.exists()) {
            toDelete.delete();
        }
    }

    /// <summary>
    /// Validates that the metadata is valid for a resume operation, and also updates the internal Segment States to match what the Server looks like.
    /// If any changes are made, the metadata will be saved to its canonical location.
    /// </summary>
    /// <param name="metadata"></param>
    private void ValidateMetadataForResume(UploadMetadata metadata) throws Exception {
        ValidateMetadataMatchesLocalFile(metadata);

        //verify that the target stream does not already exist (in case we don't want to overwrite)
        if (!this.Parameters.isOverwrite() && _frontEnd.StreamExists(metadata.TargetStreamPath)) {
            throw new OperationsException("Target Stream already exists");
        }

        //make sure we don't upload part of the file as binary, while the rest is non-binary (that's just asking for trouble)
        if (this.Parameters.isBinary() != metadata.IsBinary) {
            throw new OperationsException(
                    MessageFormat.format(
                            "Existing metadata was created for a {0}binary file while the current parameters requested a {1}binary upload.",
                            metadata.IsBinary ? "" : "non-",
                            this.Parameters.isBinary() ? "" : "non-"));
        }

        //see what files(segments) already exist - update metadata accordingly (only for segments that are missing from server; if it's on the server but not in metadata, reupload)
        for (UploadSegmentMetadata segment : metadata.Segments) {
            if (segment.Status == SegmentUploadStatus.Complete) {
                int retryCount = 0;
                while (retryCount < SingleSegmentUploader.MaxBufferUploadAttemptCount) {
                    retryCount++;
                    try {
                        //verify that the stream exists and that the length is as expected
                        if (!_frontEnd.StreamExists(segment.Path)) {
                            // this segment was marked as completed, but no target stream exists; it needs to be reuploaded
                            segment.Status = SegmentUploadStatus.Pending;
                        } else {
                            long remoteLength = _frontEnd.GetStreamLength(segment.Path);
                            if (remoteLength != segment.Length) {
                                //the target stream has a different length than the input segment, which implies they are inconsistent; it needs to be reuploaded
                                segment.Status = SegmentUploadStatus.Pending;
                            }
                        }

                        break;
                    } catch (Exception e) {
                        if (retryCount >= SingleSegmentUploader.MaxBufferUploadAttemptCount) {
                            throw new UploadFailedException(
                                    MessageFormat.format(
                                            "Cannot validate metadata in order to resume due to the following exception retrieving file information: {0}",
                                            e));
                        }

                        SingleSegmentUploader.WaitForRetry(retryCount, Parameters.isUseSegmentBlockBackOffRetryStrategy());
                    }
                }
            } else {
                //anything which is not in 'Completed' status needs to be reuploaded
                segment.Status = SegmentUploadStatus.Pending;
            }
        }
        metadata.Save();
    }

    /// <summary>
    /// Verifies that the metadata is valid for a fresh upload.
    /// </summary>
    /// <param name="metadata"></param>
    private void ValidateMetadataForFreshUpload(UploadMetadata metadata) throws Exception {
        ValidateMetadataMatchesLocalFile(metadata);

        //verify that the target stream does not already exist (in case we don't want to overwrite)
        if (!this.Parameters.isOverwrite() && _frontEnd.StreamExists(metadata.TargetStreamPath)) {
            throw new OperationsException("Target Stream already exists");
        }
    }

    /// <summary>
    /// Verifies that the metadata is consistent with the local file information.
    /// </summary>
    /// <param name="metadata"></param>
    private void ValidateMetadataMatchesLocalFile(UploadMetadata metadata) throws OperationsException {
        if (!metadata.TargetStreamPath.trim().equalsIgnoreCase(this.Parameters.getTargetStreamPath().trim())) {
            throw new OperationsException("Metadata points to a different target stream than the input parameters");
        }

        //verify that it matches against local file (size, name)
        File metadataInputFileInfo = new File(metadata.InputFilePath);
        File paramInputFileInfo = new File(this.Parameters.getInputFilePath());

        if (!paramInputFileInfo.toString().toLowerCase().equals(metadataInputFileInfo.toString().toLowerCase())) {
            throw new OperationsException("The metadata refers to different file than the one requested");
        }

        if (!metadataInputFileInfo.exists()) {
            throw new OperationsException("The metadata refers to a file that does not exist");
        }

        if (metadata.FileLength != metadataInputFileInfo.length()) {
            throw new OperationsException("The metadata's file information differs from the actual file");
        }
    }

    /// <summary>
    /// Uploads the file using the given metadata.
    ///
    /// </summary>
    /// <param name="metadata"></param>
    private void UploadFile(UploadMetadata metadata) throws Exception {
        try {
            //TODO: figure out if we need a ServicePointManager equivalent for the connection limit
            //match up the metadata with the information on the server
            if (this.Parameters.isResume()) {
                ValidateMetadataForResume(metadata);
            } else {
                ValidateMetadataForFreshUpload(metadata);
            }

            // TODO: figure out if we need a way to track progress.
            if (metadata.SegmentCount == 0) {
                // simply create the target stream, overwriting existing streams if they exist
                _frontEnd.CreateStream(metadata.TargetStreamPath, true, null, 0);
            } else if (metadata.SegmentCount > 1) {
                //perform the multi-segment upload
                MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, this.Parameters.getThreadCount(), _frontEnd);
                msu.UseSegmentBlockBackOffRetryStrategy = this.Parameters.isUseSegmentBlockBackOffRetryStrategy();
                msu.Upload();

                //concatenate the files at the end
                ConcatenateSegments(metadata);
            } else {
                //optimization if we only have one segment: upload it directly to the target stream
                metadata.Segments[0].Path = metadata.TargetStreamPath;
                SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, _frontEnd);
                ssu.UseBackOffRetryStrategy = this.Parameters.isUseSegmentBlockBackOffRetryStrategy();
                ssu.Upload();
            }
        } catch (InterruptedException ex) {
            // do nothing since we have already marked everything as failed
        }
    }

    /// <summary>
    /// Concatenates all the segments defined in the metadata into a single stream.
    /// </summary>
    /// <param name="metadata"></param>
    private void ConcatenateSegments(final UploadMetadata metadata) throws Exception {
        final String[] inputPaths = new String[metadata.SegmentCount];

        //verify if target stream exists
        if (_frontEnd.StreamExists(metadata.TargetStreamPath)) {
            if (this.Parameters.isOverwrite()) {
                _frontEnd.DeleteStream(metadata.TargetStreamPath, false);
            } else {
                throw new OperationsException("Target Stream already exists");
            }
        }

        //ensure all input streams exist and are of the expected length
        //ensure all segments in the metadata are marked as 'complete'
        final List<Exception> exceptions = new ArrayList<>();
        ExecutorService exec = Executors.newFixedThreadPool(this.Parameters.getThreadCount());
        for (int i = 0; i < metadata.SegmentCount; i++) {
            final int finalI = i;
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (metadata.Segments[finalI].Status != SegmentUploadStatus.Complete) {
                            throw new UploadFailedException("Cannot perform 'Concatenate' operation because not all streams are fully uploaded.");
                        }

                        String remoteStreamPath = metadata.Segments[finalI].Path;
                        int retryCount = 0;
                        long remoteLength = -1;

                        while (retryCount < SingleSegmentUploader.MaxBufferUploadAttemptCount) {
                            retryCount++;
                            try {
                                remoteLength = _frontEnd.GetStreamLength(remoteStreamPath);
                                break;
                            } catch (Exception e) {
                                if (retryCount >= SingleSegmentUploader.MaxBufferUploadAttemptCount) {
                                    throw new UploadFailedException(
                                            MessageFormat.format(
                                                    "Cannot perform 'Concatenate' operation due to the following exception retrieving file information: {0}",
                                                    e));
                                }

                                SingleSegmentUploader.WaitForRetry(retryCount, Parameters.isUseSegmentBlockBackOffRetryStrategy());
                            }
                        }


                        if (remoteLength != metadata.Segments[finalI].Length) {
                            throw new UploadFailedException(MessageFormat.format("Cannot perform 'Concatenate' operation because segment {0} has an incorrect length (expected {1}, actual {2}).", finalI, metadata.Segments[finalI].Length, remoteLength));
                        }

                        inputPaths[finalI] = remoteStreamPath;
                    } catch (Exception ex) {
                        //collect any exceptions, whether we just generated them above or whether they come from the Front End,
                        exceptions.add(ex);
                    }
                }
            });
        }

        if (exceptions.size() > 0) {
            throw new AggregateUploadException("At least one concatenate test failed", exceptions.remove(0), exceptions);
        }

        //issue the command
        _frontEnd.Concatenate(metadata.TargetStreamPath, inputPaths);
    }
}
