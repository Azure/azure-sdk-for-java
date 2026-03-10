// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.VoiceLiveServiceVersion;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.core.credential.KeyCredential;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Base class for VoiceLive live tests with shared utilities.
 */
@SuppressWarnings("unused")
public abstract class VoiceLiveTestBase extends TestProxyTestBase {

    // Model constants
    protected static final String MODEL_GPT_4O = "gpt-4o";
    protected static final String MODEL_GPT_4O_REALTIME = "gpt-4o-realtime";
    protected static final String MODEL_GPT_4O_REALTIME_PREVIEW = "gpt-4o-realtime-preview";
    protected static final String MODEL_GPT_4O_REALTIME_PREVIEW_2025_06_03 = "gpt-4o-realtime-preview-2025-06-03";
    protected static final String MODEL_GPT_41 = "gpt-4.1";
    protected static final String MODEL_GPT_5 = "gpt-5";
    protected static final String MODEL_GPT_5_CHAT = "gpt-5-chat";
    protected static final String MODEL_PHI4_MM_REALTIME = "phi4-mm-realtime";
    protected static final String MODEL_PHI4_MINI = "phi4-mini";

    // Default models for non-parameterized tests
    protected static final String TEST_MODEL = MODEL_GPT_4O;
    protected static final String TEST_MODEL_REALTIME = MODEL_GPT_4O_REALTIME_PREVIEW;

    // Timeout constants
    protected static final Duration SESSION_TIMEOUT = Duration.ofSeconds(30);
    protected static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);
    protected static final int EVENT_TIMEOUT_SECONDS = 60;
    protected static final int SETUP_DELAY_MS = 2000;

    // Audio thresholds
    protected static final int MIN_AUDIO_BYTES = 10 * 1024;
    protected static final int MIN_AUDIO_BYTES_LARGE = 40 * 1000;

    // Default silence settings
    protected static final int DEFAULT_SAMPLE_RATE = 24000;
    protected static final double DEFAULT_SILENCE_DURATION = 2.0;

    // API version constants
    protected static final String API_VERSION_GA = "2025-10-01";
    protected static final String API_VERSION_PREVIEW = "2026-01-01-preview";

    protected String getEndpoint() {
        String endpoint = Configuration.getGlobalConfiguration().get("AI_SERVICES_ENDPOINT");
        if (endpoint == null || endpoint.trim().isEmpty()) {
            Assertions.fail("AI_SERVICES_ENDPOINT environment variable must be set and not empty.");
        }
        return endpoint;
    }

    protected String getApiKey() {
        String apiKey = Configuration.getGlobalConfiguration().get("AI_SERVICES_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            Assertions.fail("AI_SERVICES_KEY environment variable must be set and not empty.");
        }
        return apiKey;
    }

    protected VoiceLiveAsyncClient createClient() {
        return new VoiceLiveClientBuilder().endpoint(getEndpoint())
            .credential(new KeyCredential(getApiKey()))
            .buildAsyncClient();
    }

    protected VoiceLiveAsyncClient createClient(String apiVersion) {
        return new VoiceLiveClientBuilder().endpoint(getEndpoint())
            .credential(new KeyCredential(getApiKey()))
            .serviceVersion(parseServiceVersion(apiVersion))
            .buildAsyncClient();
    }

    protected static VoiceLiveServiceVersion parseServiceVersion(String version) {
        for (VoiceLiveServiceVersion sv : VoiceLiveServiceVersion.values()) {
            if (sv.getVersion().equals(version)) {
                return sv;
            }
        }
        throw new IllegalArgumentException("Unknown service version: " + version);
    }

    protected static Stream<Arguments> crossProduct(String[] models, String[] apiVersions) {
        return Arrays.stream(models).flatMap(model -> Arrays.stream(apiVersions).map(v -> Arguments.of(model, v)));
    }

    protected static Stream<Arguments> withApiVersions(Stream<Arguments> base) {
        return withApiVersions(base, API_VERSION_GA, API_VERSION_PREVIEW);
    }

    protected static Stream<Arguments> withApiVersions(Stream<Arguments> base, String... apiVersions) {
        Arguments[] baseArgs = base.toArray(Arguments[]::new);
        return Arrays.stream(apiVersions).flatMap(version -> Arrays.stream(baseArgs).map(args -> {
            Object[] existing = args.get();
            Object[] extended = Arrays.copyOf(existing, existing.length + 1);
            extended[existing.length] = version;
            return Arguments.of(extended);
        }));
    }

    protected byte[] loadAudioFile(String filename) throws IOException {
        Path testResourcesPath = Paths.get("test-resources", filename);
        if (Files.exists(testResourcesPath)) {
            return Files.readAllBytes(testResourcesPath);
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (is != null) {
                return readAllBytes(is);
            }
        }

        Path srcTestResourcesPath = Paths.get("src", "test", "resources", filename);
        if (Files.exists(srcTestResourcesPath)) {
            return Files.readAllBytes(srcTestResourcesPath);
        }

        throw new IOException("Could not find audio file: " + filename);
    }

    protected byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[8192];
        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    protected byte[] getTrailingSilenceBytes(int sampleRate, double durationSeconds) {
        int numSamples = (int) (sampleRate * durationSeconds);
        return new byte[numSamples * 2];
    }

    protected byte[] getTrailingSilenceBytes() {
        return getTrailingSilenceBytes(DEFAULT_SAMPLE_RATE, DEFAULT_SILENCE_DURATION);
    }

    protected void handleError(SessionUpdate event) {
        if (event instanceof SessionUpdateError) {
            SessionUpdateError errorEvent = (SessionUpdateError) event;
            System.err.println(
                "Error in session: " + errorEvent.getError().getCode() + " - " + errorEvent.getError().getMessage());
        }
    }

    protected AudioInputTranscriptionOptions getSpeechRecognitionSetting(String model) {
        AudioInputTranscriptionOptionsModel transcriptionModel
            = model.startsWith("gpt-4o-realtime") || model.startsWith("gpt-4o-mini-realtime")
                ? AudioInputTranscriptionOptionsModel.WHISPER_1
                : AudioInputTranscriptionOptionsModel.AZURE_SPEECH;
        return new AudioInputTranscriptionOptions(transcriptionModel).setLanguage("en-US");
    }

    protected OutputAudioFormat parseOutputAudioFormat(String format) {
        return OutputAudioFormat.fromString(format);
    }

    protected InputAudioFormat parseInputAudioFormat(String format) {
        return "g711_ulaw".equals(format) ? InputAudioFormat.G711_ULAW : InputAudioFormat.G711_ALAW;
    }

    protected String getAudioFileForSamplingRate(int samplingRate) {
        switch (samplingRate) {
            case 16000:
                return "largest_lake.16kHz.wav";

            case 44100:
                return "largest_lake.44kHz.wav";

            case 8000:
            default:
                return "largest_lake.8kHz.wav";
        }
    }

    protected void closeSession(VoiceLiveSessionAsyncClient session) {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                System.err.println("Error closing session: " + e.getMessage());
            }
        }
    }

    protected void waitForSetup() throws InterruptedException {
        Thread.sleep(SETUP_DELAY_MS);
    }

    protected BinaryData createFunctionParameters(String... requiredFields) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        for (String field : requiredFields) {
            Map<String, Object> fieldDef = new HashMap<>();
            fieldDef.put("type", "string");
            fieldDef.put("description", "The " + field + " parameter.");
            properties.put(field, fieldDef);
        }
        params.put("properties", properties);
        params.put("required", Arrays.asList(requiredFields));

        return BinaryData.fromObject(params);
    }
}
