// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestResourceProviderRegistration {
    @Test
    public void testManuallyUnregisteredRp() {
        String error = "{\"error\":{\"code\":\"MissingSubscriptionRegistration\",\"message\":\"The subscription is not registered to use namespace 'Microsoft.Devices'. See https://aka.ms/rps-not-found for how to register subscriptions.\"}}";
        Matcher matcher = Pattern.compile(".*'(.*)'").matcher(error);
        matcher.find();
        Assertions.assertEquals("Microsoft.Devices", matcher.group(1));
    }

    @Test
    public void testRpInNewSubscription() {
        String error = "{\"error\":{\"code\":\"MissingSubscriptionRegistration\",\"message\":\"The subscription registration is in 'Unregistered' state. The subscription must be registered to use namespace 'Microsoft.Devices'. See https://aka.ms/rps-not-found for how to register subscriptions.\"}}";
        Matcher matcher = Pattern.compile(".*'(.*)'").matcher(error);
        matcher.find();
        Assertions.assertEquals("Microsoft.Devices", matcher.group(1));
    }
}
