package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.models.RankRequest;
import com.azure.ai.personalizer.implementation.models.RankResponse;
import com.azure.ai.personalizer.implementation.models.RankableAction;
import com.azure.ai.personalizer.implementation.models.RewardRequest;
import com.azure.core.credential.AzureKeyCredential;
import org.junit.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonalizerClientTest {

    private static PersonalizerClient client;

    @BeforeClass
    public static void initialize() {
        client = new PersonalizerClient("https://<resource>.cognitiveservices.azure.com/", new AzureKeyCredential("<apikey>"));
    }

    @Test
    public void testRankThenReward() {
        String eventId = UUID.randomUUID().toString();
        RankResponse response = client.rank(createRankRequest(eventId));
        assertEquals(eventId, response.getEventId(),"Event Ids must match");
        RewardRequest rewardRequest = new RewardRequest().setValue(0.5f);
        client.reward(eventId, rewardRequest);
    }

    @Test
    public void testRankThenActivateAndReward() {
        String eventId = "123456789";
        RankResponse response = client.rank(createRankRequest(eventId));
        assertEquals(eventId, response.getEventId(),"Event Ids must match");
        client.activate(eventId);
        RewardRequest rewardRequest = new RewardRequest().setValue(0.5f);
        client.reward(eventId, rewardRequest);
    }

    public RankRequest createRankRequest(String eventId) {
        List<Object> contextFeatures = new ArrayList<>();
        contextFeatures.add(new Object() { Object Features = new Object() { String day = "tuesday"; String time = "night"; String weather = "rainy"; };});
        contextFeatures.add(new Object() { Object Features = new Object() { String userId = "1234"; boolean payingUser = true; String favoriteGenre = "documentary"; double hoursOnSite = 0.12; String lastwatchedType = "movie"; };});

        List<Object> person1features = new ArrayList<>();
        person1features.add(new Object() {
            String videoType = "documentary";
            Integer videoLength = 35;
            String director = "CarlSagan";
        });

        person1features.add(new Object(){
            String mostWatchedByAge = "30-35";
        });

        List<Object> person2features = new ArrayList<>();
        person2features.add(new Object() {
            String videoType = "documentary";
            Integer videoLength = 35;
            String director = "CarlSagan";
        });

        person2features.add(new Object(){
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
