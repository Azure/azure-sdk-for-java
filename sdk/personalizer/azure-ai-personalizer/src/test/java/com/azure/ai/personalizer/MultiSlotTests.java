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
        actions.add(new PersonalizerRankableAction().setId("NewsArticle").setFeatures(newsFeatures));
        actions.add(new PersonalizerRankableAction().setId("SportsArticle").setFeatures(sportsFeatures));
        actions.add(new PersonalizerRankableAction().setId("EntertainmentArticle").setFeatures(entertainmentFeatures));
        return actions;
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
        return new PersonalizerSlotOptions().setId("Main Article").setBaselineAction("NewsArticle").setFeatures(positionFeatures).setExcludedActions(excludedActions);
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
