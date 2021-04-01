// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

import com.azure.spring.integration.core.api.PartitionSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.AbstractMessageProducingHandler;
import org.springframework.integration.support.DefaultErrorMessageStrategy;
import org.springframework.integration.support.ErrorMessageStrategy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Optional;

/**
 * Base class of outbound adapter to publish to azure backed messaging service
 */
public abstract class AbstractDefaultMassageHandler extends AbstractMessageProducingHandler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultMessageHandler.class);
    protected static final long DEFAULT_SEND_TIMEOUT = 10000;
    protected boolean sync = false;
    protected ListenableFutureCallback<Void> sendCallback;
    protected EvaluationContext evaluationContext;
    protected Expression sendTimeoutExpression = new ValueExpression<>(DEFAULT_SEND_TIMEOUT);
    protected ErrorMessageStrategy errorMessageStrategy = new DefaultErrorMessageStrategy();
    protected Expression partitionKeyExpression;
    protected Expression partitionIdExpression;
    protected MessageChannel sendFailureChannel;
    protected String sendFailureChannelName;

    public void setSync(boolean sync) {
        this.sync = sync;
        LOGGER.info("DefaultMessageHandler sync becomes: {}", sync);
    }

    public void setPartitionKey(String partitionKey) {
        setPartitionKeyExpression(new LiteralExpression(partitionKey));
    }

    public void setPartitionKeyExpression(Expression partitionKeyExpression) {
        this.partitionKeyExpression = partitionKeyExpression;
    }

    public void setPartitionIdExpression(Expression partitionIdExpression) {
        this.partitionIdExpression = partitionIdExpression;
    }

    protected PartitionSupplier toPartitionSupplier(Message<?> message) {
        PartitionSupplier partitionSupplier = new PartitionSupplier();
        // Priority setting partitionId
        String partitionId = getHeaderValue(message, AzureHeaders.PARTITION_ID);
        if (!StringUtils.hasText(partitionId) && this.partitionIdExpression != null) {
            partitionId = this.partitionIdExpression.getValue(this.evaluationContext, message, String.class);
        }
        if (StringUtils.hasText(partitionId)) {
            partitionSupplier.setPartitionId(partitionId);
        } else {
            String partitionKey = getHeaderValue(message, AzureHeaders.PARTITION_KEY);
            // The default key expression is the hash code of the payload.
            if (!StringUtils.hasText(partitionKey) && this.partitionKeyExpression != null) {
                partitionKey = this.partitionKeyExpression.getValue(this.evaluationContext, message, String.class);
            }
            if (StringUtils.hasText(partitionKey)) {
                partitionSupplier.setPartitionKey(partitionKey);
            }
        }
        return partitionSupplier;
    }

    protected void handleSendResponseAsyncSuccess(Message<?> message, Throwable ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("{} sent failed in async mode due to {}", message, ex.getMessage());
        }
        if (this.sendCallback != null) {
            this.sendCallback.onFailure(ex);
        }

        if (getSendFailureChannel() != null) {
            this.messagingTemplate.send(getSendFailureChannel(),
                getErrorMessageStrategy()
                    .buildErrorMessage(new AzureSendFailureException(message, ex), null));
        }

    }

    /**
     * Get header value from MessageHeaders
     * @param message MessageHeaders
     * @param keyName Key name
     * @return String header value
     */
    protected String getHeaderValue(Message<?> message, String keyName) {
        return Optional.ofNullable(message)
            .map(Message::getHeaders)
            .map(headers -> headers.getOrDefault(keyName, null))
            .map(String::valueOf)
            .orElse(null);
    }

    public void setSendCallback(ListenableFutureCallback<Void> callback) {
        this.sendCallback = callback;
    }

    public Expression getSendTimeoutExpression() {
        return sendTimeoutExpression;
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
