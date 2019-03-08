/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.blob;

import io.reactivex.Flowable;
import io.reactivex.Single;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

/**
 * {@code ProgressReporterImpl} offers a convenient way to add progress tracking to a given Flowable.
 */
public final class ProgressReporter {

    private static abstract class ProgressReporterImpl implements IProgressReceiver{
        long blockProgress;

        final IProgressReceiver progressReceiver;

        ProgressReporterImpl(IProgressReceiver progressReceiver) {
            this.blockProgress = 0;
            this.progressReceiver = progressReceiver;
        }

        @Override
        public void reportProgress(long bytesTransferred) {
            this.blockProgress += bytesTransferred;
        }

        void rewindProgress() {
            this.blockProgress = 0;
        }

        Flowable<ByteBuffer> addProgressReporting(Flowable<ByteBuffer> data) {
            return Single.just(this)
                    .flatMapPublisher(progressReporter -> {
                    /*
                    Each time there is a new subscription, we will rewind the progress. This is desirable specifically
                    for retries, which resubscribe on each try. The first time this flowable is subscribed to, the
                    rewind will be a noop as there will have been no progress made. Subsequent rewinds will work as
                    expected.
                     */
                        progressReporter.rewindProgress();
                    /*
                    Every time we emit some data, report it to the Tracker, which will pass it on to the end user.
                     */
                        return data.doOnNext(buffer ->
                                progressReporter.reportProgress(buffer.remaining()));
                    });
        }
    }

    /**
     * This type is used to keep track of the total amount of data transferred for a single request. This is the type
     * we will use when the customer uses the factory to add progress reporting to their Flowable. We need this
     * additional type because we can't keep local state directly as lambdas require captured local variables to be
     * effectively final.
     */
    private static class SequentialProgressReporter extends ProgressReporterImpl {
        SequentialProgressReporter(IProgressReceiver progressReceiver) {
            super(progressReceiver);
        }

        @Override
        public void reportProgress(long bytesTransferred) {
            super.reportProgress(bytesTransferred);
            this.progressReceiver.reportProgress(this.blockProgress);
        }
    }

    /**
     * This type is used to keep track of the total amount of data transferred as a part of a parallel upload in order
     * to coordinate progress reporting to the end user. We need this additional type because we can't keep local state
     * directly as lambdas require captured local variables to be effectively final.
     */
    private static class ParallelProgressReporter extends ProgressReporterImpl {
        /*
        This lock will be instantiated by the operation initiating the whole transfer to coordinate each
        ProgressReporterImpl.
         */
        private final Lock transferLock;

        /*
        We need an AtomicLong to be able to update the value referenced. Because we are already synchronizing with the
        lock, we don't incur any additional performance hit here by the synchronization.
         */
        private AtomicLong totalProgress;

        ParallelProgressReporter(IProgressReceiver progressReceiver, Lock lock, AtomicLong totalProgress) {
            super(progressReceiver);
            this.transferLock = lock;
            this.totalProgress = totalProgress;
        }

        @Override
        public void reportProgress(long bytesTransferred) {
            super.reportProgress(bytesTransferred);

            /*
            It is typically a bad idea to lock around customer code (which the progressReceiver is) because they could
            never release the lock. However, we have decided that it is sufficiently difficult for them to make their
            progressReporting code threadsafe that we will take that burden and the ensuing risks. Although it is the
            case that only one thread is allowed to be in onNext at once, however there are multiple independent
            requests happening at once to stage/download separate chunks, so we still need to lock either way.
             */
            transferLock.lock();
            this.progressReceiver.reportProgress(this.totalProgress.addAndGet(bytesTransferred));
            transferLock.unlock();
    }

        /*
        This is used in the case of retries to rewind the amount of progress reported so as not to over-report at the
        end.
         */
        @Override
        public void rewindProgress() {
            /*
            Blocks do not interfere with each other's block progress and there is no way that, for a single block, one
            thread will be trying to add to the progress while the other is trying to zero it. The updates are strictly
            sequential. Avoiding using the lock is ideal.
             */
            this.totalProgress.addAndGet(-1 * this.blockProgress);
            super.rewindProgress();
        }

    }

    /**
     * Adds progress reporting functionality to the given {@code Flowable}. Each subscription (and therefore each
     * retry) will rewind the progress reported so as not to over-report. The data reported will be the total amount
     * of data emitted so far, or the "current position" of the Flowable.
     *
     * @param data
     *         The data whose transfer progress is to be tracked.
     * @param progressReceiver
     *         {@link IProgressReceiver}
     *
     * @return A {@code Flowable} that emits the same data as the source but calls a callback to report the total amount
     * of data emitted so far.
     *
     * @apiNote ## Sample Code \n
     * [!code-java[Sample_Code](../azure-storage-java/src/test/java/com/microsoft/azure/storage/Samples.java?name=progress "Sample code for ProgressReporterFactor.addProgressReporting")] \n
     * For more samples, please see the [Samples file](%https://github.com/Azure/azure-storage-java/blob/master/src/test/java/com/microsoft/azure/storage/Samples.java)
     */
    public static Flowable<ByteBuffer> addProgressReporting(Flowable<ByteBuffer> data,
            IProgressReceiver progressReceiver) {
        if (progressReceiver == null) {
            return data;
        }
        else {
            ProgressReporterImpl tracker = new SequentialProgressReporter(progressReceiver);
            return tracker.addProgressReporting(data);
        }
    }

    static Flowable<ByteBuffer> addParallelProgressReporting(Flowable<ByteBuffer> data,
            IProgressReceiver progressReceiver, Lock lock, AtomicLong totalProgress) {
        if (progressReceiver == null) {
            return data;
        }
        else {
            ParallelProgressReporter tracker = new ParallelProgressReporter(progressReceiver, lock, totalProgress);
            return tracker.addProgressReporting(data);
        }
    }
}
