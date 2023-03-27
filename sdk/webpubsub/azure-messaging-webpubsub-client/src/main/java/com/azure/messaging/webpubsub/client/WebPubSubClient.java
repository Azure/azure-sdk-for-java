// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.EventHandlerCollection;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.implementation.ws.Session;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.models.EventHandler;
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

/**
 * The WebPubSubAsync client.
 */
@ServiceClient(builder = WebPubSubClientBuilder.class)
public class WebPubSubClient {

    private final WebPubSubAsyncClient client;

    private final EventHandlerCollection eventHandlerCollection = new EventHandlerCollection();
    private static final String GROUP_MESSAGE_EVENT = "GroupMessageEvent";
    private static final String SERVER_MESSAGE_EVENT = "ServerMessageEvent";
    private static final String CONNECT_EVENT = "ConnectedEvent";
    private static final String DISCONNECT_EVENT = "DisconnectedEvent";
    private static final String STOPPED_EVENT = "StoppedEvent";
    private static final String REJOIN_GROUP_FAILED_EVENT = "RejoinGroupFailedEvent";

    WebPubSubClient(WebPubSubAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the connection ID.
     *
     * @return the connection ID.
     */
    public String getConnectionId() {
        return this.client.getConnectionId();
    }

    /**
     * Starts the client for connecting to the server.
     *
     * @exception IllegalStateException thrown if client is not currently stopped.
     * @exception ConnectFailedException thrown if failed to connect to server.
     */
    public void start() {
        client.start(() -> {
            this.client.receiveGroupMessageEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(GROUP_MESSAGE_EVENT, event));
            this.client.receiveServerMessageEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(SERVER_MESSAGE_EVENT, event));
            this.client.receiveConnectedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(CONNECT_EVENT, event));
            this.client.receiveDisconnectedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(DISCONNECT_EVENT, event));
            this.client.receiveStoppedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(STOPPED_EVENT, event));
            this.client.receiveRejoinGroupFailedEvents().publishOn(Schedulers.boundedElastic())
                .subscribe(event -> eventHandlerCollection.fireEvent(REJOIN_GROUP_FAILED_EVENT, event));
        }).block();
    }

    /**
     * Stops the client for disconnecting from the server.
     *
     * @exception ConnectFailedException thrown if failed to disconnect from server, or other failure.
     */
    public void stop() {
        client.stop().block();
    }

    /**
     * Adds event handler for GroupMessageEvent.
     *
     * @param eventEventHandler the event handler for GroupMessageEvent.
     */
    public void addOnGroupMessageEventHandler(EventHandler<GroupMessageEvent> eventEventHandler) {
        eventHandlerCollection.addEventHandler(GROUP_MESSAGE_EVENT, eventEventHandler);
    }

    /**
     * Removes event handler for GroupMessageEvent.
     *
     * @param eventEventHandler the event handler for GroupMessageEvent.
     */
    public void removeOnGroupMessageEventHandler(EventHandler<GroupMessageEvent> eventEventHandler) {
        eventHandlerCollection.removeEventHandler(GROUP_MESSAGE_EVENT, eventEventHandler);
    }

    /**
     * Adds event handler for ServerMessageEvent.
     *
     * @param eventEventHandler the event handler for ServerMessageEvent.
     */
    public void addOnServerMessageEventHandler(EventHandler<ServerMessageEvent> eventEventHandler) {
        eventHandlerCollection.addEventHandler(SERVER_MESSAGE_EVENT, eventEventHandler);
    }

    /**
     * Removes event handler for ServerMessageEvent.
     *
     * @param eventEventHandler the event handler for ServerMessageEvent.
     */
    public void removeOnServerMessageEventHandler(EventHandler<ServerMessageEvent> eventEventHandler) {
        eventHandlerCollection.removeEventHandler(SERVER_MESSAGE_EVENT, eventEventHandler);
    }

    /**
     * Adds event handler for ConnectedEvent.
     *
     * @param eventEventHandler the event handler for ConnectedEvent.
     */
    public void addOnConnectedEventHandler(EventHandler<ConnectedEvent> eventEventHandler) {
        eventHandlerCollection.addEventHandler(CONNECT_EVENT, eventEventHandler);
    }

    /**
     * Removes event handler for ConnectedEvent.
     *
     * @param eventEventHandler the event handler for ConnectedEvent.
     */
    public void removeOnConnectedEventHandler(EventHandler<ConnectedEvent> eventEventHandler) {
        eventHandlerCollection.removeEventHandler(CONNECT_EVENT, eventEventHandler);
    }

    /**
     * Adds event handler for DisconnectedEvent.
     *
     * @param eventEventHandler the event handler for DisconnectedEvent.
     */
    public void addOnDisconnectedEventHandler(EventHandler<DisconnectedEvent> eventEventHandler) {
        eventHandlerCollection.addEventHandler(DISCONNECT_EVENT, eventEventHandler);
    }

    /**
     * Removes event handler for DisconnectedEvent.
     *
     * @param eventEventHandler the event handler for DisconnectedEvent.
     */
    public void removeOnDisconnectedEventHandler(EventHandler<DisconnectedEvent> eventEventHandler) {
        eventHandlerCollection.removeEventHandler(DISCONNECT_EVENT, eventEventHandler);
    }

    /**
     * Adds event handler for StoppedEvent.
     *
     * @param eventEventHandler the event handler for StoppedEvent.
     */
    public void addOnStoppedEventHandler(EventHandler<StoppedEvent> eventEventHandler) {
        eventHandlerCollection.addEventHandler(STOPPED_EVENT, eventEventHandler);
    }

    /**
     * Removes event handler for StoppedEvent.
     *
     * @param eventEventHandler the event handler for StoppedEvent.
     */
    public void removeOnStoppedEventHandler(EventHandler<StoppedEvent> eventEventHandler) {
        eventHandlerCollection.removeEventHandler(STOPPED_EVENT, eventEventHandler);
    }

    /**
     * Adds event handler for RejoinGroupFailedEvent.
     *
     * @param eventEventHandler the event handler for RejoinGroupFailedEvent.
     */
    public void addOnRejoinGroupFailedEventHandler(EventHandler<RejoinGroupFailedEvent> eventEventHandler) {
        eventHandlerCollection.addEventHandler(REJOIN_GROUP_FAILED_EVENT, eventEventHandler);
    }

    /**
     * Removes event handler for RejoinGroupFailedEvent.
     *
     * @param eventEventHandler the event handler for RejoinGroupFailedEvent.
     */
    public void removeOnRejoinGroupFailedEventHandler(EventHandler<RejoinGroupFailedEvent> eventEventHandler) {
        eventHandlerCollection.removeEventHandler(REJOIN_GROUP_FAILED_EVENT, eventEventHandler);
    }

