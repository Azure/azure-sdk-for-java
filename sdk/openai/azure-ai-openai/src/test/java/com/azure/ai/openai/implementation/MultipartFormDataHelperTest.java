package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.AudioTranscription;
import com.azure.core.http.rest.RequestOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonReader;
import com.azure.json.JsonProviders;

public class MultipartFormDataHelperTest {
    private MultipartFormDataHelper helper;

    @BeforeEach
    public void setup() {
        RequestOptions requestOptions = new RequestOptions();
        helper = new MultipartFormDataHelper(requestOptions);
    }

    @Test
    public void testDeserializeAudioTranscription() throws IOException {
        // Define the file path (adjust if necessary)
        File jsonFile = new File("src/test/resources/audio_transcription_test.json");

        // Ensure the file exists
        assertTrue(jsonFile.exists(), "JSON file does not exist: " + jsonFile.getAbsolutePath());

        // Read the JSON file
        try (FileInputStream fis = new FileInputStream(jsonFile);
            JsonReader jsonReader = JsonProviders.createReader(fis)) {

            // Deserialize the JSON to an AudioTranscription object
            AudioTranscription transcription = AudioTranscription.fromJson(jsonReader);

            // Assertions to verify deserialization
            assertNotNull(transcription, "Deserialized object should not be null");
            assertEquals("Hello, this is a test transcription.", transcription.getText());
            assertEquals("en", transcription.getLanguage());
            assertNotNull(transcription.getSegments());
            assertFalse(transcription.getSegments().isEmpty(), "Segments should not be empty");
            assertNotNull(transcription.getWords());
            assertFalse(transcription.getWords().isEmpty(), "Words should not be empty");

            // Additional checks for expected data
            assertEquals(2, transcription.getSegments().size());
            assertEquals("Hello, this is a", transcription.getSegments().get(0).getText());
            assertEquals("test transcription.", transcription.getSegments().get(1).getText());

            assertEquals(6, transcription.getWords().size());
            assertEquals("Hello", transcription.getWords().get(0).getWord());
            assertEquals("transcription", transcription.getWords().get(5).getWord());
        }
    }

    @Test
    public void testEnd() {
        String fieldName = "textField";
        String value = "Some text value";

        helper.serializeTextField(fieldName, value);
        helper.end();

        BinaryData requestBody = helper.getRequestBody();
        String requestBodyString = new String(requestBody.toBytes(), StandardCharsets.UTF_8);

        // Ensure the multipart form ends correctly
        assertTrue(requestBodyString.endsWith("--" + helper.getBoundary() + "--"));
    }
}
