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
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private void multiSlotTestInner(PersonalizerClient client) {
        rankMultiSlotNullParameters(client);
        rankMultiSlotNoOptions(client);
        rankMultiSlot(client);
        reward(client);
        rewardForOneSlot(client);
        activate(client);
    }

    private void rankMultiSlotNullParameters(PersonalizerClient client) {
        PersonalizerRankMultiSlotOptions request
            = new PersonalizerRankMultiSlotOptions().setActions(getActions()).setSlots(getSlots());
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
        assertEquals(getSlot2().getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void rankMultiSlot(PersonalizerClient client) {
        String eventId = "sdkTestEventId";
        PersonalizerRankMultiSlotOptions request = new PersonalizerRankMultiSlotOptions().setActions(getActions())
            .setSlots(getSlots())
            .setContextFeatures(getContextFeatures())
            .setEventId(eventId);

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
        List<BinaryData> newsFeatures
            = Collections.singletonList(BinaryData.fromObject(new FeatureMetadata().setFeatureType("News")));

        List<BinaryData> sportsFeatures
            = Collections.singletonList(BinaryData.fromObject(new FeatureMetadata().setFeatureType("Sports")));

        List<BinaryData> entertainmentFeatures
            = Collections.singletonList(BinaryData.fromObject(new FeatureMetadata().setFeatureType("Entertainment")));

        actions.add(new PersonalizerRankableAction().setId("NewsArticle").setFeatures(newsFeatures));
        actions.add(new PersonalizerRankableAction().setId("SportsArticle").setFeatures(sportsFeatures));
        actions.add(new PersonalizerRankableAction().setId("EntertainmentArticle").setFeatures(entertainmentFeatures));
        return actions;
    }

    private static PersonalizerSlotOptions getSlot1() {
        List<BinaryData> positionFeatures = Collections.singletonList(
            BinaryData.fromObject(new SlotPositionFeatures().setSize("Large").setPosition("Top Middle")));

        List<String> excludedActions = Arrays.asList("SportsArticle", "EntertainmentArticle");

        return new PersonalizerSlotOptions().setId("Main Article")
            .setBaselineAction("NewsArticle")
            .setFeatures(positionFeatures)
            .setExcludedActions(excludedActions);
    }

    private static PersonalizerSlotOptions getSlot2() {
        List<BinaryData> positionFeatures = Collections.singletonList(
            BinaryData.fromObject(new SlotPositionFeatures().setSize("Small").setPosition("Bottom Right")));

        List<String> excludedActions = Collections.singletonList("EntertainmentArticle");

        return new PersonalizerSlotOptions().setId("Side Bar")
            .setBaselineAction("SportsArticle")
            .setFeatures(positionFeatures)
            .setExcludedActions(excludedActions);
    }

    private static List<PersonalizerSlotOptions> getSlots() {
        return Arrays.asList(getSlot1(), getSlot2());
    }

    private static List<BinaryData> getContextFeatures() {
        return Arrays.asList(
            BinaryData.fromObject(new UserProfile().setProfileType("AnonymousUser").setLatLong("47.6,-122.1")),
            BinaryData.fromObject(new Environment().setDayOfMonth("28").setMonthOfYear("8").setWeather("Sunny")),
            BinaryData.fromObject(new Device().setMobile(true).setWindows(true)),
            BinaryData.fromObject(new RecentActivity().setItemsInCart(3)));
    }
}
