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
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

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
        // Let us say that they like the action presented to them for the "Main Article" (first) slot. So we associate a
        // reward of 1.
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
        return Arrays.asList(
            BinaryData.fromObject(new UserProfile().setProfileType("AnonymousUser").setLatLong("47.6,-122.1")),
            BinaryData.fromObject(new Environment().setDayOfMonth("28").setMonthOfYear("8").setWeather("Sunny")),
            BinaryData.fromObject(new Device().setMobile(true).setWindows(true)),
            BinaryData.fromObject(new RecentActivity().setItemsInCart(3)));
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
        return Arrays.asList(getSlot1(), getSlot2());
    }

    private static PersonalizerSlotOptions getSlot1() {
        return new PersonalizerSlotOptions().setId("Main Article")
            .setBaselineAction("NewsArticle")
            .setFeatures(Arrays.asList(
                BinaryData.fromObject(new SlotPositionFeatures().setSize("Large").setPosition("Top Middle"))))
            .setExcludedActions(Arrays.asList("SportsArticle", "EntertainmentArticle"));
    }

    private static PersonalizerSlotOptions getSlot2() {
        return new PersonalizerSlotOptions().setId("Side Bar")
            .setBaselineAction("SportsArticle")
            .setFeatures(Arrays.asList(
                BinaryData.fromObject(new SlotPositionFeatures().setSize("Small").setPosition("Bottom Right"))))
            .setExcludedActions(Arrays.asList("EntertainmentArticle"));
    }

    static <T> T deserializationHelper(JsonReader jsonReader, Supplier<T> createObject,
        ReadValue<T> readValue) throws IOException {
        return jsonReader.readObject(reader -> {
            T object = createObject.get();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                readValue.read(reader, fieldName, object);
            }

            return object;
        });
    }

    private interface ReadValue<T> {
        void read(JsonReader reader, String fieldName, T object) throws IOException;
    }

    static class FeatureMetadata implements JsonSerializable<FeatureMetadata> {
        String featureType;

        public String getFeatureType() {
            return featureType;
        }

        public FeatureMetadata setFeatureType(String featureType) {
            this.featureType = featureType;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("featureType", featureType)
                .writeEndObject();
        }

        public static FeatureMetadata fromJson(JsonReader jsonReader) throws IOException {
            return deserializationHelper(jsonReader, FeatureMetadata::new, (reader, fieldName, featureMetadata) -> {
                if ("featureType".equals(fieldName)) {
                    featureMetadata.featureType = reader.getString();
                } else {
                    reader.skipChildren();
                }
            });
        }

    }

    static class SlotPositionFeatures implements JsonSerializable<SlotPositionFeatures> {
        String size;
        String position;

        public String getSize() {
            return size;
        }

        public SlotPositionFeatures setSize(String size) {
            this.size = size;
            return this;
        }

        public String getPosition() {
            return position;
        }

        public SlotPositionFeatures setPosition(String position) {
            this.position = position;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("size", size)
                .writeStringField("position", position)
                .writeEndObject();
        }

        public static SlotPositionFeatures fromJson(JsonReader jsonReader) throws IOException {
            return deserializationHelper(jsonReader, SlotPositionFeatures::new, (reader, fieldName, features) -> {
                if ("size".equals(fieldName)) {
                    features.size = reader.getString();
                } else if ("position".equals(fieldName)) {
                    features.position = reader.getString();
                } else {
                    reader.skipChildren();
                }
            });
        }
    }

    static class UserProfile implements JsonSerializable<UserProfile> {
        String profileType;
        String latLong;

        public String getProfileType() {
            return profileType;
        }

        public UserProfile setProfileType(String profileType) {
            this.profileType = profileType;
            return this;
        }

        public String getLatLong() {
            return latLong;
        }

        public UserProfile setLatLong(String latLong) {
            this.latLong = latLong;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("profileType", profileType)
                .writeStringField("latLong", latLong)
                .writeEndObject();
        }

        public static UserProfile fromJson(JsonReader jsonReader) throws IOException {
            return deserializationHelper(jsonReader, UserProfile::new, (reader, fieldName, userProfile) -> {
                if ("profileType".equals(fieldName)) {
                    userProfile.profileType = reader.getString();
                } else if ("latLong".equals(fieldName)) {
                    userProfile.latLong = reader.getString();
                } else {
                    reader.skipChildren();
                }
            });
        }
    }

    static class Environment implements JsonSerializable<Environment> {
        String dayOfMonth;
        String monthOfYear;
        String weather;

        public String getDayOfMonth() {
            return dayOfMonth;
        }

        public Environment setDayOfMonth(String dayOfMonth) {
            this.dayOfMonth = dayOfMonth;
            return this;
        }

        public String getMonthOfYear() {
            return monthOfYear;
        }

        public Environment setMonthOfYear(String monthOfYear) {
            this.monthOfYear = monthOfYear;
            return this;
        }

        public String getWeather() {
            return weather;
        }

        public Environment setWeather(String weather) {
            this.weather = weather;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeStringField("dayOfMonth", dayOfMonth)
                .writeStringField("monthOfYear", monthOfYear)
                .writeStringField("weather", weather)
                .writeEndObject();
        }

        public static Environment fromJson(JsonReader jsonReader) throws IOException {
            return deserializationHelper(jsonReader, Environment::new, (reader, fieldName, environment) -> {
                if ("dayOfMonth".equals(fieldName)) {
                    environment.dayOfMonth = reader.getString();
                } else if ("monthOfYear".equals(fieldName)) {
                    environment.monthOfYear = reader.getString();
                } else if ("weather".equals(fieldName)) {
                    environment.weather = reader.getString();
                } else {
                    reader.skipChildren();
                }
            });
        }
    }

    static class Device implements JsonSerializable<Device> {
        boolean isMobile;
        boolean isWindows;

        public boolean isMobile() {
            return isMobile;
        }

        public Device setMobile(boolean mobile) {
            isMobile = mobile;
            return this;
        }

        public boolean isWindows() {
            return isWindows;
        }

        public Device setWindows(boolean windows) {
            isWindows = windows;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject()
                .writeBooleanField("isMobile", isMobile)
                .writeBooleanField("isWindows", isWindows)
                .writeEndObject();
        }

        public static Device fromJson(JsonReader jsonReader) throws IOException {
            return deserializationHelper(jsonReader, Device::new, (reader, fieldName, device) -> {
                if ("isMobile".equals(fieldName)) {
                    device.isMobile = reader.getBoolean();
                } else if ("isWindows".equals(fieldName)) {
                    device.isWindows = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            });
        }
    }

    static class RecentActivity implements JsonSerializable<RecentActivity> {
        Integer itemsInCart;

        public Integer getItemsInCart() {
            return itemsInCart;
        }

        public RecentActivity setItemsInCart(Integer itemsInCart) {
            this.itemsInCart = itemsInCart;
            return this;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            return jsonWriter.writeStartObject().writeNumberField("itemsInCart", itemsInCart).writeEndObject();
        }

        public static RecentActivity fromJson(JsonReader jsonReader) throws IOException {
            return deserializationHelper(jsonReader, RecentActivity::new, (reader, fieldName, activity) -> {
                if ("itemsInCart".equals(fieldName)) {
                    activity.itemsInCart = reader.getNullable(JsonReader::getInt);
                } else {
                    reader.skipChildren();
                }
            });
        }
    }
}
