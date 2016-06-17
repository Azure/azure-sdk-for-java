/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.StringUtils;

import javax.management.OperationsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Represents a general purpose file uploader into DataLake. Supports the efficient upload of large files.
 */
public class DataLakeStoreUploader {

    /**
     * The maximum number of parallel threads to allow.
     */
    public static final int MAX_ALLOWED_THREADS = 1024;
    private FrontEndAdapter frontEnd;
    private String metadataFilePath;

    /**
     * Creates a new instance of the DataLakeUploader class, by specifying a pointer to the FrontEnd to use for the upload.
     *
     * @param uploadParameters The upload parameters to use.
     * @param frontEnd A pointer to the FrontEnd interface to use for the upload.
     * @throws FileNotFoundException if the local file cannot be found or is inaccessible
     */
    public DataLakeStoreUploader(UploadParameters uploadParameters, FrontEndAdapter frontEnd) throws FileNotFoundException {
        this.parameters = uploadParameters;
        this.frontEnd = frontEnd;

        //ensure that input parameters are correct
        validateParameters();
        metadataFilePath = getCanonicalMetadataFilePath();
    }

    /**
     * Gets the canonical metadata file path.
     *
     * @return A string representation of the canonical metadata file path.
     */
    private String getCanonicalMetadataFilePath() {
        return Paths.get(this.getParameters().getLocalMetadataLocation(), MessageFormat.format("{0}.upload.xml", Paths.get(this.getParameters().getInputFilePath()).getFileName())).toString();
    }

    private UploadParameters parameters;

    /**
     * Gets the parameters to use for this upload.
     * @return the parameters for this upload.
     */
    public UploadParameters getParameters() {
        return parameters;
    }
    /**
     * Executes the upload as defined by the input parameters.
     *
     * @throws Exception if there is any failure that occurs during execution.
     */
    public void execute() throws Exception {
        //load up existing metadata or create a fresh one
        UploadMetadata metadata = getMetadata();

        if (metadata.getSegmentCount() < this.getParameters().getThreadCount()) {
            // reducing the thread count to make it equal to the segment count
            // if it is larger, since those extra threads will not be used.
            this.getParameters().setThreadCount(metadata.getSegmentCount());
        }

        //begin (or resume) uploading the file
        uploadFile(metadata);

        //clean up metadata after a successful upload
        metadata.deleteFile();
    }

    /**
     * Validates the parameters.
     *
     * @throws FileNotFoundException Could not find input file
     * @throws IllegalArgumentException Null or empty account name, stream path should not end with a '/' or the thread count is out of range.
     */
    private void validateParameters() throws FileNotFoundException, IllegalArgumentException {
        if (!(new File(this.getParameters().getInputFilePath()).exists())) {
            throw new FileNotFoundException("Could not find input file: " + this.getParameters().getInputFilePath());
        }

        if (this.getParameters().getTargetStreamPath() == null || StringUtils.isEmpty(this.getParameters().getTargetStreamPath())) {
            throw new IllegalArgumentException("Null or empty Target Stream path");
        }

        if (this.getParameters().getTargetStreamPath().endsWith("/")) {
            throw new IllegalArgumentException("Invalid TargetStreamPath, a stream path should not end with /");
        }

        if (this.getParameters().getAccountName() == null || StringUtils.isEmpty(this.getParameters().getAccountName())) {
            throw new IllegalArgumentException("Null or empty Account Name");
        }

        if (this.getParameters().getThreadCount() < 1 || this.getParameters().getThreadCount() > MAX_ALLOWED_THREADS) {
            throw new IllegalArgumentException(MessageFormat.format("ThreadCount must be at least 1 and at most {0}", MAX_ALLOWED_THREADS));
        }
    }

    /**
     * Gets the metadata.
     *
     * @return The {@link UploadMetadata} used by this upload.
     * @throws IOException
     * @throws InvalidMetadataException
     * @throws UploadFailedException
     */
    private UploadMetadata getMetadata() throws IOException, InvalidMetadataException, UploadFailedException {
        UploadMetadataGenerator metadataGenerator = new UploadMetadataGenerator(this.parameters);
        if (this.getParameters().isResume()) {
            return metadataGenerator.getExistingMetadata(metadataFilePath);
        } else {
            return metadataGenerator.createNewMetadata(metadataFilePath);
        }
    }

    /**
     * Deletes the metadata file from disk.
     */
    public void deleteMetadataFile() {
        File toDelete = new File(metadataFilePath);
        if (toDelete.exists()) {
            toDelete.delete();
        }
    }

