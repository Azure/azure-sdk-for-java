/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.util.*;

/**
 * Uploads a local file in parallel by splitting it into several segments, according to the given metadata.
 */
public class MultipleSegmentUploader implements Runnable {

    public static final int MaxUploadAttemptCount = 4;
    private UploadMetadata _metadata;
    private FrontEndAdapter _frontEnd;
    private int _maxThreadCount;
    private Queue<SegmentQueueItem> _pendingSegments;
    private List<Exception> _exceptions;

    /**
     * Creates a new MultipleSegmentUploader.
     *
     * @param uploadMetadata The metadata that keeps track of the file upload.
     * @param maxThreadCount The maximum number of threads to use. Note that in some cases, this number may not be reached.
     * @param frontEnd A pointer to the Front End interface to perform the upload to.
     */
    public MultipleSegmentUploader(UploadMetadata uploadMetadata, int maxThreadCount, FrontEndAdapter frontEnd) {
        _metadata = uploadMetadata;
        _maxThreadCount = maxThreadCount;
        _frontEnd = frontEnd;
        _exceptions = new ArrayList<>();
        _pendingSegments = GetPendingSegmentsToUpload(_metadata);
        this.UseSegmentBlockBackOffRetryStrategy = true;
    }

    /**
     * Gets or sets a value indicating whether to use a back-off (exponenential) in case of individual block failures.
     * The MultipleSegmentUploader does not use this directly; it passes it on to SingleSegmentUploader.
     */
    public boolean UseSegmentBlockBackOffRetryStrategy;

    /**
     * Executes the upload of the segments in the file that were not already uploaded (i.e., those that are in a 'Pending' state).
     *
     * @throws InterruptedException
     * @throws AggregateUploadException
     */
    public void Upload() throws InterruptedException, AggregateUploadException {
        int threadCount = Math.min(_pendingSegments.size(), _maxThreadCount);
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
        if (_exceptions.size() > 0) {
            // always return the first exception as the primary exception.
            throw new AggregateUploadException("One or more segments could not be uploaded. Review the Upload Metadata to determine which segments failed", _exceptions.remove(0), _exceptions);
        }
    }

    /**
     * Processes the pending segments.
     * @param pendingSegments The pending segments.
     * @param exceptions The exceptions.
     */
    private void ProcessPendingSegments(Queue<SegmentQueueItem> pendingSegments, Collection<Exception> exceptions) {
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
                UploadSegment(toProcess.SegmentNumber, _metadata);
            } catch (Exception ex) {
                if (toProcess.AttemptCount + 1 < MaxUploadAttemptCount) {
                    //re-enqueue at the end, but with an incremented attempt count
                    synchronized (pendingSegments) {
                        pendingSegments.add(new SegmentQueueItem(toProcess.SegmentNumber, toProcess.AttemptCount + 1));
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
     * @throws Exception
     */
    private void UploadSegment(int segmentNumber, UploadMetadata metadata) throws Exception {
        //mark the segment as 'InProgress' in the metadata
        UpdateSegmentMetadataStatus(metadata, segmentNumber, SegmentUploadStatus.InProgress);

        SingleSegmentUploader segmentUploader = new SingleSegmentUploader(segmentNumber, metadata, _frontEnd);
        segmentUploader.UseBackOffRetryStrategy = this.UseSegmentBlockBackOffRetryStrategy;

        try {
            segmentUploader.Upload();

            //if we reach this point, the upload was successful; mark it as such 
            UpdateSegmentMetadataStatus(metadata, segmentNumber, SegmentUploadStatus.Complete);
        } catch (Exception e) {
            //something horrible happened, mark the segment as failed and throw the original exception (the caller will handle it)
            UpdateSegmentMetadataStatus(metadata, segmentNumber, SegmentUploadStatus.Failed);
            throw e;
        }
    }

    /**
     * Gets the pending segments to upload.
     *
     * @param metadata The metadata.
     * @return A queue containing the remaining pending segments to upload
     */
    private static Queue<SegmentQueueItem> GetPendingSegmentsToUpload(UploadMetadata metadata) {
        Queue<SegmentQueueItem> result = new LinkedList<>();
        for (UploadSegmentMetadata segment : metadata.Segments) //.Where(segment => segment.Status == SegmentUploadStatus.Pending))
        {
            if (segment.Status == SegmentUploadStatus.Pending) {
                result.add(new SegmentQueueItem(segment.SegmentNumber, 0));
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
    private static void UpdateSegmentMetadataStatus(UploadMetadata metadata, int segmentNumber, SegmentUploadStatus newStatus) {
        metadata.Segments[segmentNumber].Status = newStatus;
        try {
            metadata.Save();
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
        ProcessPendingSegments(_pendingSegments, _exceptions);
    }

    /**
     * Represents a tuple that pairs a segment number with the number of times it was attempted for upload
     */
    private static class SegmentQueueItem {
        public SegmentQueueItem(int segmentNumber, int attemptCount) {
            this.SegmentNumber = segmentNumber;
            this.AttemptCount = attemptCount;
        }

        public int SegmentNumber;

        public int AttemptCount;
    }
}
