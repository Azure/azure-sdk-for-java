// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.RankRequest;
import com.azure.ai.personalizer.models.RankResponse;
import com.azure.ai.personalizer.models.RankableAction;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonalizerClientTest extends PersonalizerTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public void testRankThenReward(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerClient client = getPersonalizerClient(httpClient, serviceVersion);
        String eventId = UUID.randomUUID().toString();
        RankResponse response = client.rank(createRankRequest(eventId));
        assertEquals(eventId, response.getEventId(), "Event Ids must match");
        client.reward(eventId, 0.5f);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public void testRankThenActivateAndReward(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerClient client = getPersonalizerClient(httpClient, serviceVersion);
        String eventId = UUID.randomUUID().toString();
        RankResponse response = client.rank(createRankRequest(eventId));
        assertEquals(eventId, response.getEventId(), "Event Ids must match");
        client.activate(eventId);
        client.reward(eventId, 0.5f);
    }

    private PersonalizerClient getPersonalizerClient(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        return getPersonalizerClientBuilder(httpClient, serviceVersion, true)
            .buildClient();
    }

    public RankRequest createRankRequest(String eventId) {
        List<Object> contextFeatures = new ArrayList<>();
        contextFeatures.add(new Object() { Object features = new Object() { String day = "tuesday"; String time = "night"; String weather = "rainy"; }; });
        contextFeatures.add(new Object() { Object features = new Object() { String userId = "1234"; boolean payingUser = true; String favoriteGenre = "documentary"; double hoursOnSite = 0.12; String lastwatchedType = "movie"; }; });

        List<Object> person1features = new ArrayList<>();
        person1features.add(new Object() {
            String videoType = "documentary";
            Integer videoLength = 35;
            String director = "CarlSagan";
        });

        person1features.add(new Object() {
            String mostWatchedByAge = "30-35";
        });

        List<Object> person2features = new ArrayList<>();
        person2features.add(new Object() {
            String videoType = "documentary";
            Integer videoLength = 35;
            String director = "CarlSagan";
        });

        person2features.add(new Object() {
            String mostWatchedByAge = "40-45";
        });

        List<RankableAction> actions = new ArrayList<>();
        actions.add(new RankableAction().setId("Person1").setFeatures(person1features));
        actions.add(new RankableAction().setId("Person2").setFeatures(person2features));
        List<String> excludeActions = new ArrayList<>();
        excludeActions.add("Person1");

        return new RankRequest().setActions(actions).setContextFeatures(contextFeatures).setExcludedActions(excludeActions).setEventId(eventId);
    }
}
