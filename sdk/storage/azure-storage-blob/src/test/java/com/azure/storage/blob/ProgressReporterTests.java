// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;

@SuppressWarnings("deprecation")
public class ProgressReporterTests extends BlobTestBase {

    @Test
    public void reportProgressSequential() {
        ByteBuffer buf1 = getRandomData(10);
        ByteBuffer buf2 = getRandomData(15);
        ByteBuffer buf3 = getRandomData(5);

        ProgressReceiver mockReceiver = Mockito.mock(ProgressReceiver.class);

        Flux<ByteBuffer> data = Flux.just(buf1, buf2, buf3);
        data = ProgressReporter.addProgressReporting(data, mockReceiver);

        data.subscribe();
        data.subscribe(); // Subscribing twice enforces invocation of rewind

        // The same benchmarks should be reported on each subscription (retry). We should never go over total data size.
        Mockito.verify(mockReceiver, times(2)).reportProgress(10);
        Mockito.verify(mockReceiver, times(2)).reportProgress(25);
        Mockito.verify(mockReceiver, times(2)).reportProgress(30);
        Mockito.verify(mockReceiver, times(0)).reportProgress(Mockito.longThat(arg -> arg > 30));
    }

    @EnabledIf("com.azure.storage.blob.BlobTestBase#isLiveMode")
    @Test
    public void reportProgressSequentialNetworkTest() {
        ProgressReceiver mockReceiver = Mockito.mock(ProgressReceiver.class);

        ByteBuffer buffer = getRandomData(1024 * 1024);
        Flux<ByteBuffer> data = ProgressReporter.addProgressReporting(Flux.just(buffer), mockReceiver);

        BlockBlobAsyncClient bu = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            cc.getBlobContainerUrl(), generateBlobName()).getBlockBlobAsyncClient();

        bu.upload(data, buffer.remaining()).block();

        /*
        With the HTTP client, etc. involved, the best we can guarantee is that it's called once with the total. There
        may or may not be any intermediary calls. This test mostly looks to validate that there is no interference
        with actual network calls.
         */
        Mockito.verify(mockReceiver, times(1)).reportProgress(1024 * 1024);
    }

    @Test
    public void reportProgressParallel() {
        ByteBuffer buf1 = getRandomData(10);
        ByteBuffer buf2 = getRandomData(15);
        ByteBuffer buf3 = getRandomData(5);

        ReentrantLock lock = new ReentrantLock();
        AtomicLong totalProgress = new AtomicLong(0);

        ProgressReceiver mockReceiver = Mockito.mock(ProgressReceiver.class);
        Flux<ByteBuffer> data = Flux.just(buf1, buf2, buf3);
        Flux<ByteBuffer> data2 = Flux.just(buf3, buf2, buf1);
        data = ProgressReporter.addParallelProgressReporting(data, mockReceiver, lock, totalProgress);
        data2 = ProgressReporter.addParallelProgressReporting(data2, mockReceiver, lock, totalProgress);

        Disposable disposable1 = data.subscribe();
        Disposable disposable2 = data2.subscribe();
        Disposable disposable3 = data.subscribe();
        Disposable disposable4 = data2.subscribe();

        // a dummy value to avoid compiler warning
        int count = 0;
        while (!(disposable1.isDisposed() && disposable2.isDisposed() && disposable3.isDisposed() && disposable4.isDisposed())) {
            // Busy-wait loop; be cautious about potential for high CPU usage
            count++;
        }

        /*
        There should be at least one call reporting the total length of the data. There may be two if both data and
        data2 complete before the second batch of subscriptions
         */
        // Verify that reportProgress was called 1 to 2 times with argument 60
        Mockito.verify(mockReceiver, Mockito.atLeast(1)).reportProgress(60);
        Mockito.verify(mockReceiver, Mockito.atMost(3)).reportProgress(60);

        /*
        There should be 12 calls total, but either one or two of them could be reporting the total length, so we
        can only guarantee four calls with an unknown parameter. This test doesn't strictly mimic the network as
        there would never be concurrent subscriptions to the same Flux as may be the case here, but it is good
        enough.
         */
        // Verify that reportProgress was called 10 to 11 times with any long argument
        Mockito.verify(mockReceiver, Mockito.atLeast(10)).reportProgress(anyLong());
        Mockito.verify(mockReceiver, Mockito.atMost(12)).reportProgress(anyLong());

        /*
        We should never report more progress than the 60 total (30 from each Flux--Resubscribing is a retry and
        therefore rewinds).
         */
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(mockReceiver, Mockito.atLeast(0)).reportProgress(argumentCaptor.capture());

        List<Long> capturedArguments = argumentCaptor.getAllValues();
        for (Long argument : capturedArguments) {
            assertTrue(argument <= 60);
        }
    }

    /**
     * This test asserts that ProgressListener from core dispatches progress notification to old
     * ProgressReceiver API to assure backwards compatibility.
     */
    @Test
    public void progressListenerDelegatesToProgressReceiverByDefault() {
        TestBlobProgressReceiver blobReceiver = new TestBlobProgressReceiver();
        TestCommonProgressReceiver commonReceiver = new TestCommonProgressReceiver();

        blobReceiver.handleProgress(1L);
        blobReceiver.handleProgress(3L);
        blobReceiver.handleProgress(5L);

        assertArrayEquals(new Long[]{1L, 3L, 5L}, blobReceiver.progresses.toArray());

        commonReceiver.handleProgress(1L);
        commonReceiver.handleProgress(3L);
        commonReceiver.handleProgress(5L);

        assertArrayEquals(new Long[]{1L, 3L, 5L}, commonReceiver.progresses.toArray());
    }

    private static class TestBlobProgressReceiver implements ProgressReceiver {

        List<Long> progresses = new ArrayList<>();

        @Override
        public void reportProgress(long bytesTransferred) {
            progresses.add(bytesTransferred);
        }
    }

    private static class TestCommonProgressReceiver implements com.azure.storage.common.ProgressReceiver {

        List<Long> progresses = new ArrayList<>();

        @Override
        public void reportProgress(long bytesTransferred) {
            progresses.add(bytesTransferred);
        }
    }
}
