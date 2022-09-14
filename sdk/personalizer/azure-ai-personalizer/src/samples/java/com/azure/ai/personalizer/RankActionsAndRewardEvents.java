// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankOptions;
import com.azure.ai.personalizer.models.PersonalizerRankResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the use of a Personalizer client to rank actions for multiple slots and reward the presented action.
 */
public class RankActionsAndRewardEvents {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException Exception thrown when endpoint or key is empty.
     * @throws NullPointerException Exception thrown when endpoint or key is null.
     */
    public static void main(final String[] args) throws IllegalArgumentException, NullPointerException {
        // Instantiate a client that will be used to call the service.
        PersonalizerClient client = new PersonalizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        PersonalizerRankOptions rankOptions = new PersonalizerRankOptions().setActions(getActions()).setContextFeatures(getContextFeatures());
        System.out.println("Sending rank request");
        PersonalizerRankResult result = client.rank(rankOptions);
        System.out.println(
            String.format("Rank returned response with event id: %s and recommended: %s as the best action",
                result.getEventId(),
                result.getRewardActionId()));

        // The event response will be determined by how the user interacted with the action that was presented to them.
        // Let us say that they like the action. So we associate a reward of 1.
        System.out.println("Sending reward for event");
        client.reward(rankOptions.getEventId(), 1);
        System.out.println("Completed sending reward for event");
    }

    /**
     * Get the rich features associated with the current context.
     * @return the current context.
     */
    private static List<BinaryData> getContextFeatures() {
        return new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new Context().setCurrentFeatures(new CurrentFeatures().setDay("Tuesday").setWeather("rainy"))));
                add(BinaryData.fromObject(new UserFeatures().setPayingUser(true).setFavoriteGenre("rainy").setHoursOnSite(0.12).setLastWatchedType("movie")));
            }
        };
    }

    /**
     * Get the actions that have to be ranked by the rank api.
     * @return The list of actions (videos in this case) to be ranked with metadata associated for each action.
     */
    private static List<PersonalizerRankableAction> getActions() {
        List<PersonalizerRankableAction> actions = new ArrayList<>();
        List<BinaryData> features1 =  new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));
            }
        };

        actions.add(new PersonalizerRankableAction().setId("Video1").setFeatures(features1));

        List<BinaryData> features2 = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("40-45")));
            }
        };

        actions.add(new PersonalizerRankableAction().setId("Video2").setFeatures(features2));
        return actions;
    }
}
