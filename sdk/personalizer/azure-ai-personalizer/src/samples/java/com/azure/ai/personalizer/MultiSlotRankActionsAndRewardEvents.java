// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.models.PersonalizerSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotResult;
import com.azure.ai.personalizer.testmodels.Device;
import com.azure.ai.personalizer.testmodels.Environment;
import com.azure.ai.personalizer.testmodels.FeatureMetadata;
import com.azure.ai.personalizer.testmodels.RecentActivity;
import com.azure.ai.personalizer.testmodels.SlotPositionFeatures;
import com.azure.ai.personalizer.testmodels.UserProfile;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates the use of a Personalizer client to rank actions for multiple slots and reward the presented action.
 */
public class MultiSlotRankActionsAndRewardEvents {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException Exception thrown when endpoint or key is empty.
     * @throws NullPointerException Exception thrown when endpoint or key is null.
     */
    public static void main(final String[] args) throws IllegalArgumentException, NullPointerException {
        // Instantiate a client that will be used to call the service.
        PersonalizerClient client = new PersonalizerClientBuilder().credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        PersonalizerRankMultiSlotOptions rankOptions = new PersonalizerRankMultiSlotOptions().setActions(getActions())
            .setContextFeatures(getContextFeatures())
            .setSlots(getSlots());

        System.out.println("Sending rank request");
        PersonalizerRankMultiSlotResult result = client.rankMultiSlot(rankOptions);
        String eventId = rankOptions.getEventId();
        System.out.printf("Rank returned response with event id %s and recommended the following:%n", eventId);

        for (PersonalizerSlotResult slot : result.getSlots()) {
            System.out.printf("Action %s for slot %s%n", slot.getRewardActionId(), slot.getId());
        }

        // The event response will be determined by how the user interacted with the action that was presented to them.
        // Let us say that they like the action presented to them for the "Main Article" (first) slot. So we associate a reward of 1.
        System.out.println("Sending reward for event for the Main Article slot");
        client.rewardMultiSlot(eventId, "Main Article", 1);
        System.out.println("Completed sending reward for event");
    }

    /**
     * Get the rich features associated with the current context.
     *
     * @return the current context.
     */
    private static List<BinaryData> getContextFeatures() {
        return new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new UserProfile().setProfileType("AnonymousUser").setLatLong("47.6,-122.1")));
                add(BinaryData.fromObject(
                    new Environment().setDayOfMonth("28").setMonthOfYear("8").setWeather("Sunny")));
                add(BinaryData.fromObject(new Device().setMobile(true).setWindows(true)));
                add(BinaryData.fromObject(new RecentActivity().setItemsInCart(3)));
            }
        };
    }

    /**
     * Get the actions that have to be ranked by the rank api.
     *
     * @return The list of actions (videos in this case) to be ranked with metadata associated for each action.
     */
    private static List<PersonalizerRankableAction> getActions() {
        ArrayList<PersonalizerRankableAction> actions = new ArrayList<>();
        List<BinaryData> newsFeatures = Arrays.asList(
            BinaryData.fromObject(new FeatureMetadata().setFeatureType("News")));
        List<BinaryData> sportsFeatures = Arrays.asList(
            BinaryData.fromObject(new FeatureMetadata().setFeatureType("Sports")));
        List<BinaryData> entertainmentFeatures = Arrays.asList(
            BinaryData.fromObject(new FeatureMetadata().setFeatureType("Entertainment")));

        actions.add(new PersonalizerRankableAction().setId("NewsArticle").setFeatures(newsFeatures));
        actions.add(new PersonalizerRankableAction().setId("SportsArticle").setFeatures(sportsFeatures));
        actions.add(new PersonalizerRankableAction().setId("EntertainmentArticle").setFeatures(entertainmentFeatures));
        return actions;
    }

    /**
     * Get the slots that we need the recommended actions for.
     *
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
        List<BinaryData> positionFeatures = Arrays.asList(
            BinaryData.fromObject(new SlotPositionFeatures().setSize("Large").setPosition("Top Middle")));
        List<String> excludedActions = Arrays.asList("SportsArticle", "EntertainmentArticle");

        return new PersonalizerSlotOptions().setId("Main Article")
            .setBaselineAction("NewsArticle")
            .setFeatures(positionFeatures)
            .setExcludedActions(excludedActions);
    }

    private static PersonalizerSlotOptions getSlot2() {
        List<BinaryData> positionFeatures = Arrays.asList(
            BinaryData.fromObject(new SlotPositionFeatures().setSize("Small").setPosition("Bottom Right")));
        List<String> excludedActions = Arrays.asList("EntertainmentArticle");

        return new PersonalizerSlotOptions().setId("Side Bar")
            .setBaselineAction("SportsArticle")
            .setFeatures(positionFeatures)
            .setExcludedActions(excludedActions);
    }
}
