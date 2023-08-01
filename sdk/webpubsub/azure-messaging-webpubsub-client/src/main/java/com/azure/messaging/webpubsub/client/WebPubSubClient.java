// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.EventHandlerCollection;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketSession;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.RejoinGroupFailedEvent;
import com.azure.messaging.webpubsub.client.models.SendEventOptions;
import com.azure.messaging.webpubsub.client.models.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.ServerMessageEvent;
import com.azure.messaging.webpubsub.client.models.StoppedEvent;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

/**
 * The WebPubSub client that manages the WebSocket connections, join group, and send messages.
 * <p>
 * This client is instantiated through {@link WebPubSubClientBuilder}.
 * <p>
 * Please refer to <a href="https://aka.ms/awps/doc">Azure Web PubSub</a> for more information.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <!-- src_embed com.azure.messaging.webpubsub.client.WebPubSubClient -->
 * <pre>
 * &#47;&#47; create WebPubSub client
 * WebPubSubClient client = new WebPubSubClientBuilder&#40;&#41;
 *     .clientAccessUrl&#40;&quot;&lt;client-access-url&gt;&quot;&#41;
 *     .buildClient&#40;&#41;;
 *
 * &#47;&#47; add event handler for group message
 * client.addOnGroupMessageEventHandler&#40;event -&gt; &#123;
 *     System.out.println&#40;&quot;Received group message from &quot; + event.getFromUserId&#40;&#41; + &quot;: &quot;
 *         + event.getData&#40;&#41;.toString&#40;&#41;&#41;;
 * &#125;&#41;;
 *
 * &#47;&#47; start
 * client.start&#40;&#41;;
 * &#47;&#47; join group
 * client.joinGroup&#40;&quot;message-group&quot;&#41;;
 * &#47;&#47; send message
 * client.sendToGroup&#40;&quot;message-group&quot;, &quot;hello world&quot;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.webpubsub.client.WebPubSubClient -->
 */
@ServiceClient(builder = WebPubSubClientBuilder.class)
public class WebPubSubClient {

    private final WebPubSubAsyncClient asyncClient;

    private final EventHandlerCollection eventHandlerCollection = new EventHandlerCollection();
    private static final String GROUP_MESSAGE_EVENT = "GroupMessageEvent";
    private static final String SERVER_MESSAGE_EVENT = "ServerMessageEvent";
    private static final String CONNECT_EVENT = "ConnectedEvent";
    private static final String DISCONNECT_EVENT = "DisconnectedEvent";
    private static final String STOPPED_EVENT = "StoppedEvent";
    private static final String REJOIN_GROUP_FAILED_EVENT = "RejoinGroupFailedEvent";

