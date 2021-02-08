// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

import com.azure.spring.integration.core.api.SendOperation;
import com.azure.spring.integration.core.api.PartitionSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.integration.expression.ExpressionUtils;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.AbstractMessageProducingHandler;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base class of outbound adapter to publish to azure backed messaging service
 *
 * <p>
 * It delegates real operation to {@link SendOperation} which supports synchronous and asynchronous sending.
 *
 * @author Warren Zhu
 */
public class DefaultMessageHandler extends AbstractMessageProducingHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMessageHandler.class);
    private static final long DEFAULT_SEND_TIMEOUT = 10000;
    private final String destination;
    private final SendOperation sendOperation;
    private boolean sync = false;
    private ListenableFutureCallback<Void> sendCallback;
    private EvaluationContext evaluationContext;
    private Expression sendTimeoutExpression = new ValueExpression<>(DEFAULT_SEND_TIMEOUT);
    private ErrorMessageStrategy errorMessageStrategy = new DefaultErrorMessageStrategy();
    private Expression partitionKeyExpression;
    private MessageChannel sendFailureChannel;
    private String sendFailureChannelName;

    public DefaultMessageHandler(String destination, @NonNull SendOperation sendOperation) {
        Assert.hasText(destination, "destination can't be null or empty");
        this.destination = destination;
        this.sendOperation = sendOperation;
    }

    @Override
    protected void onInit() {
        super.onInit();
        this.evaluationContext = ExpressionUtils.createStandardEvaluationContext(getBeanFactory());
        LOG.info("Started DefaultMessageHandler with properties: {}", buildPropertiesMap());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleMessageInternal(Message<?> message) {

        PartitionSupplier partitionSupplier = toPartitionSupplier(message);
        String destination = toDestination(message);
        CompletableFuture<?> future = this.sendOperation.sendAsync(destination, message, partitionSupplier);

        if (this.sync) {
            waitingSendResponse(future, message);
            return;
        }

        handleSendResponseAsync(message, future);
    }

    private void handleSendResponseAsync(Message<?> message, CompletableFuture<?> future) {
        future.handle((t, ex) -> {
            if (ex != null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("{} sent failed in async mode due to {}", message, ex.getMessage());
                }
                if (this.sendCallback != null) {
                    this.sendCallback.onFailure(ex);
                }

                if (getSendFailureChannel() != null) {
                    this.messagingTemplate.send(getSendFailureChannel(),
                        getErrorMessageStrategy()
                            .buildErrorMessage(new AzureSendFailureException(message, ex), null));
                }

            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} sent successfully in async mode", message);
                }
                if (this.sendCallback != null) {
                    this.sendCallback.onSuccess((Void) t);
                }
            }

            return null;
        });
    }

    private void waitingSendResponse(CompletableFuture<?> future, Message<?> message) {
        Long sendTimeout = this.sendTimeoutExpression.getValue(this.evaluationContext, message, Long.class);
        if (sendTimeout == null || sendTimeout < 0) {
            try {
                future.get();
            } catch (Exception e) {
                throw new MessageDeliveryException(e.getMessage());
            }
        } else {
            try {
                future.get(sendTimeout, TimeUnit.MILLISECONDS);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} sent successfully in sync mode", message);
                }
            } catch (TimeoutException e) {
                throw new MessageTimeoutException(message, "Timeout waiting for send event hub response", e);
            } catch (Exception e) {
                throw new MessageDeliveryException(e.getMessage());
            }
        }
    }

    public void setSync(boolean sync) {
        this.sync = sync;
        LOG.info("DefaultMessageHandler sync becomes: {}", sync);
    }

    public void setSendTimeout(long sendTimeout) {
        setSendTimeoutExpression(new ValueExpression<>(sendTimeout));
    }

    public void setPartitionKey(String partitionKey) {
        setPartitionKeyExpression(new LiteralExpression(partitionKey));
    }

    public void setPartitionKeyExpression(Expression partitionKeyExpression) {
        this.partitionKeyExpression = partitionKeyExpression;
    }

    public void setPartitionKeyExpressionString(String partitionKeyExpression) {
        setPartitionKeyExpression(EXPRESSION_PARSER.parseExpression(partitionKeyExpression));
    }

    private String toDestination(Message<?> message) {
        if (message.getHeaders().containsKey(AzureHeaders.NAME)) {
            return message.getHeaders().get(AzureHeaders.NAME, String.class);
        }

        return this.destination;
    }

    private PartitionSupplier toPartitionSupplier(Message<?> message) {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        String partitionKey = message.getHeaders().get(AzureHeaders.PARTITION_KEY, String.class);
        if (!StringUtils.hasText(partitionKey) && this.partitionKeyExpression != null) {
            partitionKey = this.partitionKeyExpression.getValue(this.evaluationContext, message, String.class);
        }

        if (StringUtils.hasText(partitionKey)) {
            partitionSupplier.setPartitionKey(partitionKey);
        }

        if (message.getHeaders().containsKey(AzureHeaders.PARTITION_ID)) {
            partitionSupplier
                .setPartitionId(message.getHeaders().get(AzureHeaders.PARTITION_ID, String.class));
        }
        return partitionSupplier;
    }

    private Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("sync", sync);
        properties.put("sendTimeout", sendTimeoutExpression);
        properties.put("destination", destination);

        return properties;
    }

    public void setSendCallback(ListenableFutureCallback<Void> callback) {
        this.sendCallback = callback;
    }

    public Expression getSendTimeoutExpression() {
        return sendTimeoutExpression;
    }

    public void setSendTimeoutExpression(Expression sendTimeoutExpression) {
        Assert.notNull(sendTimeoutExpression, "'sendTimeoutExpression' must not be null");
        this.sendTimeoutExpression = sendTimeoutExpression;
        LOG.info("DefaultMessageHandler syncTimeout becomes: {}", sendTimeoutExpression);
    }

    protected MessageChannel getSendFailureChannel() {
        if (this.sendFailureChannel != null) {
            return this.sendFailureChannel;
        } else if (this.sendFailureChannelName != null) {
            this.sendFailureChannel = getChannelResolver().resolveDestination(this.sendFailureChannelName);
            return this.sendFailureChannel;
        }

        return null;
    }

    public void setSendFailureChannel(MessageChannel sendFailureChannel) {
        this.sendFailureChannel = sendFailureChannel;
    }

    public void setSendFailureChannelName(String sendFailureChannelName) {
        this.sendFailureChannelName = sendFailureChannelName;
    }

    protected ErrorMessageStrategy getErrorMessageStrategy() {
        return this.errorMessageStrategy;
    }

    public void setErrorMessageStrategy(ErrorMessageStrategy errorMessageStrategy) {
        Assert.notNull(errorMessageStrategy, "'errorMessageStrategy' must not be null");
        this.errorMessageStrategy = errorMessageStrategy;
    }
}
