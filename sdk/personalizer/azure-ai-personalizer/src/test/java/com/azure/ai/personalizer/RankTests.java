// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankOptions;
import com.azure.ai.personalizer.models.PersonalizerRankResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
import com.azure.ai.personalizer.testmodels.ActionCategory;
import com.azure.ai.personalizer.testmodels.ActionFeatures;
import com.azure.ai.personalizer.testmodels.Context;
import com.azure.ai.personalizer.testmodels.CurrentFeatures;
import com.azure.ai.personalizer.testmodels.UserFeatures;
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

public class RankTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void singleSlotRankTests(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerClient client = getClient(httpClient, serviceVersion, true);
        singleSlotRankTests(client);
    }

    private void singleSlotRankTests(PersonalizerClient client) {
        rankNullParameters(client);
        rankServerFeatures(client);
        rankWithNoExcludedFeatures(client);
    }

    private void rankNullParameters(PersonalizerClient client) {
        List<BinaryData> features = Arrays.asList(BinaryData.fromObject(
                new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")),
            BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));

        List<PersonalizerRankableAction> actions = new ArrayList<>();
        actions.add(new PersonalizerRankableAction().setId("Person").setFeatures(features));
        PersonalizerRankOptions request = new PersonalizerRankOptions().setActions(actions)
            .setContextFeatures(null)
            .setExcludedActions(null);
        // Action
        PersonalizerRankResult response = client.rank(request);
        // Assert
        assertEquals(actions.size(), response.getRanking().size());
        for (int i = 0; i < response.getRanking().size(); i++) {
            assertEquals(actions.get(i).getId(), response.getRanking().get(i).getId());
        }
    }

    private void rankServerFeatures(PersonalizerClient client) {
        List<BinaryData> contextFeatures = Arrays.asList(BinaryData.fromObject(
                new Context().setCurrentFeatures(new CurrentFeatures().setDay("Tuesday").setWeather("rainy"))),
            BinaryData.fromObject(new UserFeatures().setPayingUser(true)
                .setFavoriteGenre("rainy")
                .setHoursOnSite(0.12)
                .setLastWatchedType("movie")));

        List<PersonalizerRankableAction> actions = new ArrayList<>();
        List<BinaryData> features1 = Arrays.asList(BinaryData.fromObject(
                new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")),
            BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));

        actions.add(new PersonalizerRankableAction().setId("Person1").setFeatures(features1));

        List<BinaryData> features2 = Arrays.asList(BinaryData.fromObject(
                new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")),
            BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("40-45")));

        actions.add(new PersonalizerRankableAction().setId("Person2").setFeatures(features2));

        List<String> excludeActions = Collections.singletonList("Person1");

        String eventId = "123456789";
        PersonalizerRankOptions request = new PersonalizerRankOptions().setActions(actions)
            .setContextFeatures(contextFeatures)
            .setExcludedActions(excludeActions)
            .setEventId(eventId);
        // Action
        PersonalizerRankResult response = client.rank(request);
        // Assert
        assertEquals(eventId, response.getEventId());
        assertEquals(actions.size(), response.getRanking().size());
        for (int i = 0; i < response.getRanking().size(); i++) {
            assertEquals(actions.get(i).getId(), response.getRanking().get(i).getId());
        }
    }

    private void rankWithNoExcludedFeatures(PersonalizerClient client) {
        List<BinaryData> contextFeatures = Arrays.asList(BinaryData.fromObject(new UserFeatures().setPayingUser(true)
            .setFavoriteGenre("documentary")
            .setHoursOnSite(0.12)
            .setLastWatchedType("movie")),
            BinaryData.fromObject(new Context().setCurrentFeatures(new CurrentFeatures().setDay("tuesday")
                .setWeather("rainy"))));

        List<BinaryData> features1 = Arrays.asList(BinaryData.fromObject(
                new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")),
            BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));

        List<BinaryData> features2 = Arrays.asList(BinaryData.fromObject(
                new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")),
            BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("40-45")));

        List<PersonalizerRankableAction> actions = new ArrayList<PersonalizerRankableAction>();
        actions.add(new PersonalizerRankableAction().setId("Person1").setFeatures(features1));
        actions.add(new PersonalizerRankableAction().setId("Person2").setFeatures(features2));
        // Action
        PersonalizerRankResult response = client.rank(
            new PersonalizerRankOptions().setActions(actions).setContextFeatures(contextFeatures));
        assertEquals(actions.size(), response.getRanking().size());
    }
}