    /**
     * Validates that the metadata is valid for a resume operation, and also updates the internal Segment States to match what the Server looks like.
     * If any changes are made, the metadata will be saved to its canonical location.
     *
     * @param metadata The {@link UploadMetadata} to resume the upload from.
     * @throws Exception
     */
    private void validateMetadataForResume(UploadMetadata metadata) throws Exception {
        validateMetadataMatchesLocalFile(metadata);

        //verify that the target stream does not already exist (in case we don't want to overwrite)
        if (!this.getParameters().isOverwrite() && frontEnd.streamExists(metadata.getTargetStreamPath())) {
            throw new OperationsException("Target Stream already exists");
        }

        //make sure we don't upload part of the file as binary, while the rest is non-binary (that's just asking for trouble)
        if (this.getParameters().isBinary() != metadata.isBinary()) {
            throw new OperationsException(
                    MessageFormat.format(
                            "Existing metadata was created for a {0}binary file while the current parameters requested a {1}binary upload.",
                            metadata.isBinary() ? "" : "non-",
                            this.getParameters().isBinary() ? "" : "non-"));
        }

        //see what files(segments) already exist - update metadata accordingly (only for segments that are missing from server; if it's on the server but not in metadata, reupload)
        for (UploadSegmentMetadata segment : metadata.getSegments()) {
            if (segment.getStatus() == SegmentUploadStatus.Complete) {
                int retryCount = 0;
                while (retryCount < SingleSegmentUploader.MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
                    retryCount++;
                    try {
                        //verify that the stream exists and that the length is as expected
                        if (!frontEnd.streamExists(segment.getPath())) {
                            // this segment was marked as completed, but no target stream exists; it needs to be reuploaded
                            segment.setStatus(SegmentUploadStatus.Pending);
                        } else {
                            long remoteLength = frontEnd.getStreamLength(segment.getPath());
                            if (remoteLength != segment.getLength()) {
                                //the target stream has a different length than the input segment, which implies they are inconsistent; it needs to be reuploaded
                                segment.setStatus(SegmentUploadStatus.Pending);
                            }
                        }

                        break;
                    } catch (Exception e) {
                        if (retryCount >= SingleSegmentUploader.MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
                            throw new UploadFailedException(
                                    MessageFormat.format(
                                            "Cannot validate metadata in order to resume due to the following exception retrieving file information: {0}",
                                            e));
                        }

                        SingleSegmentUploader.waitForRetry(retryCount, parameters.isUseSegmentBlockBackOffRetryStrategy());
                    }
                }
            } else {
                //anything which is not in 'Completed' status needs to be reuploaded
                segment.setStatus(SegmentUploadStatus.Pending);
            }
        }
        metadata.save();
    }

    /**
     * Verifies that the metadata is valid for a fresh upload.
     *
     * @param metadata {@link UploadMetadata} to validate for a fresh upload.
     * @throws Exception
     */
    private void validateMetadataForFreshUpload(UploadMetadata metadata) throws Exception {
        validateMetadataMatchesLocalFile(metadata);

        //verify that the target stream does not already exist (in case we don't want to overwrite)
        if (!this.getParameters().isOverwrite() && frontEnd.streamExists(metadata.getTargetStreamPath())) {
            throw new OperationsException("Target Stream already exists");
        }
    }

    /**
     * Verifies that the metadata is consistent with the local file information.
     *
     * @param metadata The {@link UploadMetadata} to check against a serialized copy.
     * @throws OperationsException
     */
    private void validateMetadataMatchesLocalFile(UploadMetadata metadata) throws OperationsException {
        if (!metadata.getTargetStreamPath().trim().equalsIgnoreCase(this.getParameters().getTargetStreamPath().trim())) {
            throw new OperationsException("Metadata points to a different target stream than the input parameters");
        }

        //verify that it matches against local file (size, name)
        File metadataInputFileInfo = new File(metadata.getInputFilePath());
        File paramInputFileInfo = new File(this.getParameters().getInputFilePath());

        if (!paramInputFileInfo.toString().toLowerCase().equals(metadataInputFileInfo.toString().toLowerCase())) {
            throw new OperationsException("The metadata refers to different file than the one requested");
        }

        if (!metadataInputFileInfo.exists()) {
            throw new OperationsException("The metadata refers to a file that does not exist");
        }

        if (metadata.getFileLength() != metadataInputFileInfo.length()) {
            throw new OperationsException("The metadata's file information differs from the actual file");
        }
    }

