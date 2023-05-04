package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class NonAzureOpenAI {

    /**
     * Runs the sample algorithm and demonstrates how to get completions for the provided input prompts.
     * Completions support a wide variety of tasks and generate text that continues from or "completes" provided
     * prompt data.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String openAIKey = Configuration.getGlobalConfiguration().get("OPENAI_SECRET_KEY");
        String openAIEndpoint =  Configuration.getGlobalConfiguration().get("OPENAI_ENDPOINT");

        OpenAIServiceVersion openAIServiceVersion = OpenAIServiceVersion.V1;

        TokenCredential tokenCredential = request ->
            Mono.justOrEmpty(new AccessToken(openAIKey, OffsetDateTime.now().plusDays(180)));

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(openAIEndpoint)
            .serviceVersion(openAIServiceVersion)
            .credential(tokenCredential)
            .isAzure(false)
            .buildClient();

        String prompt = "Tell me 3 jokes about trains";

        Completions completions = client.getCompletions("text-davinci-003", prompt);
        for (Choice choice : completions.getChoices()) {
            System.out.printf("%s.%n", choice.getText());
        }
    }
}
