// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.models.PersonalizerSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotResult;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the use of a Personalizer client to rank actions for multiple slots and reward the presented action.
 */
public class MultiSlotRankActionsAndRewardEvents {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        PersonalizerClient client = new PersonalizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        PersonalizerRankMultiSlotOptions rankOptions = new PersonalizerRankMultiSlotOptions()
            .setActions(getActions())
            .setContextFeatures(getContextFeatures())
            .setSlots(getSlots());

        System.out.println("Sending rank request");
        PersonalizerRankMultiSlotResult result = client.rankMultiSlot(rankOptions);
        String eventId = rankOptions.getEventId();
        System.out.println(String.format(
            "Rank returned response with event id %s and recommended the following:",
            eventId));

        for (PersonalizerSlotResult slot : result.getSlots()) {
            System.out.println(String.format(
                "Action ${slotResponse.rewardActionId} for slot %s",
                slot.getRewardActionId(),
                slot.getId()));
        }

        // The event response will be determined by how the user interacted with the action that was presented to them.
        // Let us say that they like the action presented to them for the "Main Article" (first) slot. So we associate a reward of 1.
        System.out.println("Sending reward for event for the Main Article slot");
        client.rewardMultiSlot(eventId, "Main Article", 1);
        System.out.println("Completed sending reward for event");
    }

    /**
     * Get the rich features associated with the current context.
     * @return the current context.
     */
    private static List<Object> getContextFeatures() {
        return new ArrayList<Object>() {
            {
                add(new Object() { Object user = new Object() { String profileType = "AnonymousUser"; String latLong = "47.6,-122.1"; }; });
                add(new Object() { Object environment = new Object() { String dayOfMonth = "28"; String monthOfYear = "8"; String weather = "Sunny"; }; });
                add(new Object() { Object device = new Object() { boolean mobile = true; boolean windows = true; }; });
                add(new Object() { Object recentActivity = new Object() { Integer itemsInCart = 3; }; });
            }
        };
    }

    /**
     * Get the actions that have to be ranked by the rank api.
     * @return The list of actions (videos in this case) to be ranked with metadata associated for each action.
     */
    private static List<PersonalizerRankableAction> getActions() {
        ArrayList<PersonalizerRankableAction> actions = new ArrayList<>();
        ArrayList<Object> newsFeatures = new ArrayList<Object>();
        newsFeatures.add(new Object() {
            Object type = "News";
        });
        ArrayList<Object> sportsFeatures = new ArrayList<Object>();
        sportsFeatures.add(new Object() {
            final Object type = "Sports";
        });
        ArrayList<Object> entertainmentFeatures = new ArrayList<Object>();
        entertainmentFeatures.add(new Object() {
            Object type = "Entertainment";
        });
        actions.add(new PersonalizerRankableAction().setId("NewsArticle").setFeatures(newsFeatures));
        actions.add(new PersonalizerRankableAction().setId("SportsArticle").setFeatures(sportsFeatures));
        actions.add(new PersonalizerRankableAction().setId("EntertainmentArticle").setFeatures(entertainmentFeatures));
        return actions;
    }

    /**
     * Get the slots that we need the recommended actions for.
     * @return
     */
    private static List<PersonalizerSlotOptions> getSlots() {
        return new ArrayList<PersonalizerSlotOptions>() {
            {
                add(getSlot1());
                add(getSlot2());
            }
        };
    }

    private static PersonalizerSlotOptions getSlot1() {
        ArrayList<Object> positionFeatures = new ArrayList<Object>();
        positionFeatures.add(new Object() {
            Object size = "Large";
            Object position = "Top Middle";
        });
        ArrayList<String> excludedActions = new ArrayList<String>() {
            {
                add("SportsArticle");
                add("EntertainmentArticle");
            }
        };
        return new PersonalizerSlotOptions()
            .setId("Main Article")
            .setBaselineAction("NewsArticle")
            .setFeatures(positionFeatures)
            .setExcludedActions(excludedActions);
    }

    private static PersonalizerSlotOptions getSlot2() {
        ArrayList<Object> positionFeatures = new ArrayList<Object>();
        positionFeatures.add(new Object() {
            Object size = "Small";
            Object position = "Bottom Right";
        });
        ArrayList<String> excludedActions = new ArrayList<String>() {
            {
                add("EntertainmentArticle");
            }
        };
        return new PersonalizerSlotOptions()
            .setId("Side Bar")
            .setBaselineAction("SportsArticle")
            .setFeatures(positionFeatures)
            .setExcludedActions(excludedActions);
    }
}
