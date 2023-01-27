// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.lro;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.AbstractSummaryOperationDetail;
import com.azure.ai.textanalytics.models.AbstractSummaryOptions;
import com.azure.ai.textanalytics.models.AbstractSummaryResult;
import com.azure.ai.textanalytics.models.AbstractiveSummary;
import com.azure.ai.textanalytics.models.SummaryContext;
import com.azure.ai.textanalytics.util.AbstractSummaryPagedIterable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates how to synchronously execute an "Abstractive Summarization" in a batch of documents.
 */
public class AbstractiveSummarization {
    /**
     * Main method to invoke this demo about how to analyze an "Abstractive Summarization".
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        TextAnalyticsClient client = new TextAnalyticsClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

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

        SyncPoller<AbstractSummaryOperationDetail, AbstractSummaryPagedIterable> syncPoller =
            client.beginAbstractSummary(documents,
                "en",
                new AbstractSummaryOptions().setDisplayName("{tasks_display_name}").setMaxSentenceCount(3));

        syncPoller.waitForCompletion();

        syncPoller.getFinalResult().forEach(resultCollection -> {
            for (AbstractSummaryResult documentResult : resultCollection) {
                if (!documentResult.isError()) {
                    System.out.println("\tAbstract summary sentences:");
                    for (AbstractiveSummary summarySentence : documentResult.getSummaries()) {
                        System.out.printf("\t\t Summary text: %s.%n", summarySentence.getText());
                        for (SummaryContext summaryContext : summarySentence.getContexts()) {
                            System.out.printf("\t\t offset: %d, length: %d%n",
                                summaryContext.getOffset(), summaryContext.getLength());
                        }
                    }
                } else {
                    System.out.printf("\tCannot get abstract summary. Error: %s%n",
                        documentResult.getError().getMessage());
                }
            }
        });
    }
}
