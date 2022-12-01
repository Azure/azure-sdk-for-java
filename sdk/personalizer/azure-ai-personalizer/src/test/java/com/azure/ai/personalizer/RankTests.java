// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.PersonalizerRankOptions;
import com.azure.ai.personalizer.models.PersonalizerRankResult;
import com.azure.ai.personalizer.models.PersonalizerRankableAction;
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
        List<BinaryData> features = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));
            }
        };
        List<PersonalizerRankableAction> actions = new ArrayList<>();
        actions.add(new PersonalizerRankableAction().setId("Person").setFeatures(features));
        PersonalizerRankOptions request = new PersonalizerRankOptions().setActions(actions).setContextFeatures(null).setExcludedActions(null);
        // Action
        PersonalizerRankResult response = client.rank(request);
        // Assert
        assertEquals(actions.size(), response.getRanking().size());
        for (int i = 0; i < response.getRanking().size(); i++) {
            assertEquals(actions.get(i).getId(), response.getRanking().get(i).getId());
        }
    }

    private void rankServerFeatures(PersonalizerClient client) {

        List<BinaryData> contextFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new Context().setCurrentFeatures(new CurrentFeatures().setDay("Tuesday").setWeather("rainy"))));
                add(BinaryData.fromObject(new UserFeatures().setPayingUser(true).setFavoriteGenre("rainy").setHoursOnSite(0.12).setLastWatchedType("movie")));
            }
        };

        List<PersonalizerRankableAction> actions = new ArrayList<>();
        List<BinaryData> features1 = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));
            }
        };
        actions.add(new PersonalizerRankableAction().setId("Person1").setFeatures(features1));

        List<BinaryData> features2 = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("40-45")));
            }
        };
        actions.add(new PersonalizerRankableAction().setId("Person2").setFeatures(features2));

        ArrayList<String> excludeActions = new ArrayList<String>() {
            {
                add("Person1");
            }
        };
        String eventId = "123456789";
        PersonalizerRankOptions request = new PersonalizerRankOptions()
            .setActions(actions)
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
        List<BinaryData> contextFeatures = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new UserFeatures().setPayingUser(true).setFavoriteGenre("documentary").setHoursOnSite(0.12).setLastWatchedType("movie")));
                add(BinaryData.fromObject(new Context().setCurrentFeatures(new CurrentFeatures().setDay("tuesday").setWeather("rainy"))));
            }
        };

        List<BinaryData> features1 = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("30-35")));
            }
        };

        List<BinaryData> features2 = new ArrayList<BinaryData>() {
            {
                add(BinaryData.fromObject(new ActionFeatures().setVideoType("documentary").setVideoLength(35).setDirector("CarlSagan")));
                add(BinaryData.fromObject(new ActionCategory().setMostWatchedByAge("40-45")));
            }
        };

        List<PersonalizerRankableAction> actions = new ArrayList<PersonalizerRankableAction>();
        actions.add(new PersonalizerRankableAction().setId("Person1").setFeatures(features1));
        actions.add(new PersonalizerRankableAction().setId("Person2").setFeatures(features2));
        // Action
        PersonalizerRankResult response = client.rank(new PersonalizerRankOptions().setActions(actions).setContextFeatures(contextFeatures));
        assertEquals(actions.size(), response.getRanking().size());
    }
}


class CurrentFeatures {
    @JsonGetter
    public String getDay() {
        return day;
    }

    @JsonSetter
    public CurrentFeatures setDay(String day) {
        this.day = day;
        return this;
    }

    @JsonGetter
    public String getWeather() {
        return weather;
    }

    @JsonSetter
    public CurrentFeatures setWeather(String weather) {
        this.weather = weather;
        return this;
    }

    @JsonProperty
    private String day;

    @JsonProperty
    private String weather;
}

class Context {
    @JsonGetter
    public CurrentFeatures getFeatures() {
        return currentFeatures;
    }

    @JsonSetter
    public Context setCurrentFeatures(CurrentFeatures currentFeatures) {
        this.currentFeatures = currentFeatures;
        return this;
    }

    @JsonProperty
    CurrentFeatures currentFeatures;
}

class UserFeatures {
    @JsonGetter
    public boolean isPayingUser() {
        return isPayingUser;
    }

    @JsonSetter
    public UserFeatures setPayingUser(boolean payingUser) {
        isPayingUser = payingUser;
        return this;
    }

    @JsonGetter
    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    @JsonSetter
    public UserFeatures setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
        return this;
    }

    @JsonGetter
    public double getHoursOnSite() {
        return hoursOnSite;
    }

    @JsonSetter
    public UserFeatures setHoursOnSite(double hoursOnSite) {
        this.hoursOnSite = hoursOnSite;
        return this;
    }

    @JsonGetter
    public String getLastWatchedType() {
        return lastWatchedType;
    }

    @JsonSetter
    public UserFeatures setLastWatchedType(String lastWatchedType) {
        this.lastWatchedType = lastWatchedType;
        return this;
    }

    @JsonProperty
    private boolean isPayingUser;
    @JsonProperty
    private String favoriteGenre;
    @JsonProperty
    private double hoursOnSite;
    @JsonProperty
    private String lastWatchedType;
}

class ActionFeatures {
    @JsonGetter
    public String getVideoType() {
        return videoType;
    }

    @JsonSetter
    public ActionFeatures setVideoType(String videoType) {
        this.videoType = videoType;
        return this;
    }

    @JsonGetter
    public Integer getVideoLength() {
        return videoLength;
    }

    @JsonSetter
    public ActionFeatures setVideoLength(Integer videoLength) {
        this.videoLength = videoLength;
        return this;
    }

    @JsonGetter
    public String getDirector() {
        return director;
    }

    @JsonSetter
    public ActionFeatures setDirector(String director) {
        this.director = director;
        return this;
    }

    @JsonProperty
    String videoType;
    @JsonProperty
    Integer videoLength;
    @JsonProperty
    String director;
}
class ActionCategory {
    @JsonGetter
    public String getMostWatchedByAge() {
        return mostWatchedByAge;
    }

    @JsonSetter
    public ActionCategory setMostWatchedByAge(String mostWatchedByAge) {
        this.mostWatchedByAge = mostWatchedByAge;
        return this;
    }

    @JsonProperty
    String mostWatchedByAge;
}
