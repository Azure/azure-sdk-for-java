package com.microsoft.rest.v2.http;

import io.reactivex.FlowableSubscriber;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import static org.junit.Assert.fail;

public class FlowableFileStreamTests {
    @Test
    public void cancelTest() throws IOException {

        Path filePath = Paths.get("cancelTest.dat");
        try {
            Random random = new Random();
            byte[] buf = new byte[1024 * 1024 * 100];
            random.nextBytes(buf);

            Files.deleteIfExists(filePath);
            Files.write(filePath, buf, StandardOpenOption.CREATE);

            FlowableFileStream stream = new FlowableFileStream(AsynchronousFileChannel.open(filePath, StandardOpenOption.READ));
            stream.blockingSubscribe(new FlowableSubscriber<byte[]>() {
                Subscription subscription;
                int count = 0;

                @Override
                public void onSubscribe(Subscription s) {
                    subscription = s;
                    s.request(Long.MAX_VALUE);
                }

                @Override
                public void onNext(byte[] bytes) {
                    count++;
                    System.out.println("count == " + count);
                    if (count == 3) {
                        subscription.cancel();
                    }

                    // Canceling the subscription causes downstream to not
                    // receive events even if upstream is still emitting events.
                    // Unsure how to test meaningfully without just looking at println output.
                    if (count > 3) {
                        fail();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    fail(t.getMessage());
                }

                @Override
                public void onComplete() {
                    fail();
                }
            });
        }
        finally {
            Files.deleteIfExists(filePath);
        }
    }
}
