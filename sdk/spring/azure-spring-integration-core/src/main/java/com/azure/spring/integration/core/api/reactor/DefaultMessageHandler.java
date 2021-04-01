// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api.reactor;

import com.azure.spring.integration.core.AbstractDefaultMassageHandler;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.PartitionSupplier;
import org.springframework.expression.Expression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Base class of outbound adapter to publish to azure backed messaging service
 *
 * <p>
 * It delegates real operation to {@link SendOperation} which supports synchronous and asynchronous sending.
 *
 * @author Warren Zhu
 * @author Xiaolu
 */
public class DefaultMessageHandler extends AbstractDefaultMassageHandler {

    private final String destination;
    private final SendOperation sendOperation;

    public DefaultMessageHandler(String destination, @NonNull SendOperation sendOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
        this.sendOperation = sendOperation;
    }

    @Override
    protected void onInit() {
        super.onInit();
        this.evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
        LOGGER.info("Started DefaultMessageHandler with properties: {}", buildPropertiesMap());
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        PartitionSupplier partitionSupplier = toPartitionSupplier(message);
        String destination = toDestination(message);
        final Mono<Void> mono = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        if (this.sync) {
            waitingSendResponse(mono, message);
        } else {
            handleSendResponseAsync(mono, message);
        }

    }

    private <T> void handleSendResponseAsync(Mono<T> mono, Message<?> message) {
        mono.doOnError(ex -> {
            handleSendResponseAsyncSuccess(message, ex);
        }).doOnSuccess(t -> {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("{} sent successfully in async mode", message);
            }
            if (this.sendCallback != null) {
                this.sendCallback.onSuccess((Void) t);
            }
        }).subscribe();
    }

    private <T> void waitingSendResponse(Mono<T> mono, Message<?> message) {
        Long sendTimeout = this.sendTimeoutExpression.getValue(this.evaluationContext, message, Long.class);

        if (sendTimeout == null || sendTimeout < 0) {
            try {
                mono.block();
            } catch (Exception e) {
                throw new MessageDeliveryException(e.getMessage());
            }
        } else {
            try {
                mono.block(Duration.of(sendTimeout, ChronoUnit.MILLIS));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("{} sent successfully in sync mode", message);
                }
            } catch (Exception e) {
                if (e.getCause() instanceof TimeoutException) {
                    throw new MessageTimeoutException(message, "Timeout waiting for send event hub response");
                }
                throw new MessageDeliveryException(e.getMessage());
            }

        }
    }

    public void setSync(boolean sync) {
        this.sync = sync;
        LOGGER.info("DefaultMessageHandler sync becomes: {}", sync);
    }

    public void setSendTimeout(long sendTimeout) {
        setSendTimeoutExpression(new ValueExpression<>(sendTimeout));
    }

    public void setSendTimeoutExpression(Expression sendTimeoutExpression) {
        Assert.notNull(sendTimeoutExpression, "'sendTimeoutExpression' must not be null");
        this.sendTimeoutExpression = sendTimeoutExpression;
        LOGGER.info("DefaultMessageHandler syncTimeout becomes: {}", sendTimeoutExpression);
    }

    private String toDestination(Message<?> message) {
        if (message.getHeaders().containsKey(AzureHeaders.NAME)) {
            return message.getHeaders().get(AzureHeaders.NAME, String.class);
        }

        return this.destination;
    }

    private Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sync", sync);
        properties.put("sendTimeout", sendTimeoutExpression);
        properties.put("destination", destination);
        return properties;
    }
}
