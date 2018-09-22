package com.microsoft.azure.servicebus.primitives;

import com.microsoft.azure.servicebus.ClientSettings;
import com.microsoft.azure.servicebus.TransactionContext;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transaction.*;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.amqp.transport.Target;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class Controller {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(Controller.class);
    private MessagingFactory messagingFactory;
    private CoreMessageSender internalSender;
    private boolean isInitialized = false;
    private URI namespaceEndpointURI;
    private ClientSettings clientSettings;

    public Controller (URI namespaceEndpointURI, MessagingFactory factory, ClientSettings clientSettings) {
        this.namespaceEndpointURI = namespaceEndpointURI;
        this.messagingFactory = factory;
        this.clientSettings = clientSettings;
    }

    synchronized CompletableFuture<Void> initializeAsync() {
        if (this.isInitialized) {
            return CompletableFuture.completedFuture(null);
        } else {
            TRACE_LOGGER.info("Creating MessageSender to coordinator");
            CompletableFuture<CoreMessageSender> senderFuture = CoreMessageSender.create(
                    this.messagingFactory,
                    StringUtil.getShortRandomString(),
                    null,
                    Controller.getControllerLinkSettings(this.messagingFactory));
            CompletableFuture<Void> postSenderCreationFuture = new CompletableFuture<Void>();
            senderFuture.handleAsync((s, coreSenderCreationEx) -> {
                if (coreSenderCreationEx == null) {
                    this.internalSender = s;
                    this.isInitialized = true;
                    TRACE_LOGGER.info("Created MessageSender to coordinator");
                    postSenderCreationFuture.complete(null);
                } else {
                    Throwable cause = ExceptionUtil.extractAsyncCompletionCause(coreSenderCreationEx);
                    TRACE_LOGGER.error("Creating MessageSender to coordinator failed", cause);
                    this.messagingFactory.closeAsync();
                    postSenderCreationFuture.completeExceptionally(cause);
                }
                return null;
            }, MessagingFactory.INTERNAL_THREAD_POOL);
            return postSenderCreationFuture;
        }
    }

    public CompletableFuture<Binary> declareAsync() {
        Message message = Message.Factory.create();
        Declare declare = new Declare();
        message.setBody(new AmqpValue(declare));        

        return this.internalSender.sendAndReturnDeliveryStateAsync(
                message,
                TransactionContext.NULL_TXN)
                .thenApply( state -> {
                    Binary txnId = null;
                    if (state instanceof Declared) {
                        Declared declared = (Declared) state;
                        txnId = declared.getTxnId();
                        TRACE_LOGGER.debug("New TX started: {}", txnId);
                    } else {
                        CompletableFuture<String> exceptionFuture = new CompletableFuture<>();
                        exceptionFuture.completeExceptionally(new UnsupportedOperationException("Received unknown state: " + state.toString()));
                    }

                    return txnId;
                });
    }

    public CompletableFuture<Void> dischargeAsync(Binary txnId, boolean isCommit) {
        Message message = Message.Factory.create();
        Discharge discharge = new Discharge();
        discharge.setFail(!isCommit);
        discharge.setTxnId(txnId);
        message.setBody(new AmqpValue(discharge));        

        return this.internalSender.sendAndReturnDeliveryStateAsync(
                message,
                TransactionContext.NULL_TXN)
                .thenCompose( state -> {
                    if (state instanceof Accepted) {
                        return CompletableFuture.completedFuture(null);
                    }
                    else {
                        CompletableFuture<Void> returnTask = new CompletableFuture<>();
                        returnTask.completeExceptionally(new UnsupportedOperationException("Received unknown state: " + state.toString()));
                        return returnTask;
                    }
                });
    }

    protected CompletableFuture<Void> closeAsync() {
        return null;
    }

    private static SenderLinkSettings getControllerLinkSettings(MessagingFactory underlyingFactory)
    {
        SenderLinkSettings linkSettings = new SenderLinkSettings();
        linkSettings.linkPath = "coordinator";

        final Target target = new Coordinator();
        linkSettings.target = target;
        linkSettings.source = new Source();
        linkSettings.settleMode = SenderSettleMode.UNSETTLED;
        linkSettings.requiresAuthentication = false;

        Map<Symbol, Object> linkProperties = new HashMap<>();
        // ServiceBus expects timeout to be of type unsignedint
        linkProperties.put(ClientConstants.LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(Util.adjustServerTimeout(underlyingFactory.getOperationTimeout()).toMillis()));
        linkSettings.linkProperties = linkProperties;

        return linkSettings;
    }
}
