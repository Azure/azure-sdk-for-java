// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Detail;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeResponseInputContentSerializationTests {
    @Test
    public void heterogeneousUserContentRoundTrips() throws IOException {
        List<RealtimeResponseInputContent> content
            = Arrays.asList(RealtimeResponseInputContent.ofText(new RealtimeResponseInputText("text")),
                RealtimeResponseInputContent.ofAudio(new RealtimeResponseInputAudio("YXVkaW8=").setTranscript("audio")),
                RealtimeResponseInputContent
                    .ofImage(new RealtimeResponseInputImage("data:image/png;base64,aW1hZ2U=").setDetail(Detail.HIGH)));
        String json = UnionTypeSerializationTestUtils.serialize(new RealtimeResponseInputMessageUser(content));
        RealtimeResponseInputMessageUser result
            = UnionTypeSerializationTestUtils.deserialize(json, RealtimeResponseInputMessageUser::fromJson);
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
        assertEquals(3, result.getContent().size());
        assertEquals("text", result.getContent().get(0).getText().getText());
        assertEquals("YXVkaW8=", result.getContent().get(1).getAudio().getAudio());
        assertEquals("audio", result.getContent().get(1).getAudio().getTranscript());
        assertEquals("data:image/png;base64,aW1hZ2U=", result.getContent().get(2).getImage().getImageUrl());
        assertEquals(Detail.HIGH, result.getContent().get(2).getImage().getDetail());
        assertTrue(json.contains("\"type\":\"input_text\""));
        assertTrue(json.contains("\"type\":\"input_audio\""));
        assertTrue(json.contains("\"type\":\"input_image\""));
    }

    @Test
    public void factoriesExposeOnlyTheirVariant() {
        RealtimeResponseInputText text = new RealtimeResponseInputText("text");
        RealtimeResponseInputContent textContent = RealtimeResponseInputContent.ofText(text);
        assertSame(text, textContent.getText());
        assertNull(textContent.getAudio());
        assertNull(textContent.getImage());

        RealtimeResponseInputAudio audio = new RealtimeResponseInputAudio("YQ==");
        RealtimeResponseInputContent audioContent = RealtimeResponseInputContent.ofAudio(audio);
        assertSame(audio, audioContent.getAudio());
        assertNull(audioContent.getText());
        assertNull(audioContent.getImage());

        RealtimeResponseInputImage image = new RealtimeResponseInputImage("data:image/png;base64,YQ==");
        RealtimeResponseInputContent imageContent = RealtimeResponseInputContent.ofImage(image);
        assertSame(image, imageContent.getImage());
        assertNull(imageContent.getText());
        assertNull(imageContent.getAudio());
    }

    @Test
    public void readsDiscriminatorAfterOtherFields() throws IOException {
        RealtimeResponseInputContent content = UnionTypeSerializationTestUtils.deserialize(
            "{\"ignored\":{\"type\":\"input_audio\"},\"text\":\"text\",\"type\":\"input_text\"}",
            RealtimeResponseInputContent::fromJson);
        assertEquals("text", content.getText().getText());
    }

    @Test
    public void imageDetailValuesRoundTrip() throws IOException {
        for (Detail detail : Arrays.asList(Detail.LOW, Detail.HIGH, Detail.AUTO)) {
            RealtimeResponseInputContent content = RealtimeResponseInputContent
                .ofImage(new RealtimeResponseInputImage("data:image/png;base64,YQ==").setDetail(detail));
            String json = UnionTypeSerializationTestUtils.serialize(content);
            assertTrue(json.contains("\"detail\":\"" + detail.asString() + "\""));
            RealtimeResponseInputContent result
                = UnionTypeSerializationTestUtils.deserialize(json, RealtimeResponseInputContent::fromJson);
            assertEquals(detail, result.getImage().getDetail());
        }
    }

    @Test
    public void jsonNullReturnsNull() throws IOException {
        assertNull(UnionTypeSerializationTestUtils.deserialize("null", RealtimeResponseInputContent::fromJson));
    }

    @Test
    public void invalidVariantsFailExplicitly() {
        for (String json : Arrays.asList("{}", "{\"type\":\"unsupported\"}")) {
            assertThrows(IllegalStateException.class,
                () -> UnionTypeSerializationTestUtils.deserialize(json, RealtimeResponseInputContent::fromJson));
        }
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputContent.ofText(null));
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputContent.ofAudio(null));
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputContent.ofImage(null));
    }
}
