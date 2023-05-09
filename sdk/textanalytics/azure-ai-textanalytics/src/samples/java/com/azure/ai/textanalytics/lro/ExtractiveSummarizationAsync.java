// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsAsyncClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.ExtractSummaryOperationDetail;
import com.azure.ai.textanalytics.models.ExtractSummaryOptions;
import com.azure.ai.textanalytics.models.ExtractSummaryResult;
import com.azure.ai.textanalytics.models.SummarySentence;
import com.azure.ai.textanalytics.models.SummarySentencesOrder;
import com.azure.ai.textanalytics.util.ExtractSummaryResultCollection;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to asynchronously execute an "Extractive Summarization" in a batch of documents.
 */
public class ExtractiveSummarizationAsync {
    /**
     * Main method to invoke this demo about how to analyze an "Extractive Summarization".
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsAsyncClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        List<String> documents = new ArrayList<>();

        documents.add(
            "At Microsoft, we have been on a quest to advance AI beyond existing techniques, by taking a more holistic,"
                + " human-centric approach to learning and understanding. As Chief Technology Officer of Azure AI"
                + " Cognitive Services, I have been working with a team of amazing scientists and engineers to turn "
                + "this quest into a reality. In my role, I enjoy a unique perspective in viewing the relationship"
                + " among three attributes of human cognition: monolingual text (X), audio or visual sensory signals,"
                + " (Y) and multilingual (Z). At the intersection of all three, there’s magic—what we call XYZ-code"
                + " as illustrated in Figure 1—a joint representation to create more powerful AI that can speak, hear,"
                + " see, and understand humans better. We believe XYZ-code will enable us to fulfill our long-term"
                + " vision: cross-domain transfer learning, spanning modalities and languages. The goal is to have"
                + " pretrained models that can jointly learn representations to support a broad range of downstream"
                + " AI tasks, much in the way humans do today. Over the past five years, we have achieved human"
                + " performance on benchmarks in conversational speech recognition, machine translation, "
                + "conversational question answering, machine reading comprehension, and image captioning. These"
                + " five breakthroughs provided us with strong signals toward our more ambitious aspiration to"
                + " produce a leap in AI capabilities, achieving multisensory and multilingual learning that "
                + "is closer in line with how humans learn and understand. I believe the joint XYZ-code is a "
                + "foundational component of this aspiration, if grounded with external knowledge sources in "
                + "the downstream AI tasks.");

        client.beginExtractSummary(documents,
            "en",
            new ExtractSummaryOptions().setMaxSentenceCount(4).setOrderBy(SummarySentencesOrder.RANK))
            .flatMap(result -> {
                ExtractSummaryOperationDetail operationDetail = result.getValue();
                System.out.printf("Operation created time: %s, expiration time: %s.%n",
                    operationDetail.getCreatedAt(), operationDetail.getExpiresAt());
                return result.getFinalResult();
            })
            .flatMap(pagedFlux -> pagedFlux) // this unwrap the Mono<> of Mono<PagedFlux<T>> to return PagedFlux<T>
            .subscribe(
                resultCollection -> processResult(resultCollection),
                ex -> System.out.println("Error listing pages: " + ex.getMessage()),
                () -> System.out.println("Successfully listed all pages"));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void processResult(ExtractSummaryResultCollection resultCollection) {
        System.out.println("Extractive Summarization action results:");
        for (ExtractSummaryResult documentResult : resultCollection) {
            if (!documentResult.isError()) {
                System.out.println("\tExtracted summary sentences:");
                for (SummarySentence summarySentence : documentResult.getSentences()) {
                    System.out.printf(
                        "\t\t Sentence text: %s, length: %d, offset: %d, rank score: %f.%n",
                        summarySentence.getText(), summarySentence.getLength(),
                        summarySentence.getOffset(), summarySentence.getRankScore());
                }
            } else {
                System.out.printf("\tCannot extract summary sentences. Error: %s%n",
                    documentResult.getError().getMessage());
            }
        }
    }
}
