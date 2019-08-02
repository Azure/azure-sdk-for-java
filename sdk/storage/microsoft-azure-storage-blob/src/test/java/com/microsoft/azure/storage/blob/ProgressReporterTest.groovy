// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.APISpec
import io.reactivex.Flowable

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock;

class ProgressReporterTest extends APISpec {
    def "Report progress sequential"() {
        setup:
        def buf1 = getRandomData(10)
        def buf2 = getRandomData(15)
        def buf3 = getRandomData(5)

        def mockReceiver = Mock(IProgressReceiver)

        Flowable<ByteBuffer> data = Flowable.just(buf1, buf2, buf3)
        data = ProgressReporter.addProgressReporting(data, mockReceiver)

        when:
        data.blockingSubscribe()
        data.blockingSubscribe() // Subscribing twice enforces invocation of rewind

        then:
        // The same benchmarks should be reported on each subscription (retry). We should never go over total data size.
        2 * mockReceiver.reportProgress(10)
        2 * mockReceiver.reportProgress(25)
        2 * mockReceiver.reportProgress(30)
        0 * mockReceiver.reportProgress({it > 30})
    }

    def "Report progress sequential network test"() {
        setup:
        def mockReceiver = Mock(IProgressReceiver)

        def buffer = getRandomData(1 * 1024 * 1024)
        def data = ProgressReporter.addProgressReporting(Flowable.just(buffer), mockReceiver)

        when:
        def bu = cu.createBlockBlobURL(generateBlobName())
        bu.upload(data, buffer.remaining()).blockingGet()

        then:
        /*
        With the HTTP client, etc. involved, the best we can guarantee is that it's called once with the total. There
        may or may not be any intermediary calls. This test mostly looks to validate that there is no interference
        with actual network calls.
         */
        1 * mockReceiver.reportProgress(1 * 1024 * 1024)
    }

    def "Report progress parallel"() {
        setup:
        def buf1 = getRandomData(10)
        def buf2 = getRandomData(15)
        def buf3 = getRandomData(5)

        def lock = new ReentrantLock()
        def totalProgress = new AtomicLong(0)

        def mockReceiver = Mock(IProgressReceiver)
        def data = Flowable.just(buf1, buf2, buf3)
        def data2 = Flowable.just(buf3, buf2, buf1)
        data = ProgressReporter.addParallelProgressReporting(data, mockReceiver, lock, totalProgress)
        data2 = ProgressReporter.addParallelProgressReporting(data2, mockReceiver, lock, totalProgress)

        when:
        data.subscribe()
        data2.subscribe()
        data.subscribe()
        data2.subscribe()

        sleep(3000) // These Flowables should complete quickly, but we don't want to block or it'll order everything

        then:
        /*
        There should be at least one call reporting the total length of the data. There may be two if both data and
        data2 complete before the second batch of subscriptions
         */
        (1..2) * mockReceiver.reportProgress(60)

        /*
        There should be 12 calls total, but either one or two of them could be reporting the total length, so we
        can only guarantee four calls with an unknown parameter. This test doesn't strictly mimic the network as
        there would never be concurrent subscriptions to the same Flowable as may be the case here, but it is good
        enough.
         */
        (10..11) * mockReceiver.reportProgress(_)

        /*
        We should never report more progress than the 60 total (30 from each Flowable--Resubscribing is a retry and
        therefore rewinds).
         */
        0 * mockReceiver.reportProgress({it > 60})
    }

    // See TransferManagerTest for network tests of the parallel ProgressReporter.
}
