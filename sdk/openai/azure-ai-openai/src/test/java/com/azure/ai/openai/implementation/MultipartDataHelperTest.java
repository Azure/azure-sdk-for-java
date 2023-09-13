package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.EmbeddingsOptions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link MultipartDataHelper}
 */
public class MultipartDataHelperTest {

    @Test
    public void serializeAudioTranslationOptionsAllFields() {}

    @Test
    public void serializeAudioTranscriptionOptionsAllFields() {}

    @Test
    public void serializeAudioTranslationOptionsMinimumFields() {}

    @Test
    public void serializeAudioTranscriptionOptionsMinimumFields() {}

    @Test
    public void serializeUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> {
            MultipartDataHelper helper = new MultipartDataHelper(new TestBoundaryGenerator());
            EmbeddingsOptions embeddingsOptions = new EmbeddingsOptions(new ArrayList<>());
            helper.serializeRequest(embeddingsOptions, "path/to/file");
        });
    }
}