    WebPubSubClient(WebPubSubAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets the connection ID.
     * <p>
     * Connection ID may not be available immediately after client start. It is provided by server via ConnectedEvent.
     *
     * @return the connection ID.
     */
    public String getConnectionId() {
        return this.asyncClient.getConnectionId();
    }

    /**
     * Starts the client for connecting to the server.
     * <p>
     * Client is required to be started, before message to server can be sent via
     * {@link #joinGroup(String)} or {@link #sendToGroup(String, String)}.
     * A WebSocket connection is established between the client and the Web PubSub server/hub.
     * <p>
     * Event handler can be added before client start, via e.g. {@link #addOnGroupMessageEventHandler(Consumer)}.
     *
     * @exception IllegalStateException thrown if client is not currently stopped.
     * @exception ConnectFailedException thrown if failed to connect to server.
     */
    public synchronized void start() {
        asyncClient.start(() -> {
            this.asyncClient.receiveGroupMessageEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(GROUP_MESSAGE_EVENT, event));
            this.asyncClient.receiveServerMessageEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(SERVER_MESSAGE_EVENT, event));
            this.asyncClient.receiveConnectedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(CONNECT_EVENT, event));
            this.asyncClient.receiveDisconnectedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(DISCONNECT_EVENT, event));
            this.asyncClient.receiveStoppedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(STOPPED_EVENT, event));
            this.asyncClient.receiveRejoinGroupFailedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(REJOIN_GROUP_FAILED_EVENT, event));
        }).block();
    }

    /**
     * Stops the client for disconnecting from the server.
     * <p>
     * The WebSocket connection is closed.
     *
     * @exception ConnectFailedException thrown if failed to disconnect from server, or other failure.
     */
    public synchronized void stop() {
        asyncClient.stop().block();
    }

    /**
     * Adds event handler for GroupMessageEvent.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed readme-sample-listenMessages -->
     * <pre>
     * client.addOnGroupMessageEventHandler&#40;event -&gt; &#123;
     *     System.out.println&#40;&quot;Received group message from &quot; + event.getFromUserId&#40;&#41; + &quot;: &quot;
     *         + event.getData&#40;&#41;.toString&#40;&#41;&#41;;
     * &#125;&#41;;
     * client.addOnServerMessageEventHandler&#40;event -&gt; &#123;
     *     System.out.println&#40;&quot;Received server message: &quot;
     *         + event.getData&#40;&#41;.toString&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end readme-sample-listenMessages -->
     *
     * @param onGroupMessageEventHandler the event handler for GroupMessageEvent.
     */
    public void addOnGroupMessageEventHandler(Consumer<GroupMessageEvent> onGroupMessageEventHandler) {
        eventHandlerCollection.addEventHandler(GROUP_MESSAGE_EVENT, onGroupMessageEventHandler);
    }

    /**
     * Removes event handler for GroupMessageEvent.
     *
     * @param onGroupMessageEventHandler the event handler for GroupMessageEvent.
     */
    public void removeOnGroupMessageEventHandler(Consumer<GroupMessageEvent> onGroupMessageEventHandler) {
        eventHandlerCollection.removeEventHandler(GROUP_MESSAGE_EVENT, onGroupMessageEventHandler);
    }

    /**
     * Adds event handler for ServerMessageEvent.
     *
     * @param onServiceMessageEventHandler the event handler for ServerMessageEvent.
     */
    public void addOnServerMessageEventHandler(Consumer<ServerMessageEvent> onServiceMessageEventHandler) {
        eventHandlerCollection.addEventHandler(SERVER_MESSAGE_EVENT, onServiceMessageEventHandler);
    }

    /**
     * Removes event handler for ServerMessageEvent.
     *
     * @param onServiceMessageEventHandler the event handler for ServerMessageEvent.
     */
    public void removeOnServerMessageEventHandler(Consumer<ServerMessageEvent> onServiceMessageEventHandler) {
        eventHandlerCollection.removeEventHandler(SERVER_MESSAGE_EVENT, onServiceMessageEventHandler);
    }

    /**
     * Adds event handler for ConnectedEvent.
     *
     * @param onConnectedEventHandler the event handler for ConnectedEvent.
     */
    public void addOnConnectedEventHandler(Consumer<ConnectedEvent> onConnectedEventHandler) {
        eventHandlerCollection.addEventHandler(CONNECT_EVENT, onConnectedEventHandler);
    }

    /**
     * Removes event handler for ConnectedEvent.
     *
     * @param onConnectedEventHandler the event handler for ConnectedEvent.
     */
    public void removeOnConnectedEventHandler(Consumer<ConnectedEvent> onConnectedEventHandler) {
        eventHandlerCollection.removeEventHandler(CONNECT_EVENT, onConnectedEventHandler);
    }

    /**
     * Adds event handler for DisconnectedEvent.
     *
     * @param onDisconnectedEventHandler the event handler for DisconnectedEvent.
     */
    public void addOnDisconnectedEventHandler(Consumer<DisconnectedEvent> onDisconnectedEventHandler) {
        eventHandlerCollection.addEventHandler(DISCONNECT_EVENT, onDisconnectedEventHandler);
    }

    /**
     * Removes event handler for DisconnectedEvent.
     *
     * @param onDisconnectedEventHandler the event handler for DisconnectedEvent.
     */
    public void removeOnDisconnectedEventHandler(Consumer<DisconnectedEvent> onDisconnectedEventHandler) {
        eventHandlerCollection.removeEventHandler(DISCONNECT_EVENT, onDisconnectedEventHandler);
    }

    /**
     * Adds event handler for StoppedEvent.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.messaging.webpubsub.client.WebPubSubClient.addOnStoppedEventHandler -->
     * <pre>
     * client.addOnStoppedEventHandler&#40;event -&gt; &#123;
     *     System.out.println&#40;&quot;Client is stopped&quot;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.webpubsub.client.WebPubSubClient.addOnStoppedEventHandler -->
     *
     * @param onStoppedEventHandler the event handler for StoppedEvent.
     */
    public void addOnStoppedEventHandler(Consumer<StoppedEvent> onStoppedEventHandler) {
        eventHandlerCollection.addEventHandler(STOPPED_EVENT, onStoppedEventHandler);
    }

    /**
     * Removes event handler for StoppedEvent.
     *
     * @param onStoppedEventHandler the event handler for StoppedEvent.
     */
    public void removeOnStoppedEventHandler(Consumer<StoppedEvent> onStoppedEventHandler) {
        eventHandlerCollection.removeEventHandler(STOPPED_EVENT, onStoppedEventHandler);
    }

    /**
     * Adds event handler for RejoinGroupFailedEvent.
     *
     * @param onRejoinGroupFailedEventHandler the event handler for RejoinGroupFailedEvent.
     */
    public void addOnRejoinGroupFailedEventHandler(
        Consumer<RejoinGroupFailedEvent> onRejoinGroupFailedEventHandler) {
        eventHandlerCollection.addEventHandler(REJOIN_GROUP_FAILED_EVENT, onRejoinGroupFailedEventHandler);
    }

    /**
     * Removes event handler for RejoinGroupFailedEvent.
     *
     * @param onRejoinGroupFailedEventHandler the event handler for RejoinGroupFailedEvent.
     */
    public void removeOnRejoinGroupFailedEventHandler(
        Consumer<RejoinGroupFailedEvent> onRejoinGroupFailedEventHandler) {
        eventHandlerCollection.removeEventHandler(REJOIN_GROUP_FAILED_EVENT, onRejoinGroupFailedEventHandler);
    }

    /**
     * Joins a group.
     * <p>
     * {@link #start()} the client, before join group.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.messaging.webpubsub.client.WebPubSubClient.joinGroup -->
     * <pre>
     * client.start&#40;&#41;;
     * client.joinGroup&#40;&quot;message-group&quot;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.webpubsub.client.WebPubSubClient.joinGroup -->
     *
     * @param group the group name.
     * @exception SendMessageFailedException thrown if client not connected, or join group message failed.
     * @return the result.
     */
    public WebPubSubResult joinGroup(String group) {
        return asyncClient.joinGroup(group).block();
    }

    /**
     * Joins a group.
     * <p>
     * {@link #start()} the client, before join group.
     *
     * @param group the group name.
     * @param ackId the ackId. Client will provide auto increment ID, if set to {@code null}.
     * @exception SendMessageFailedException thrown if client not connected, or join group message failed.
     * @return the result.
     */
    public WebPubSubResult joinGroup(String group, Long ackId) {
        return asyncClient.joinGroup(group, ackId).block();
    }

    /**
     * Leaves a group.
     *
     * @param group the group name.
     * @exception SendMessageFailedException thrown if client not connected, or leave group message failed.
     * @return the result.
     */
    public WebPubSubResult leaveGroup(String group) {
        return asyncClient.leaveGroup(group).block();
    }

    /**
     * Leaves a group.
     *
     * @param group the group name.
     * @param ackId the ackId. Client will provide auto increment ID, if set to {@code null}.
     * @exception SendMessageFailedException thrown if client not connected, or leave group message failed.
     * @return the result.
     */
    public WebPubSubResult leaveGroup(String group, Long ackId) {
        return asyncClient.leaveGroup(group, ackId).block();
    }

    /**
     * Sends message to group.
     * <p>
     * {@link #start()} the client, before send message.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.text -->
     * <pre>
     * client.start&#40;&#41;;
     * client.sendToGroup&#40;&quot;message-group&quot;, &quot;hello world&quot;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.text -->
     *
     * @param group the group name.
     * @param content the data as WebPubSubDataType.TEXT.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendToGroup(String group, String content) {
        return sendToGroup(group, BinaryData.fromString(content), WebPubSubDataType.TEXT);
    }

    /**
     * Sends message to group.
     * <p>
     * {@link #start()} the client, before send message.
     *
     * @param group the group name.
     * @param content the data as WebPubSubDataType.TEXT.
     * @param options the options.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendToGroup(String group, String content, SendToGroupOptions options) {
        return sendToGroup(group, BinaryData.fromString(content), WebPubSubDataType.TEXT, options);
    }

    /**
     * Sends message to group.
     * <p>
     * {@link #start()} the client, before send message.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.json -->
     * <pre>
     * client.start&#40;&#41;;
     * &#47;&#47; it can be any class instance that can be serialized to JSON
     * Map&lt;String, String&gt; jsonObject = new HashMap&lt;&gt;&#40;&#41;;
     * jsonObject.put&#40;&quot;name&quot;, &quot;john&quot;&#41;;
     * client.sendToGroup&#40;&quot;message-group&quot;, BinaryData.fromObject&#40;jsonObject&#41;, WebPubSubDataType.BINARY&#41;;
     * </pre>
     * <!-- end com.azure.messaging.webpubsub.client.WebPubSubClient.sendToGroup.json -->
     *
     * @param group the group name.
     * @param content the data.
     * @param dataType the data type.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return asyncClient.sendToGroup(group, content, dataType).block();
    }

    /**
     * Sends message to group.
     * <p>
     * {@link #start()} the client, before send message.
     *
     * @param group the group name.
     * @param content the data.
     * @param dataType the data type.
     * @param options the options.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                       SendToGroupOptions options) {
        return asyncClient.sendToGroup(group, content, dataType, options).block();
    }

    /**
     * Sends event.
     * <p>
     * {@link #start()} the client, before send event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataType the data type.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType) {
        return asyncClient.sendEvent(eventName, content, dataType).block();
    }

    /**
     * Sends event.
     * <p>
     * {@link #start()} the client, before send event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataType the data type.
     * @param options the options.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType,
                                           SendEventOptions options) {
        return asyncClient.sendEvent(eventName, content, dataType, options).block();
    }

    // following API is for testing
    WebPubSubClientState getClientState() {
        return this.asyncClient.getClientState();
    }
    WebSocketSession getWebsocketSession() {
        return this.asyncClient.getWebsocketSession();
    }
}
