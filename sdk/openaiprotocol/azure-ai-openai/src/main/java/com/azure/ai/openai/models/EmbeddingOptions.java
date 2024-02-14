package com.azure.ai.openai.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The EmbeddingOptions model.
 */
public final class EmbeddingOptions {
    /*
     * An identifier for the caller or end user of the operation. This may be used for tracking
     * or rate-limiting purposes.
     */
    @JsonProperty(value = "user")
    private String user;


    /**
     * Get the user property: An identifier for the caller or end user of the operation. This may be used for tracking
     * or rate-limiting purposes.
     *
     * @return the user value.
     */

    public String getUser() {
        return this.user;
    }

    /**
     * Set the user property: An identifier for the caller or end user of the operation. This may be used for tracking
     * or rate-limiting purposes.
     *
     * @param user the user value to set.
     * @return the EmbeddingsOptions object itself.
     */

    public EmbeddingOptions setUser(String user) {
        this.user = user;
        return this;
    }


    /**
     * Creates an instance of EmbeddingsOptions class.
     */

    public EmbeddingOptions() {
    }
}
