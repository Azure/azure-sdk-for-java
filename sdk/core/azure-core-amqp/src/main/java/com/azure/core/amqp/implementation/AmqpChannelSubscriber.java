package com.azure.core.amqp.implementation;


import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Operators;
import reactor.core.publisher.ReplayProcessor;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Subscriber that manages the AMQP channel and publishes state.
 *
 * @param <T> AMQP channel
 */
public final class AmqpChannelSubscriber<T> extends BaseSubscriber<AmqpEndpointState> {
    private final T amqpChannel;
    private final AtomicBoolean hasStarted = new AtomicBoolean();
    private final ClientLogger logger;
    private final ReplayProcessor<Boolean> isOpenProcessor = ReplayProcessor.cacheLast();
    private final FluxSink<Boolean> isOpenSink = isOpenProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final String identifier;

    private volatile boolean isDisposed = false;

    public AmqpChannelSubscriber(String identifier, T amqpChannel) {
        this.identifier = Objects.requireNonNull(identifier, "'identifier' cannot be null");
        this.logger = new ClientLogger(AmqpChannelSubscriber.class + "[" + identifier + "]");
        this.amqpChannel = Objects.requireNonNull(amqpChannel, "'amqpChannel' cannot be null.");
    }

    /**
     * Gets the identifier for this connection subscriber.
     *
     * @return The identifier for this connection subscriber.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the channel wrapped by this subscriber.
     *
     * @return The channel wrapped by this subscriber.
     */
    public T getChannel() {
        return amqpChannel;
    }

    public Flux<Boolean> isOpen() {
        return isOpenProcessor;
    }

    @Override
    protected void hookOnNext(AmqpEndpointState value) {
        switch (value) {
            case ACTIVE:
                if (!hasStarted.getAndSet(true)) {
                    isOpenSink.next(true);
                }
                break;
            case CLOSED:
                isOpenSink.complete();
                break;
            case UNINITIALIZED:
                logger.verbose("Channel is opening.");
                break;
            default:
                logger.warning("Unprocessed status: {}", value);
                break;
        }
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        if (isDisposed) {
            Operators.onErrorDropped(throwable, Context.empty());
            return;
        }

        isDisposed = true;
        isOpenSink.error(throwable);
    }

    /**
     * Disposes of the AMQP channel.
     */
    @Override
    protected void hookOnComplete() {
        if (isDisposed) {
            return;
        }

        isDisposed = true;
        isOpenSink.complete();

        if (amqpChannel instanceof AutoCloseable) {
            try {
                ((AutoCloseable) amqpChannel).close();
            } catch (Exception error) {
                logger.warning("Error occurred closing channel.", error);
            }
        } else if (amqpChannel instanceof Disposable) {
            ((Disposable) amqpChannel).dispose();
        }
    }

    @Override
    protected void hookOnCancel() {
        hookOnComplete();
    }
}
