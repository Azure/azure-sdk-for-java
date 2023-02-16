// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerRankMultiSlotResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.models.PersonalizerSlotOptions;
import com.azure.ai.personalizer.models.PersonalizerSlotResult;
import com.azure.ai.personalizer.models.PersonalizerSlotReward;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiSlotTests extends PersonalizerTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public void multiSlotTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerClient client = getClient(httpClient, serviceVersion, false);
        multiSlotTestInner(client);
    }

    private PersonalizerClient getPersonalizerClient(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        return getPersonalizerClientBuilder(httpClient, serviceVersion, false)
            .buildClient();
    }

    private void multiSlotTestInner(PersonalizerClient client) {
        rankMultiSlotNullParameters(client);
        rankMultiSlotNoOptions(client);
        rankMultiSlot(client);
        reward(client);
        rewardForOneSlot(client);
        activate(client);
    }

    private void rankMultiSlotNullParameters(PersonalizerClient client) {
        PersonalizerRankMultiSlotOptions request = new PersonalizerRankMultiSlotOptions().setActions(getActions()).setSlots(getSlots());
        // Action
        PersonalizerRankMultiSlotResult response = client.rankMultiSlot(request);
        // Assert
        assertEquals(getSlots().size(), response.getSlots().size());
        // Assertions for first slot
        PersonalizerSlotResult responseSlot1 = response.getSlots().get(0);
        assertEquals(getSlot1().getId(), responseSlot1.getId());
        assertEquals("NewsArticle", responseSlot1.getRewardActionId());
        // Assertions for second slot
        PersonalizerSlotResult responseSlot2 = response.getSlots().get(1);
        assertEquals(getSlot2().getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void rankMultiSlotNoOptions(PersonalizerClient client) {
        PersonalizerRankMultiSlotOptions request = new PersonalizerRankMultiSlotOptions().setActions(getActions()).setSlots(getSlots()).setContextFeatures(getContextFeatures());
        // Action
        PersonalizerRankMultiSlotResult response = client.rankMultiSlot(getActions(), getSlots(), getContextFeatures());
        // Assert
        assertEquals(getSlots().size(), response.getSlots().size());
        // Assertions for first slot
        PersonalizerSlotResult responseSlot1 = response.getSlots().get(0);
        assertEquals(getSlot1().getId(), responseSlot1.getId());
        assertEquals("NewsArticle", responseSlot1.getRewardActionId());
        // Assertions for second slot
        PersonalizerSlotResult responseSlot2 = response.getSlots().get(1);
        assertEquals(responseSlot2.getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void rankMultiSlot(PersonalizerClient client) {
        String eventId = "sdkTestEventId";
        PersonalizerRankMultiSlotOptions request = new PersonalizerRankMultiSlotOptions().setActions(getActions()).setSlots(getSlots()).setContextFeatures(getContextFeatures()).setEventId(eventId);
        // Action
        PersonalizerRankMultiSlotResult response = client.rankMultiSlot(request);
        // Assert
        assertEquals(getSlots().size(), response.getSlots().size());
        // Assertions for first slot
        PersonalizerSlotResult responseSlot1 = response.getSlots().get(0);
        assertEquals(getSlot1().getId(), responseSlot1.getId());
        assertEquals("NewsArticle", responseSlot1.getRewardActionId());
        // Assertions for second slot
        PersonalizerSlotResult responseSlot2 = response.getSlots().get(1);
        assertEquals(getSlot2().getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void reward(PersonalizerClient client) {
        PersonalizerSlotReward slotReward = new PersonalizerSlotReward().setSlotId("testSlot1").setValue(1);
        client.rewardMultiSlot("123456789", "testSlot1", 1);
    }

    private void rewardForOneSlot(PersonalizerClient client) {
        client.rewardMultiSlot("123456789", "testSlot", 1);
    }

    private void activate(PersonalizerClient client) {
        client.activateMultiSlot("123456789");
    }

    private static List<PersonalizerRankableAction> getActions() {
        ArrayList<PersonalizerRankableAction> actions = new ArrayList<>();
        List<BinaryData> newsFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new TestFeatureMetadata().setFeatureType("News")));
            }
        };
        List<BinaryData> sportsFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new TestFeatureMetadata().setFeatureType("Sports")));
            }
        };
        List<BinaryData> entertainmentFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new TestFeatureMetadata().setFeatureType("Entertainment")));
            }
        };

        actions.add(new PersonalizerRankableAction().setId("NewsArticle").setFeatures(newsFeatures));
        actions.add(new PersonalizerRankableAction().setId("SportsArticle").setFeatures(sportsFeatures));
        actions.add(new PersonalizerRankableAction().setId("EntertainmentArticle").setFeatures(entertainmentFeatures));
        return actions;
    }

    private static PersonalizerSlotOptions getSlot1() {
        ArrayList<BinaryData> positionFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new TestSlotPositionFeatures().setSize("Large").setPosition("Top Middle")));
            }
        };

        ArrayList<String> excludedActions = new ArrayList<String>() {
            {
                add("SportsArticle");
                add("EntertainmentArticle");
            }
        };
        return new PersonalizerSlotOptions().setId("Main Article").setBaselineAction("NewsArticle").setFeatures(positionFeatures).setExcludedActions(excludedActions);
    }

    private static PersonalizerSlotOptions getSlot2() {
        ArrayList<BinaryData> positionFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new TestSlotPositionFeatures().setSize("Small").setPosition("Bottom Right")));
            }
        };

        ArrayList<String> excludedActions = new ArrayList<String>() {
            {
                add("EntertainmentArticle");
            }
        };
        return new PersonalizerSlotOptions().setId("Side Bar").setBaselineAction("SportsArticle").setFeatures(positionFeatures).setExcludedActions(excludedActions);
    }

    private static List<PersonalizerSlotOptions> getSlots() {
        return new ArrayList<PersonalizerSlotOptions>() {
            {
                add(getSlot1());
                add(getSlot2());
            }
        };
    }

    private static List<BinaryData> getContextFeatures() {
        return new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new TestUserProfile().setProfileType("AnonymousUser").setLatLong("47.6,-122.1")));
                add(BinaryData.fromObject(new TestEnvironment().setDayOfMonth("28").setMonthOfYear("8").setWeather("Sunny")));
                add(BinaryData.fromObject(new TestDevice().setMobile(true).setWindows(true)));
                add(BinaryData.fromObject(new TestRecentActivity().setItemsInCart(3)));
            }
        };
    }
}

