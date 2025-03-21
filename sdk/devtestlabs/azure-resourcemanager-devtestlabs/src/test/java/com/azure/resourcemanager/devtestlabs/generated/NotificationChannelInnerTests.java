// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devtestlabs.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.devtestlabs.fluent.models.NotificationChannelInner;
import com.azure.resourcemanager.devtestlabs.models.Event;
import com.azure.resourcemanager.devtestlabs.models.NotificationChannelEventType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class NotificationChannelInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        NotificationChannelInner model = BinaryData.fromString(
            "{\"properties\":{\"webHookUrl\":\"lrri\",\"emailRecipient\":\"ywdxsmic\",\"notificationLocale\":\"rwfscjfnynszquj\",\"description\":\"dvoqyt\",\"events\":[{\"eventName\":\"AutoShutdown\"}],\"createdDate\":\"2021-09-24T22:06:57Z\",\"provisioningState\":\"gyavu\",\"uniqueIdentifier\":\"thjoxoism\"},\"location\":\"ksbpimlqoljx\",\"tags\":{\"dwl\":\"xxlxsffgcvizq\",\"youpfgfbkj\":\"w\",\"ttsttktlahbqact\":\"bdyhgkfminsgowz\",\"qqqxhrnxrx\":\"tgzukxitmmqt\"},\"id\":\"pjui\",\"name\":\"av\",\"type\":\"k\"}")
            .toObject(NotificationChannelInner.class);
        Assertions.assertEquals("ksbpimlqoljx", model.location());
        Assertions.assertEquals("xxlxsffgcvizq", model.tags().get("dwl"));
        Assertions.assertEquals("lrri", model.webhookUrl());
        Assertions.assertEquals("ywdxsmic", model.emailRecipient());
        Assertions.assertEquals("rwfscjfnynszquj", model.notificationLocale());
        Assertions.assertEquals("dvoqyt", model.description());
        Assertions.assertEquals(NotificationChannelEventType.AUTO_SHUTDOWN, model.events().get(0).eventName());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        NotificationChannelInner model = new NotificationChannelInner().withLocation("ksbpimlqoljx")
            .withTags(mapOf("dwl", "xxlxsffgcvizq", "youpfgfbkj", "w", "ttsttktlahbqact", "bdyhgkfminsgowz",
                "qqqxhrnxrx", "tgzukxitmmqt"))
            .withWebhookUrl("lrri")
            .withEmailRecipient("ywdxsmic")
            .withNotificationLocale("rwfscjfnynszquj")
            .withDescription("dvoqyt")
            .withEvents(Arrays.asList(new Event().withEventName(NotificationChannelEventType.AUTO_SHUTDOWN)));
        model = BinaryData.fromObject(model).toObject(NotificationChannelInner.class);
        Assertions.assertEquals("ksbpimlqoljx", model.location());
        Assertions.assertEquals("xxlxsffgcvizq", model.tags().get("dwl"));
        Assertions.assertEquals("lrri", model.webhookUrl());
        Assertions.assertEquals("ywdxsmic", model.emailRecipient());
        Assertions.assertEquals("rwfscjfnynszquj", model.notificationLocale());
        Assertions.assertEquals("dvoqyt", model.description());
        Assertions.assertEquals(NotificationChannelEventType.AUTO_SHUTDOWN, model.events().get(0).eventName());
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
