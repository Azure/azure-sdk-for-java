/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Uploads a local file in parallel by splitting it into several segments, according to the given metadata.
 */
public class MultipleSegmentUploader implements Runnable {

    /**
     * The maximum attempts to upload a segment of the file before failing out.
     */
    public static final int MAX_UPLOAD_ATTEMPT_COUNT = 4;
    private UploadMetadata metadata;
    private FrontEndAdapter frontEnd;
    private int maxThreadCount;
    private Queue<SegmentQueueItem> pendingSegments;
    private List<Exception> exceptions;

    /**
     * Creates a new MultipleSegmentUploader.
     *
     * @param uploadMetadata The metadata that keeps track of the file upload.
     * @param maxThreadCount The maximum number of threads to use. Note that in some cases, this number may not be reached.
     * @param frontEnd A pointer to the Front End interface to perform the upload to.
     */
    public MultipleSegmentUploader(UploadMetadata uploadMetadata, int maxThreadCount, FrontEndAdapter frontEnd) {
        metadata = uploadMetadata;
        this.maxThreadCount = maxThreadCount;
        this.frontEnd = frontEnd;
        exceptions = new ArrayList<>();
        pendingSegments = getPendingSegmentsToUpload(metadata);
        this.useSegmentBlockBackOffRetryStrategy = true;
    }

    /**
     * Gets or sets a value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * The MultipleSegmentUploader does not use this directly; it passes it on to SingleSegmentUploader.
     */
    private boolean useSegmentBlockBackOffRetryStrategy;

    /**
     *
     * @return A value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * The MultipleSegmentUploader does not use this directly; it passes it on to SingleSegmentUploader.
     */
    public boolean useSegmentBlockBackOffRetryStrategy() {
        return  useSegmentBlockBackOffRetryStrategy;
    }

    /**
     *
     * @param isEnabled A value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * The MultipleSegmentUploader does not use this directly; it passes it on to SingleSegmentUploader.
     */
    public void setUseSegmentBlockBackOffRetryStrategy(boolean isEnabled) {
        useSegmentBlockBackOffRetryStrategy = isEnabled;
    }
    /**
     * Executes the upload of the segments in the file that were not already uploaded (i.e., those that are in a 'Pending' state).
     *
     * @throws InterruptedException if there is some interruption sent during a wait.
     * @throws AggregateUploadException if there are any failures in any of the threads running upload.
     */
    public void upload() throws InterruptedException, AggregateUploadException {
        int threadCount = Math.min(pendingSegments.size(), maxThreadCount);
        List<Thread> threads = new ArrayList<>(threadCount);

        //start a bunch of new threads that pull from the pendingSegments and then wait for them to finish
        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(this);
            t.start();
            threads.add(t);
        }

        for (Thread t : threads) {
            t.join();
        }


        // aggregate any exceptions and throw them back at our caller
        if (exceptions.size() > 0) {
            // always return the first exception as the primary exception.
            throw new AggregateUploadException("One or more segments could not be uploaded. Review the upload Metadata to determine which segments failed", exceptions.remove(0), exceptions);
        }
    }

    /**
     * Processes the pending segments.
     * @param pendingSegments The pending segments.
     * @param exceptions The exceptions.
     */
    private void processPendingSegments(Queue<SegmentQueueItem> pendingSegments, Collection<Exception> exceptions) {
        while (pendingSegments.size() > 0) {
            //get the next item to process
            SegmentQueueItem toProcess;
            synchronized (pendingSegments) {
                if (pendingSegments.size() == 0) {
                    break;
                }
                toProcess = pendingSegments.remove();
            }

            try {
                //execute it
                uploadSegment(toProcess.segmentNumber, metadata);
            } catch (Exception ex) {
                if (toProcess.attemptCount + 1 < MAX_UPLOAD_ATTEMPT_COUNT) {
                    //re-enqueue at the end, but with an incremented attempt count
                    synchronized (pendingSegments) {
                        pendingSegments.add(new SegmentQueueItem(toProcess.segmentNumber, toProcess.attemptCount + 1));
                    }
                } else {
                    //keep track of the last exception for each segment and report it back
                    synchronized (exceptions) {
                        exceptions.add(ex);
                    }
                }
            }
        }
    }

    /**
     * Uploads the segment.
     *
     * @param segmentNumber The segment number.
     * @param metadata The metadata.
     * @throws Exception if there is any failure while uploading the segment
     */
    private void uploadSegment(int segmentNumber, UploadMetadata metadata) throws Exception {
        //mark the segment as 'InProgress' in the metadata
        updateSegmentMetadataStatus(metadata, segmentNumber, SegmentUploadStatus.InProgress);

        SingleSegmentUploader segmentUploader = new SingleSegmentUploader(segmentNumber, metadata, frontEnd);
        segmentUploader.setUseBackOffRetryStrategy(this.useSegmentBlockBackOffRetryStrategy);

        try {
            segmentUploader.upload();
            // if we reach this point, the upload was successful. Mark it as such.
            updateSegmentMetadataStatus(metadata, segmentNumber, SegmentUploadStatus.Complete);
        } catch (Exception e) {
            //something horrible happened, mark the segment as failed and throw the original exception (the caller will handle it)
            updateSegmentMetadataStatus(metadata, segmentNumber, SegmentUploadStatus.Failed);
            throw e;
        }
    }

    /**
     * Gets the pending segments to upload.
     *
     * @param metadata The metadata.
     * @return A queue containing the remaining pending segments to upload
     */
    private static Queue<SegmentQueueItem> getPendingSegmentsToUpload(UploadMetadata metadata) {
        Queue<SegmentQueueItem> result = new LinkedList<>();
        for (UploadSegmentMetadata segment : metadata.getSegments()) {
            if (segment.getStatus() == SegmentUploadStatus.Pending) {
                result.add(new SegmentQueueItem(segment.getSegmentNumber(), 0));
            }
        }
        return result;
    }

    /**
     * Updates the segment metadata status.
     *
     * @param metadata The metadata.
     * @param segmentNumber The segment number.
     * @param newStatus The new status.
     */
    private static void updateSegmentMetadataStatus(UploadMetadata metadata, int segmentNumber, SegmentUploadStatus newStatus) {
        UploadSegmentMetadata[] toSave = metadata.getSegments();
        toSave[segmentNumber].setStatus(newStatus);
        metadata.setSegments(toSave);
        try {
            metadata.save();
        } catch (Exception e) {
        } //no need to crash the program if were unable to save the metadata; it is what's in memory that's important
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * In this run, we are allowing each thread to attempt to process all
     * of the remaining segments, which will ultimately result in each thread
     * processing a subset of segments that are still in the queue.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        processPendingSegments(pendingSegments, exceptions);
    }

    /**
     * Represents a tuple that pairs a segment number with the number of times it was attempted for upload.
     */
    private static class SegmentQueueItem {
         SegmentQueueItem(int segmentNumber, int attemptCount) {
            this.segmentNumber = segmentNumber;
            this.attemptCount = attemptCount;
        }
        public int getSegmentNumber() {
            return segmentNumber;
        }
        private int segmentNumber;

        public int getAttemptCount() {
            return attemptCount;
        }
        private int attemptCount;
    }
}