class TestFeatureMetadata {
    @JsonGetter
    public String getFeatureType() {
        return featureType;
    }

    @JsonSetter
    public TestFeatureMetadata setFeatureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    @JsonProperty
    String featureType;

}

class TestSlotPositionFeatures {
    @JsonGetter
    public String getSize() {
        return size;
    }

    @JsonSetter
    public TestSlotPositionFeatures setSize(String size) {
        this.size = size;
        return this;
    }

    @JsonGetter
    public String getPosition() {
        return position;
    }

    @JsonSetter
    public TestSlotPositionFeatures setPosition(String position) {
        this.position = position;
        return this;
    }

    @JsonProperty
    String size;
    @JsonProperty
    String position;
}

class TestUserProfile {
    @JsonGetter
    public String getProfileType() {
        return profileType;
    }

    @JsonSetter
    public TestUserProfile setProfileType(String profileType) {
        this.profileType = profileType;
        return this;
    }

    @JsonGetter
    public String getLatLong() {
        return latLong;
    }

    @JsonSetter
    public TestUserProfile setLatLong(String latLong) {
        this.latLong = latLong;
        return this;
    }

    @JsonProperty
    String profileType;
    @JsonProperty
    String latLong;
}

class TestEnvironment {
    @JsonGetter
    public String getDayOfMonth() {
        return dayOfMonth;
    }

    @JsonSetter
    public TestEnvironment setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
        return this;
    }

    @JsonGetter
    public String getMonthOfYear() {
        return monthOfYear;
    }

    @JsonSetter
    public TestEnvironment setMonthOfYear(String monthOfYear) {
        this.monthOfYear = monthOfYear;
        return this;
    }

    @JsonGetter
    public String getWeather() {
        return weather;
    }

    @JsonSetter
    public TestEnvironment setWeather(String weather) {
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

class TestDevice {
    @JsonGetter
    public boolean isMobile() {
        return isMobile;
    }

    @JsonSetter
    public TestDevice setMobile(boolean mobile) {
        isMobile = mobile;
        return this;
    }

    @JsonGetter
    public boolean isWindows() {
        return isWindows;
    }

    @JsonSetter
    public TestDevice setWindows(boolean windows) {
        isWindows = windows;
        return this;
    }

    @JsonProperty
    boolean isMobile;
    @JsonProperty
    boolean isWindows;
}

class TestRecentActivity {
    @JsonGetter
    public Integer getItemsInCart() {
        return itemsInCart;
    }

    @JsonSetter
    public TestRecentActivity setItemsInCart(Integer itemsInCart) {
        this.itemsInCart = itemsInCart;
        return this;
    }

    @JsonProperty
    Integer itemsInCart;
}
