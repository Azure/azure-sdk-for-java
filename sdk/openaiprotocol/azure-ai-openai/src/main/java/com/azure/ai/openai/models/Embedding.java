package com.azure.ai.openai.models;

import java.util.List;

/**
 *
 */
public final class Embedding {
    private int index;
    private List<Double> embedding;
    private EmbeddingsUsage usage;

    /**
     * @return
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return
     */
    public List<Double> getEmbedding() {
        return embedding;
    }

    /**
     * @return
     */
    public EmbeddingsUsage getUsage() {
        return usage;
    }
}
