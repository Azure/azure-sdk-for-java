package com.azure.ai.openai.models;

import java.util.List;

/**
 *
 */
public final class EmbeddingCollection {
    private final List<Embedding> embeddings;

    /**
     * @param embeddings
     */
    public EmbeddingCollection(List<Embedding> embeddings) {
        this.embeddings = embeddings;
    }

    /**
     * @return
     */
    public List<Embedding> getEmbeddings() {
        return embeddings;
    }
}
