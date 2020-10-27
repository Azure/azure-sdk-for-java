package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.models.ServiceBusProcessorClientOptions;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *  The processor client for processing service bus messages.
 */
public final class ServiceBusProcessorClient {

    private final ClientLogger logger = new ClientLogger(ServiceBusProcessorClient.class);
    private final ServiceBusReceiverAsyncClient receiverClient;
    private final Consumer<ServiceBusProcessorContext> processMessage;
    private final Consumer<Throwable> processError;
    private final ServiceBusProcessorClientOptions processorOptions;
    private final AtomicReference<Subscription> receiverSubscription = new AtomicReference<>();

    private volatile boolean isRunning = false;

    ServiceBusProcessorClient(ServiceBusReceiverAsyncClient receiverClient,
                              Consumer<ServiceBusProcessorContext> processMessage, Consumer<Throwable> processError,
                              ServiceBusProcessorClientOptions processorOptions) {
        this.receiverClient = Objects.requireNonNull(receiverClient, "'receiverClient' cannot be null");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null");
        this.processorOptions = processorOptions;
    }

    /**
     * Starts the message processing.
     */
    public synchronized void start(){
        if (isRunning) {
            logger.info("Processor is already running");
            return;
        }

        isRunning = true;
        if (receiverSubscription.get() == null) {
            receiverClient.receiveMessages()
                .parallel(processorOptions.getMaxConcurrentCalls())
                .runOn(Schedulers.boundedElastic())
                .subscribe(new Subscriber<ServiceBusReceivedMessageContext>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        receiverSubscription.set(subscription);
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(ServiceBusReceivedMessageContext serviceBusReceivedMessageContext) {
                        try {
                            if (serviceBusReceivedMessageContext.hasError()) {
                                processError.accept(serviceBusReceivedMessageContext.getThrowable());
                            } else {
                                ServiceBusProcessorContext serviceBusProcessorContext =
                                    new ServiceBusProcessorContext(receiverClient, serviceBusReceivedMessageContext);
                                processMessage.accept(serviceBusProcessorContext);
                            }
                            if (isRunning) {
                                receiverSubscription.get().request(1);
                            }
                        } catch (Exception exception) {
                            processError.accept(exception);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        // the underlying async client should never return an error through this channel
                        logger.info("Unexpected onError call to the processor subscriber. Restarting the processor.",
                            throwable);
                        close();
                        start();
                    }

                    @Override
                    public void onComplete() {
                        logger.info("Unexpected onComplete call to the processor subscriber. Restarting the processor");
                        close();
                        start();
                    }
                });
        } else {
            receiverSubscription.get().request(1);
        }
    }

    /**
     * Stops the message processing for this processor.
     */
    public synchronized void stop() {
        isRunning = false;
    }

    /**
     * Stops message processing and closes the processor.
     */
    public synchronized void close() {
        isRunning = false;
        receiverSubscription.get().cancel();
        receiverClient.close();
    }

    /**
     * Method to check if the processor is running.
     *
     * @return {@code true} if the processor is running.
     */
    public synchronized boolean isRunning() {
        return isRunning;
    }
}
