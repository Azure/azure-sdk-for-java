// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

/** The ChatMessage model. */
@Fluent
public final class ChatMessage {
    /*
     * The id of the chat message.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * Type of the chat message.
     *
     */
    @JsonProperty(value = "type", required = true)
    private ChatMessageType type;

    /*
     * Version of the chat message.
     */
    @JsonProperty(value = "version", required = true)
    private String version;

    /*
     * Content of the chat message.
     */
    @JsonProperty(value = "content")
    private ChatMessageContent content;

    /*
     * The display name of the chat message sender. This property is used to
     * populate sender name for push notifications.
     */
    @JsonProperty(value = "senderDisplayName")
    private String senderDisplayName;

    /*
     * The timestamp when the chat message arrived at the server. The timestamp
     * is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "createdOn")
    private OffsetDateTime createdOn;

    /*
     * Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This
     * model must be interpreted as a union: Apart from rawId, at most one
     * further property may be set.
     */
    @JsonProperty(value = "senderCommunicationIdentifier")
    private CommunicationIdentifier sender;

    /*
     * The timestamp when the chat message was deleted. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "deletedOn")
    private OffsetDateTime deletedOn;

    /*
     * The timestamp when the chat message was edited. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    @JsonProperty(value = "editedOn")
    private OffsetDateTime editedOn;

    /**
     * Get the id property: The id of the chat message. This id is server generated.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: The id of the chat message.
     *
     * @param id the id to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the type property: Type of the chat message.
     *
     * <p>Possible values: - Text - ThreadActivity/TopicUpdate - ThreadActivity/AddMember - ThreadActivity/DeleteMember.
     *
     * @return the type value.
     */
    public ChatMessageType getType() {
        return this.type;
    }

    /**
     * Set the type property: Type of the chat message.
     *
     * <p>Possible values: - Text - ThreadActivity/TopicUpdate - ThreadActivity/AddMember - ThreadActivity/DeleteMember.
     *
     * @param type the type value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setType(ChatMessageType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the version property: Version of the chat message.
     *
     * @return the version value.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Set the version property: Version of the chat message.
     *
     * @param version the version to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get the content property: Content of the chat message.
     *
     * @return the content value.
     */
    public ChatMessageContent getContent() {
        return this.content;
    }

    /**
     * Set the content property: Content of the chat message.
     *
     * @param content the content value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setContent(ChatMessageContent content) {
        this.content = content;
        return this;
    }

    /**
     * Get the senderDisplayName property: The display name of the chat message sender. This property is used to
     * populate sender name for push notifications.
     *
     * @return the senderDisplayName value.
     */
    public String getSenderDisplayName() {
        return this.senderDisplayName;
    }

    /**
     * Set the senderDisplayName property: The display name of the chat message sender. This property is used to
     * populate sender name for push notifications.
     *
     * @param senderDisplayName the senderDisplayName value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
        return this;
    }

    /**
     * Get the createdOn property: The timestamp when the chat message arrived at the server. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Set the createdOn property: The timestamp when the chat message arrived at the server. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param createdOn the createdOn value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /**
     * Get the sender property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @return the sender value.
     */
    public CommunicationIdentifier getSender() {
        return this.sender;
    }

    /**
     * Set the sender property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @param sender the sender value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setSender(CommunicationIdentifier sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Get the deletedOn property: The timestamp when the chat message was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the deletedOn value.
     */
    public OffsetDateTime getDeletedOn() {
        return this.deletedOn;
    }

    /**
     * Set the deletedOn property: The timestamp when the chat message was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param deletedOn the deletedOn value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setDeletedOn(OffsetDateTime deletedOn) {
        this.deletedOn = deletedOn;
        return this;
    }

    /**
     * Get the editedOn property: The timestamp when the chat message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the editedOn value.
     */
    public OffsetDateTime getEditedOn() {
        return this.editedOn;
    }

    /**
     * Set the editedOn property: The timestamp when the chat message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param editedOn the editedOn value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setEditedOn(OffsetDateTime editedOn) {
        this.editedOn = editedOn;
        return this;
    }
}
