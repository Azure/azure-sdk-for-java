package com.azure.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestMode;
import com.azure.eventhubs.CredentialInfo;
import com.azure.eventhubs.implementation.handler.ReceiveLinkHandler;
import com.azure.eventhubs.implementation.handler.SendLinkHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CBSChannelTest extends ApiTestBase {
    private static final String CONNECTION_ID = "CbsChannelTest-Connection";

    private AmqpResponseMapper mapper = mock(AmqpResponseMapper.class);
    private Session mockSession;

    @Rule
    public TestName testName = new TestName();

    private AmqpConnection connection;
    private CBSChannel cbsChannel;
    private CredentialInfo credentials;
    private SessionHandler mockSessionHandler;
    private SendLinkHandler mockSendLinkHandler;
    private ReceiveLinkHandler mockReceiveLinkHandler;
    private ReactorHandlerProvider handlerProvider;

    @Override
    protected String testName() {
        return testName.getMethodName();
    }

    @Override
    protected void beforeTest() {
        credentials = getCredentialInfo();

        if (getTestMode() == TestMode.RECORD) {
            handlerProvider = new ReactorHandlerProvider(getReactorProvider());
            connection = new ReactorConnection(CONNECTION_ID, getConnectionParameters(), getReactorProvider(), handlerProvider, mapper);
        } else {
            setupMocks();
            handlerProvider = new MockReactorHandlerProvider(getReactorProvider(), null, null, mockSendLinkHandler, mockReceiveLinkHandler);
        }

        cbsChannel = new CBSChannel(connection, getTokenProvider(), getReactorProvider(), handlerProvider);
    }

    @Override
    protected void afterTest() {
        cbsChannel.close();

        try {
            connection.close();
        } catch (IOException e) {
            Assert.fail("Could not close connection." + e.toString());
        }
    }

    @Test
    public void successfullyAuthorizes() {
        skipIfNotRecordMode();

        // Arrange
        final String tokenAudience = String.format(ClientConstants.TOKEN_AUDIENCE_FORMAT, credentials.endpoint().getHost(), credentials.eventHubPath());
        final Duration duration = Duration.ofMinutes(10);

        // Act & Assert
        StepVerifier.create(cbsChannel.authorize(tokenAudience, duration))
            .verifyComplete();
    }

    @Test
    public void unsuccessfulAuthorize() {
        skipIfNotRecordMode();

        // Arrange
        final String tokenAudience = String.format(ClientConstants.TOKEN_AUDIENCE_FORMAT, credentials.endpoint().getHost(), credentials.eventHubPath());
        final Duration duration = Duration.ofMinutes(10);

        TokenProvider tokenProvider = null;
        try {
            tokenProvider = new SharedAccessSignatureTokenProvider(credentials.sharedAccessKeyName(), "Invalid shared access key.");
        } catch (Exception e) {
            Assert.fail("Could not create token provider: " + e.toString());
        }

        final CBSNode node = new CBSChannel(connection, tokenProvider, getReactorProvider(), handlerProvider);

        // Act & Assert
        StepVerifier.create(node.authorize(tokenAudience, duration))
            .expectErrorSatisfies(error -> {
                Assert.assertTrue(error instanceof AmqpException);

                AmqpException exception = (AmqpException) error;
                Assert.assertEquals(ErrorCondition.UNAUTHORIZED_ACCESS, exception.getErrorCondition());
                Assert.assertFalse(exception.isTransient());
                Assert.assertFalse(ImplUtils.isNullOrEmpty(exception.getMessage()));
            })
            .verify();
    }

    private void setupMocks() {
        final String host = credentials.endpoint().getHost();
        final Duration duration = Duration.ofSeconds(30);
        final ReactorProvider provider = getReactorProvider();

        connection = mock(AmqpConnection.class);
        when(connection.getIdentifier()).thenReturn(CONNECTION_ID);
        when(connection.getHost()).thenReturn(host);

        mockSessionHandler = new SessionHandler(CONNECTION_ID, host, CBSChannel.SESSION_NAME, provider.getReactorDispatcher(), duration);

        when(connection.createSession(CBSChannel.SESSION_NAME)).thenReturn(
            Mono.just(new ReactorSession(mockSession, mockSessionHandler, CBSChannel.SESSION_NAME, provider, duration)));

        // These are for setting up the RequestResponse Channel.
        mockSession = mock(Session.class);
        Sender sender = mock(Sender.class);
        when(mockSession.sender(any())).thenReturn(sender);
        when(sender.attachments()).thenReturn(mock(Record.class));

        Receiver receiver = mock(Receiver.class);
        when(mockSession.receiver(any())).thenReturn(receiver);
        when(receiver.attachments()).thenReturn(mock(Record.class));

        // Setting up the connection status for the send and receive links as opened.
        mockSendLinkHandler = new SendLinkHandler(CONNECTION_ID, host, "test-sender-cbs");
        Event openSendEvent = mock(Event.class);
        when(openSendEvent.getLink()).thenReturn(sender);
        mockSendLinkHandler.onLinkRemoteOpen(openSendEvent);

        mockReceiveLinkHandler = new ReceiveLinkHandler(CONNECTION_ID, host, "test-receiver-cbs");
        Event openReceiveEvent = mock(Event.class);
        when(openReceiveEvent.getLink()).thenReturn(receiver);
        mockReceiveLinkHandler.onLinkRemoteOpen(openReceiveEvent);
    }
}
