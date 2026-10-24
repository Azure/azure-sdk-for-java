// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.SessionResponseStatus;
import com.azure.ai.voicelive.models.SessionServerEvent;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudioSampleLifecycleTest {

    @Test
    void playbackCompletesOnlyAfterQueuedAudioIsWrittenAndDrained() {
        TestAudioOutput output = new TestAudioOutput(false);
        AudioPlaybackSample.PlaybackController playback = new AudioPlaybackSample.PlaybackController(output, 2);
        playback.start();

        assertTrue(playback.queueAudio(new byte[] { 1, 2 }));
        assertTrue(playback.queueAudio(new byte[] { 3, 4 }));
        playback.finishGracefully().block(Duration.ofSeconds(5));

        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, output.getWrittenAudio());
        assertEquals(4, playback.getAcceptedAudioBytes());
        assertEquals(AudioPlaybackSample.PlaybackState.COMPLETED, playback.getState());
        assertTrue(output.indexOf("drain") > output.lastIndexOf("write"));
        assertTrue(output.indexOf("close") > output.indexOf("drain"));
    }

    @Test
    void terminalMarkerRemainsReliableWhenAudioQueueIsFull() throws InterruptedException {
        TestAudioOutput output = new TestAudioOutput(true);
        AudioPlaybackSample.PlaybackController playback = new AudioPlaybackSample.PlaybackController(output, 1);
        playback.start();

        assertTrue(playback.queueAudio(new byte[] { 1 }));
        assertTrue(output.awaitWriteStarted());
        assertTrue(playback.queueAudio(new byte[] { 2 }));
        assertFalse(playback.queueAudio(new byte[] { 3 }));
        assertEquals(1, playback.getDroppedAudioChunks());

        // The one audio slot is full, but the terminal marker must still be accepted.
        playback.finishGracefully();
        output.releaseWrite();
        playback.completion().block(Duration.ofSeconds(5));

        assertArrayEquals(new byte[] { 1, 2 }, output.getWrittenAudio());
        assertEquals(AudioPlaybackSample.PlaybackState.COMPLETED, playback.getState());
        assertEquals(1, output.count("drain"));
    }

    @Test
    void abortDiscardsQueuedAudioAndDoesNotDrain() throws InterruptedException {
        TestAudioOutput output = new TestAudioOutput(true);
        AudioPlaybackSample.PlaybackController playback = new AudioPlaybackSample.PlaybackController(output, 2);
        playback.start();

        assertTrue(playback.queueAudio(new byte[] { 1 }));
        assertTrue(output.awaitWriteStarted());
        assertTrue(playback.queueAudio(new byte[] { 2 }));

        playback.abort(new IllegalStateException("test abort"));

        assertThrows(RuntimeException.class, () -> playback.completion().block(Duration.ofSeconds(5)));
        assertEquals(AudioPlaybackSample.PlaybackState.ABORTED, playback.getState());
        assertFalse(output.getEvents().contains("drain"));
        assertTrue(output.getEvents().contains("flush"));
        assertTrue(output.getEvents().contains("close"));
    }

    @Test
    void completedResponseRequiresCompletedStatusAndAudio() {
        AudioPlaybackSample.validateCompletedResponse("response-1", SessionResponseStatus.COMPLETED, 1);

        assertThrows(IllegalStateException.class,
            () -> AudioPlaybackSample.validateCompletedResponse("response-2", SessionResponseStatus.CANCELLED, 1));
        assertThrows(IllegalStateException.class,
            () -> AudioPlaybackSample.validateCompletedResponse("response-3", null, 1));
        assertThrows(IllegalStateException.class,
            () -> AudioPlaybackSample.validateCompletedResponse("response-4", SessionResponseStatus.COMPLETED, 0));
    }

    @Test
    void assistantHandlesResponseDoneWithMissingResponseSafely() {
        SessionServerEvent responseDone
            = BinaryData.fromString("{\"type\":\"response.done\"," + "\"event_id\":\"event-3\",\"response\":null}")
                .toObject(SessionServerEvent.class);

        VoiceAssistantSample.handleServerEvent(responseDone, null);
    }

    @Test
    void assistantTracksAggregateQueueDropAndSkipDiagnosticsWithoutHardware() {
        VoiceAssistantPlaybackDiagnostics diagnostics = new VoiceAssistantPlaybackDiagnostics();

        for (int i = 0; i < 1000; i++) {
            diagnostics.recordAudioChunk(2, true, i + 1);
        }
        diagnostics.recordAudioChunk(2, false, 1000);

        assertEquals(1001, diagnostics.getAudioChunkCount());
        assertEquals(2002, diagnostics.getAudioByteCount());
        assertEquals(1, diagnostics.getDroppedPacketCount());
        assertEquals(1000, diagnostics.getHighWaterQueueDepth());

        diagnostics.recordSkip(1000);

        assertEquals(1, diagnostics.getSkipOperationCount());
        assertEquals(1000, diagnostics.getSkippedPacketCount());
    }

    private static final class TestAudioOutput implements AudioPlaybackSample.AudioOutput {
        private volatile boolean open = true;
        private final List<String> events = new CopyOnWriteArrayList<>();
        private final ByteArrayOutputStream writtenAudio = new ByteArrayOutputStream();
        private final CountDownLatch writeStarted = new CountDownLatch(1);
        private final CountDownLatch allowWrite;

        private TestAudioOutput(boolean blockWrites) {
            this.allowWrite = new CountDownLatch(blockWrites ? 1 : 0);
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public int write(byte[] audioData, int offset, int length) {
            events.add("write");
            writeStarted.countDown();
            try {
                allowWrite.await();
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                return 0;
            }
            synchronized (writtenAudio) {
                writtenAudio.write(audioData, offset, length);
            }
            return length;
        }

        @Override
        public void drain() {
            events.add("drain");
        }

        @Override
        public void stop() {
            events.add("stop");
        }

        @Override
        public void flush() {
            events.add("flush");
        }

        @Override
        public void close() {
            events.add("close");
            open = false;
        }

        private boolean awaitWriteStarted() throws InterruptedException {
            return writeStarted.await(5, TimeUnit.SECONDS);
        }

        private void releaseWrite() {
            allowWrite.countDown();
        }

        private byte[] getWrittenAudio() {
            synchronized (writtenAudio) {
                return writtenAudio.toByteArray();
            }
        }

        private List<String> getEvents() {
            return events;
        }

        private int indexOf(String event) {
            return events.indexOf(event);
        }

        private int lastIndexOf(String event) {
            return events.lastIndexOf(event);
        }

        private long count(String event) {
            return events.stream().filter(event::equals).count();
        }
    }
}
