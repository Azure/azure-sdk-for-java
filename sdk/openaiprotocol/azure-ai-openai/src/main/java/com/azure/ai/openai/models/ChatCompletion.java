package com.azure.ai.openai.models;

import com.azure.ai.openai.implementation.models.CompletionsUsage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class ChatCompletion {

    /*
     * A unique identifier associated with this chat completions response.
     */

    @JsonProperty(value = "id")
    private String id;

    /*
     * The first timestamp associated with generation activity for this completions response,
     * represented as seconds since the beginning of the Unix epoch of 00:00 on 1 Jan 1970.
     */

    @JsonProperty(value = "created")
    private long createdAt;


    /*
     * Can be used in conjunction with the `seed` request parameter to understand when backend changes have been made
     * that
     * might impact determinism.
     */

    @JsonProperty(value = "system_fingerprint")
    private String systemFingerprint;

    /*
     * Usage information for tokens processed and generated as part of this completions operation.
     */

    @JsonProperty(value = "usage")
    private CompletionsUsage usage;

    private String content;

    private ChatRole chatRole;

    private ChatCompletionFinishReason finishReason;

    /**
     * Creates an instance of ChatCompletions class.
     *
     * @param id the id value to set.
     * @param createdAt the createdAt value to set.
     * @param choices the choices value to set.
     * @param usage the usage value to set.
     */

    private ChatCompletion(String id, OffsetDateTime createdAt, List<ChatChoice> choices, CompletionsUsage usage) {
        this.id = id;
        this.createdAt = createdAt.toEpochSecond();
        this.usage = usage;
    }


    @JsonCreator
    private ChatCompletion(@JsonProperty(value = "id") String id, @JsonProperty(value = "created") long createdAt,
                           @JsonProperty(value = "choices") List<ChatChoice> choices,
                           @JsonProperty(value = "usage") CompletionsUsage usage) {
        this(id, OffsetDateTime.ofInstant(Instant.ofEpochSecond(createdAt), ZoneOffset.UTC), choices, usage);
    }

    /**
     * Get the id property: A unique identifier associated with this chat completions response.
     *
     * @return the id value.
     */

    public String getId() {
        return this.id;
    }

    /**
     * Get the createdAt property: The first timestamp associated with generation activity for this completions
     * response,
     * represented as seconds since the beginning of the Unix epoch of 00:00 on 1 Jan 1970.
     *
     * @return the createdAt value.
     */

    public OffsetDateTime getCreatedAt() {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(this.createdAt), ZoneOffset.UTC);
    }

    /**
     * Get the systemFingerprint property: Can be used in conjunction with the `seed` request parameter to understand
     * when backend changes have been made that
     * might impact determinism.
     *
     * @return the systemFingerprint value.
     */

    public String getSystemFingerprint() {
        return this.systemFingerprint;
    }

    /**
     * Get the usage property: Usage information for tokens processed and generated as part of this completions
     * operation.
     *
     * @return the usage value.
     */

    public CompletionsUsage getUsage() {
        return this.usage;
    }

    /**
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * @return
     */
    public ChatRole getChatRole() {
        return chatRole;
    }

    /**
     * @return
     */
    public ChatCompletionFinishReason getFinishReason() {
        return finishReason;
    }
}
