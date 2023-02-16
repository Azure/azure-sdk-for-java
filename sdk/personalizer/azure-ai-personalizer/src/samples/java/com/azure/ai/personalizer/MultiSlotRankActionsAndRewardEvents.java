// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.models.PersonalizerSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

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
     * @throws IllegalArgumentException Exception thrown when endpoint or key is empty.
     * @throws NullPointerException Exception thrown when endpoint or key is null.
     */
    public static void main(final String[] args) throws IllegalArgumentException, NullPointerException {
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
     *
     * @return the current context.
     */
    private static List<BinaryData> getContextFeatures() {
        return new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new UserProfile().setProfileType("AnonymousUser").setLatLong("47.6,-122.1")));
                add(BinaryData.fromObject(new Environment().setDayOfMonth("28").setMonthOfYear("8").setWeather("Sunny")));
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
        List<BinaryData> newsFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new FeatureMetadata().setFeatureType("News")));
            }
        };
        List<BinaryData> sportsFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new FeatureMetadata().setFeatureType("Sports")));
            }
        };
        List<BinaryData> entertainmentFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new FeatureMetadata().setFeatureType("Entertainment")));
            }
        };

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
        ArrayList<BinaryData> positionFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new SlotPositionFeatures().setSize("Large").setPosition("Top Middle")));
            }
        };

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
        ArrayList<BinaryData> positionFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new SlotPositionFeatures().setSize("Small").setPosition("Bottom Right")));
            }
        };
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
class FeatureMetadata {
    @JsonGetter
    public String getFeatureType() {
        return featureType;
    }

    @JsonSetter
    public FeatureMetadata setFeatureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    @JsonProperty
    String featureType;

}

class SlotPositionFeatures {
    @JsonGetter
    public String getSize() {
        return size;
    }

    @JsonSetter
    public SlotPositionFeatures setSize(String size) {
        this.size = size;
        return this;
    }

    @JsonGetter
    public String getPosition() {
        return position;
    }

    @JsonSetter
    public SlotPositionFeatures setPosition(String position) {
        this.position = position;
        return this;
    }

    @JsonProperty
    String size;
    @JsonProperty
    String position;
}

class UserProfile {
    @JsonGetter
    public String getProfileType() {
        return profileType;
    }

    @JsonSetter
    public UserProfile setProfileType(String profileType) {
        this.profileType = profileType;
        return this;
    }

    @JsonGetter
    public String getLatLong() {
        return latLong;
    }

    @JsonSetter
    public UserProfile setLatLong(String latLong) {
        this.latLong = latLong;
        return this;
    }

    @JsonProperty
    String profileType;
    @JsonProperty
    String latLong;
}

class Environment {
    @JsonGetter
    public String getDayOfMonth() {
        return dayOfMonth;
    }

    @JsonSetter
    public Environment setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    @JsonGetter
    public String getMonthOfYear() {
        return monthOfYear;
    }

    @JsonSetter
    public Environment setMonthOfYear(String monthOfYear) {
        this.monthOfYear = monthOfYear;
        return this;
    }

    @JsonGetter
    public String getWeather() {
        return weather;
    }

    @JsonSetter
    public Environment setWeather(String weather) {
        this.weather = weather;
        return this;
    }

    @JsonProperty
    String dayOfMonth;
    @JsonProperty
    String monthOfYear;
    @JsonProperty
    String weather;
}

class Device {
    @JsonGetter
    public boolean isMobile() {
        return isMobile;
    }

    @JsonSetter
    public Device setMobile(boolean mobile) {
        isMobile = mobile;
        return this;
    }

    @JsonGetter
    public boolean isWindows() {
        return isWindows;
    }

    @JsonSetter
    public Device setWindows(boolean windows) {
        isWindows = windows;
        return this;
    }

    @JsonProperty
    boolean isMobile;
    @JsonProperty
    boolean isWindows;
}

class RecentActivity {
    @JsonGetter
    public Integer getItemsInCart() {
        return itemsInCart;
    }

    @JsonSetter
    public RecentActivity setItemsInCart(Integer itemsInCart) {
        this.itemsInCart = itemsInCart;
        return this;
    }

    @JsonProperty
    Integer itemsInCart;
}