    /**
     * Uploads the file using the given metadata.
     * @param metadata The {@link UploadMetadata} to use to upload the file.
     * @throws Exception
     */
    private void uploadFile(UploadMetadata metadata) throws Exception {
        try {
            //TODO: figure out if we need a ServicePointManager equivalent for the connection limit
            //match up the metadata with the information on the server
            if (this.getParameters().isResume()) {
                validateMetadataForResume(metadata);
            } else {
                validateMetadataForFreshUpload(metadata);
            }

            // TODO: figure out if we need a way to track progress.
            if (metadata.getSegmentCount() == 0) {
                // simply create the target stream, overwriting existing streams if they exist
                frontEnd.createStream(metadata.getTargetStreamPath(), true, null, 0);
            } else if (metadata.getSegmentCount() > 1) {
                //perform the multi-segment upload
                MultipleSegmentUploader msu = new MultipleSegmentUploader(metadata, this.getParameters().getThreadCount(), frontEnd);
                msu.setUseSegmentBlockBackOffRetryStrategy(this.getParameters().isUseSegmentBlockBackOffRetryStrategy());
                msu.upload();

                //concatenate the files at the end
                concatenateSegments(metadata);
            } else {
                //optimization if we only have one segment: upload it directly to the target stream
                UploadSegmentMetadata[] toUse = metadata.getSegments();
                toUse[0].setPath(metadata.getTargetStreamPath());
                metadata.setSegments(toUse);
                SingleSegmentUploader ssu = new SingleSegmentUploader(0, metadata, frontEnd);
                ssu.setUseBackOffRetryStrategy(this.getParameters().isUseSegmentBlockBackOffRetryStrategy());
                ssu.upload();
            }
        } catch (InterruptedException ex) {
            // do nothing since we have already marked everything as failed
        }
    }

    /**
     * Concatenates all the segments defined in the metadata into a single stream.
     *
     * @param metadata The {@link UploadMetadata} to determine the segments to concatenate
     * @throws Exception
     */
    private void concatenateSegments(final UploadMetadata metadata) throws Exception {
        final String[] inputPaths = new String[metadata.getSegmentCount()];

        //verify if target stream exists
        if (frontEnd.streamExists(metadata.getTargetStreamPath())) {
            if (this.getParameters().isOverwrite()) {
                frontEnd.deleteStream(metadata.getTargetStreamPath(), false);
            } else {
                throw new OperationsException("Target Stream already exists");
            }
        }

        //ensure all input streams exist and are of the expected length
        //ensure all segments in the metadata are marked as 'complete'
        final List<Exception> exceptions = new ArrayList<>();
        ExecutorService exec = Executors.newFixedThreadPool(this.getParameters().getThreadCount());
        for (int i = 0; i < metadata.getSegmentCount(); i++) {
            final int finalI = i;
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (metadata.getSegments()[finalI].getStatus() != SegmentUploadStatus.Complete) {
                            throw new UploadFailedException("Cannot perform 'concatenate' operation because not all streams are fully uploaded.");
                        }

                        String remoteStreamPath = metadata.getSegments()[finalI].getPath();
                        int retryCount = 0;
                        long remoteLength = -1;

                        while (retryCount < SingleSegmentUploader.MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
                            retryCount++;
                            try {
                                remoteLength = frontEnd.getStreamLength(remoteStreamPath);
                                break;
                            } catch (Exception e) {
                                if (retryCount >= SingleSegmentUploader.MAX_BUFFER_UPLOAD_ATTEMPT_COUNT) {
                                    throw new UploadFailedException(
                                            MessageFormat.format(
                                                    "Cannot perform 'concatenate' operation due to the following exception retrieving file information: {0}",
                                                    e));
                                }

                                SingleSegmentUploader.waitForRetry(retryCount, parameters.isUseSegmentBlockBackOffRetryStrategy());
                            }
                        }


                        if (remoteLength != metadata.getSegments()[finalI].getLength()) {
                            throw new UploadFailedException(MessageFormat.format("Cannot perform 'concatenate' operation because segment {0} has an incorrect length (expected {1}, actual {2}).", finalI, metadata.getSegments()[finalI].getLength(), remoteLength));
                        }

                        inputPaths[finalI] = remoteStreamPath;

                    } catch (Exception ex) {
                        //collect any exceptions, whether we just generated them above or whether they come from the Front End,
                        synchronized (exceptions) {
                            exceptions.add(ex);
                        }
                    }
                }
            });
        }

        exec.shutdown();

        try {
            exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // waits ~292 years for completion or interruption.
        }
        catch (InterruptedException e) {
            // add the exception since it will indicate that it was cancelled.
            exceptions.add(e);
        }

        if (exceptions.size() > 0) {
            throw new AggregateUploadException("At least one concatenate test failed", exceptions.remove(0), exceptions);
        }

        //issue the command
        frontEnd.concatenate(metadata.getTargetStreamPath(), inputPaths);
    }
}
