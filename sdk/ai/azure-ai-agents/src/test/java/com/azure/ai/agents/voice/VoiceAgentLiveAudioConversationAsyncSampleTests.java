// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeConversationCreatedEvent;
import com.azure.core.util.BinaryData;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentLiveAudioConversationAsyncSampleTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    @Test
    public void conversationCreatedModelIsAvailable() {
        RealtimeServerEvent event = BinaryData
            .fromString("{\"type\":\"conversation.created\","
                + "\"conversation\":{\"id\":\"test\",\"object\":\"realtime.conversation\"}}")
            .toObject(RealtimeServerEvent.class);
        assertTrue(event instanceof RealtimeConversationCreatedEvent);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void receiveTerminationClosesAudio(boolean failReceive) {
        FakeAudio audio = new FakeAudio();
        IllegalStateException error = new IllegalStateException("Disconnected");
        Mono<Void> receive = failReceive ? Mono.error(error) : Mono.empty();
        Mono<Void> conversation
            = VoiceAgentLiveAudioConversationAsyncSample.runConversation(receive, audio.processor(), emptyInput());
        if (failReceive) {
            assertSame(error, assertThrows(IllegalStateException.class, () -> conversation.block(TIMEOUT)));
        } else {
            conversation.block(TIMEOUT);
        }
        audio.assertClosed();
    }

    @Test
    public void microphoneFailureClosesAudio() {
        FakeAudio audio = new FakeAudio();
        audio.readFailure = new IllegalStateException("Microphone failed");
        Mono<Void> conversation
            = VoiceAgentLiveAudioConversationAsyncSample.runConversation(Mono.never(), audio.processor(), emptyInput());
        assertSame(audio.readFailure, assertThrows(IllegalStateException.class, () -> conversation.block(TIMEOUT)));
        audio.assertClosed();
    }

    @Test
    public void playbackAcceptsBurstsAndEnforcesByteLimitWithoutBlocking() throws Exception {
        FakeAudio audio = new FakeAudio();
        VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor processor = audio.processor();
        CountDownLatch receiving = new CountDownLatch(1);
        CompletableFuture<Void> conversation
            = VoiceAgentLiveAudioConversationAsyncSample
                .runConversation(Mono.<Void>never().doOnSubscribe(subscription -> receiving.countDown()), processor,
                    emptyInput())
                .toFuture();
        try {
            assertTrue(receiving.await(5, TimeUnit.SECONDS));
            processor.queueAudio(new byte[2]);
            assertTrue(audio.writing.await(5, TimeUnit.SECONDS));
            assertTimeoutPreemptively(TIMEOUT, () -> {
                for (int chunk = 0; chunk < 100; chunk++) {
                    processor.queueAudio(new byte[2400]);
                }
            });
            assertFalse(conversation.isDone());
            processor.skipPendingAudio();
            processor
                .queueAudio(new byte[VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor.MAX_PLAYBACK_BYTES]);
            assertFalse(conversation.isDone());
            assertTimeoutPreemptively(TIMEOUT, () -> processor.queueAudio(new byte[2]));
            ExecutionException error
                = assertThrows(ExecutionException.class, () -> conversation.get(5, TimeUnit.SECONDS));
            assertEquals("Audio playback backlog exceeded 60 seconds.", error.getCause().getMessage());
            audio.assertClosed();
        } finally {
            conversation.cancel(true);
            processor.close();
        }
    }

    @Test
    public void cancellationClosesAudioAndCancelsReceive() throws Exception {
        FakeAudio audio = new FakeAudio();
        CountDownLatch cancelled = new CountDownLatch(1);
        CountDownLatch receiving = new CountDownLatch(1);
        VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor processor = audio.processor();
        CompletableFuture<Void> conversation = VoiceAgentLiveAudioConversationAsyncSample.runConversation(
            Mono.<Void>never().doOnSubscribe(subscription -> receiving.countDown()).doOnCancel(cancelled::countDown),
            processor, emptyInput()).toFuture();
        try {
            assertTrue(receiving.await(5, TimeUnit.SECONDS));
            conversation.cancel(true);
            assertTrue(audio.closed.await(5, TimeUnit.SECONDS));
            assertTrue(cancelled.await(5, TimeUnit.SECONDS));
        } finally {
            conversation.cancel(true);
            processor.close();
        }
        audio.assertClosed();
    }

    @Test
    public void closedProcessorCannotRestart() {
        FakeAudio audio = new FakeAudio();
        VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor processor = audio.processor();
        processor.close();
        assertThrows(IllegalStateException.class, processor::start);
        audio.assertClosed();
    }

    @Test
    public void enterStopsConversationWithoutClosingStandardInput() {
        FakeAudio audio = new FakeAudio();
        AtomicBoolean inputClosed = new AtomicBoolean();
        InputStream input = new ByteArrayInputStream("end\n".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() {
                inputClosed.set(true);
            }
        };
        VoiceAgentLiveAudioConversationAsyncSample.runConversation(Mono.never(), audio.processor(), input)
            .block(TIMEOUT);
        assertFalse(inputClosed.get());
        audio.assertClosed();
    }

    @Test
    public void inputPollingNeverReadsUnavailableBytes() {
        InputStream input = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("A read with no available bytes can block indefinitely.");
            }
        };
        Mono.firstWithSignal(VoiceAgentLiveAudioConversationAsyncSample.waitForEnter(input),
            Mono.delay(Duration.ofMillis(300)).then()).block(TIMEOUT);
    }

    private static InputStream emptyInput() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static final class FakeAudio implements InvocationHandler {
        private final CountDownLatch closed = new CountDownLatch(2);
        private final CountDownLatch writing = new CountDownLatch(1);
        private final AtomicReference<Thread> captureThread = new AtomicReference<>();
        private final AtomicReference<Thread> playbackThread = new AtomicReference<>();
        private RuntimeException readFailure;

        VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor processor() {
            TargetDataLine microphone = (TargetDataLine) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { TargetDataLine.class }, this);
            SourceDataLine speaker = (SourceDataLine) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] { SourceDataLine.class }, this);
            return new VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor(null, microphone, speaker);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) throws InterruptedException {
            switch (method.getName()) {
                case "read":
                    captureThread.set(Thread.currentThread());
                    if (readFailure != null) {
                        throw readFailure;
                    }
                    closed.await();
                    return 0;

                case "write":
                    playbackThread.set(Thread.currentThread());
                    writing.countDown();
                    closed.await();
                    return arguments[2];

                case "close":
                    closed.countDown();
                    return null;

                default:
                    return null;
            }
        }

        void assertClosed() {
            assertEquals(0, closed.getCount());
            assertTimeoutPreemptively(TIMEOUT, () -> {
                if (captureThread.get() != null) {
                    captureThread.get().join();
                    assertFalse(captureThread.get().isAlive());
                }
                if (playbackThread.get() != null) {
                    playbackThread.get().join();
                    assertFalse(playbackThread.get().isAlive());
                }
            });
        }
    }
}
