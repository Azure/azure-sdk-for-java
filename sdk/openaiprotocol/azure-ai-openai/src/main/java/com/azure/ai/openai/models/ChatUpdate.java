package com.azure.ai.openai.models;

import java.time.OffsetDateTime;

/**
 *
 */
public final class ChatUpdate {
    private String authorName;
    private Integer choiceIndex;
    private String contentUpdate;
    private OffsetDateTime createdAt;
    private ChatCompletionFinishReason finishReason;
    private String id;
    private ChatRole role;
    private String systemFingerprint;


    /**
     * @return
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * @return
     */
    public Integer getChoiceIndex() {
        return choiceIndex;
    }

    /**
     * @return
     */
    public String getContentUpdate() {
        return contentUpdate;
    }

    /**
     * @return
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @return
     */
    public ChatCompletionFinishReason getFinishReason() {
        return finishReason;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @return
     */
    public ChatRole getRole() {
        return role;
    }

    /**
     * @return
     */
    public String getSystemFingerprint() {
        return systemFingerprint;
    }
}