//    /**
//     * Closes the client.
//     */
//    @Override
//    public void close() {
//        client.close();
//    }

    /**
     * Joins a group.
     *
     * @param group the group name.
     * @exception SendMessageFailedException thrown if client not connected, or join group message failed.
     * @return the result.
     */
    public WebPubSubResult joinGroup(String group) {
        return client.joinGroup(group).block();
    }

    /**
     * Joins a group.
     *
     * @param group the group name.
     * @param ackId the ackId.
     * @exception SendMessageFailedException thrown if client not connected, or join group message failed.
     * @return the result.
     */
    public WebPubSubResult joinGroup(String group, long ackId) {
        return client.joinGroup(group, ackId).block();
    }

    /**
     * Leaves a group.
     *
     * @param group the group name.
     * @exception SendMessageFailedException thrown if client not connected, or leave group message failed.
     * @return the result.
     */
    public WebPubSubResult leaveGroup(String group) {
        return client.leaveGroup(group).block();
    }

    /**
     * Leaves a group.
     *
     * @param group the group name.
     * @param ackId the ackId.
     * @exception SendMessageFailedException thrown if client not connected, or leave group message failed.
     * @return the result.
     */
    public WebPubSubResult leaveGroup(String group, long ackId) {
        return client.leaveGroup(group, ackId).block();
    }

    /**
     * Sends message to group.
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
     *
     * @param group the group name.
     * @param content the data.
     * @param dataType the data type.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return client.sendToGroup(group, content, dataType).block();
    }

    /**
     * Sends message to group.
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
        return client.sendToGroup(group, content, dataType, options).block();
    }

    /**
     * Sends event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataType the data type.
     * @exception SendMessageFailedException thrown if client not connected, or send group message failed.
     * @return the result.
     */
    public WebPubSubResult sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType) {
        return client.sendEvent(eventName, content, dataType).block();
    }

    /**
     * Sends event.
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
        return client.sendEvent(eventName, content, dataType, options).block();
    }

    WebPubSubClientState getClientState() {
        return this.client.getClientState();
    }

    Session getWebsocketSession() {
        return this.client.getWebsocketSession();
    }
}
