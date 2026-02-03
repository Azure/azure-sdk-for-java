// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

import com.azure.identity.AuthenticationUtil;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.credential.BearerTokenCredential;
import com.openai.models.audio.AudioModel;
import com.openai.models.audio.transcriptions.Transcription;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AudioTranscriptionsSample {
    private AudioTranscriptionsSample() {}

    public static void main(String[] args) throws Exception {
        // Configures using one of:
        // - The `OPENAI_API_KEY` environment variable
        // - The `AZURE_OPENAI_ENDPOINT` and `AZURE_OPENAI_KEY` environment variables
        OpenAIOkHttpClient.Builder clientBuilder = OpenAIOkHttpClient.builder();

        /* Azure-specific code starts here */
        // You can either set 'endpoint' or 'apiKey' directly in the builder.
        // or set same two env vars and use fromEnv() method instead
        clientBuilder
            .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
            .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
                new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")));
        /* Azure-specific code ends here */

        // All code from this line down is general-purpose OpenAI code
        OpenAIClient client = clientBuilder.build();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Path path = Paths.get(classloader.getResource("batman.wav").toURI());

        TranscriptionCreateParams createParams = TranscriptionCreateParams.builder()
                .file(path)
                .model(AudioModel.of("whisper"))
                .build();

        Transcription transcription =
                client.audio().transcriptions().create(createParams).asTranscription();
        System.out.println(transcription.text());
    }
}
