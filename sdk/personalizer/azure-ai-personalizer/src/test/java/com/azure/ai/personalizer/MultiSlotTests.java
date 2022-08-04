// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.*;
import com.azure.core.http.HttpClient;
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
        PersonalizerClient client = getPersonalizerClient(httpClient, serviceVersion);
        multiSlotTestInner(client);
    }

    private PersonalizerClient getPersonalizerClient(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        return getPersonalizerClientBuilder(httpClient, serviceVersion, false)
            .buildClient();
    }

    private void multiSlotTestInner(PersonalizerClient client)
    {
        rankMultiSlotNullParameters(client);
        rankMultiSlotNoOptions(client);
        rankMultiSlot(client);
        reward(client);
        rewardForOneSlot(client);
        activate(client);
    }

    private void rankMultiSlotNullParameters(PersonalizerClient client) {
        MultiSlotRankRequest request = new MultiSlotRankRequest().setActions(getActions()).setSlots(getSlots());
        // Action
        MultiSlotRankResponse response = client.rankMultiSlot(request);
        // Assert
        assertEquals(getSlots().size(), response.getSlots().size());
        // Assertions for first slot
        SlotResponse responseSlot1 = response.getSlots().get(0);
        assertEquals(getSlot1().getId(), responseSlot1.getId());
        assertEquals("NewsArticle", responseSlot1.getRewardActionId());
        // Assertions for second slot
        SlotResponse responseSlot2 = response.getSlots().get(1);
        assertEquals(getSlot2().getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void rankMultiSlotNoOptions(PersonalizerClient client)
    {
        MultiSlotRankRequest request = new MultiSlotRankRequest().setActions(getActions()).setSlots(getSlots()).setContextFeatures(getContextFeatures());
        // Action
        MultiSlotRankResponse response = client.rankMultiSlot(getActions(), getSlots(), getContextFeatures());
        // Assert
        assertEquals(getSlots().size(), response.getSlots().size());
        // Assertions for first slot
        SlotResponse responseSlot1 = response.getSlots().get(0);
        assertEquals(getSlot1().getId(), responseSlot1.getId());
        assertEquals("NewsArticle", responseSlot1.getRewardActionId());
        // Assertions for second slot
        SlotResponse responseSlot2 = response.getSlots().get(1);
        assertEquals(responseSlot2.getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void rankMultiSlot(PersonalizerClient client)
    {
        String eventId = "sdkTestEventId";
        MultiSlotRankRequest request = new MultiSlotRankRequest().setActions(getActions()).setSlots(getSlots()).setContextFeatures(getContextFeatures()).setEventId(eventId);
        // Action
        MultiSlotRankResponse response = client.rankMultiSlot(request);
        // Assert
        assertEquals(getSlots().size(), response.getSlots().size());
        // Assertions for first slot
        SlotResponse responseSlot1 = response.getSlots().get(0);
        assertEquals(getSlot1().getId(), responseSlot1.getId());
        assertEquals("NewsArticle", responseSlot1.getRewardActionId());
        // Assertions for second slot
        SlotResponse responseSlot2 = response.getSlots().get(1);
        assertEquals(getSlot2().getId(), responseSlot2.getId());
        assertEquals("SportsArticle", responseSlot2.getRewardActionId());
    }

    private void reward(PersonalizerClient client)
    {
        SlotReward slotReward = new SlotReward().setSlotId("testSlot1").setValue(1);
        client.rewardMultiSlot("123456789", "testSlot1", 1);
    }

    private void rewardForOneSlot(PersonalizerClient client)
    {
        client.rewardMultiSlot("123456789", "testSlot", 1);
    }

    private void activate(PersonalizerClient client)
    {
        client.activateMultiSlot("123456789");
    }

    private static List<RankableAction> getActions() {
        ArrayList<RankableAction> actions = new ArrayList<>();
        ArrayList<Object> newsFeatures = new ArrayList<Object>();
        newsFeatures.add(new Object() {
            Object type = "News";
        });
        ArrayList<Object> sportsFeatures = new ArrayList<Object>();
        sportsFeatures.add(new Object() {
            final Object type = "News";
        });
        ArrayList<Object> entertainmentFeatures = new ArrayList<Object>();
        entertainmentFeatures.add(new Object() {
            Object type = "News";
        });
        actions.add(new RankableAction().setId("NewsArticle").setFeatures(newsFeatures));
        actions.add(new RankableAction().setId("SportsArticle").setFeatures(sportsFeatures));
        actions.add(new RankableAction().setId("EntertainmentArticle").setFeatures(entertainmentFeatures));
        return actions;
    }

    private static SlotRequest getSlot1() {
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
        return new SlotRequest().setId("Main Article").setBaselineAction("NewsArticle").setFeatures(positionFeatures).setExcludedActions(excludedActions);
    }

    private static SlotRequest getSlot2() {
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
        return new SlotRequest().setId("Side Bar").setBaselineAction("SportsArticle").setFeatures(positionFeatures).setExcludedActions(excludedActions);
    }

    private static List<SlotRequest> getSlots() {
        return new ArrayList<SlotRequest>() {
            {
                add(getSlot1());
                add(getSlot2());
            }
        };
    }

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
}
