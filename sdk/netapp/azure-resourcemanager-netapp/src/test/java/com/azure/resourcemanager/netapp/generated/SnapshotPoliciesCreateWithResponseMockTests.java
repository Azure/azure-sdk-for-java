// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.netapp.generated;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.resourcemanager.netapp.NetAppFilesManager;
import com.azure.resourcemanager.netapp.models.DailySchedule;
import com.azure.resourcemanager.netapp.models.HourlySchedule;
import com.azure.resourcemanager.netapp.models.MonthlySchedule;
import com.azure.resourcemanager.netapp.models.SnapshotPolicy;
import com.azure.resourcemanager.netapp.models.WeeklySchedule;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public final class SnapshotPoliciesCreateWithResponseMockTests {
    @Test
    public void testCreateWithResponse() throws Exception {
        String responseStr
            = "{\"etag\":\"uwc\",\"properties\":{\"hourlySchedule\":{\"snapshotsToKeep\":1462697228,\"minute\":1843786712,\"usedBytes\":5127922681099994380},\"dailySchedule\":{\"snapshotsToKeep\":1313193442,\"hour\":1117030946,\"minute\":1305077670,\"usedBytes\":8132569337795273227},\"weeklySchedule\":{\"snapshotsToKeep\":1617896998,\"day\":\"h\",\"hour\":884114239,\"minute\":1075398674,\"usedBytes\":1841698857513805001},\"monthlySchedule\":{\"snapshotsToKeep\":732294522,\"daysOfMonth\":\"lxqzvn\",\"hour\":1381551038,\"minute\":1534178183,\"usedBytes\":8510007533425740916},\"enabled\":false,\"provisioningState\":\"amikzebrqbsm\"},\"location\":\"ziqgfuh\",\"tags\":{\"czznvfbycjsxj\":\"ruswhv\",\"vumwmxqh\":\"wix\"},\"id\":\"dvnoamldsehaohdj\",\"name\":\"hflzokxco\",\"type\":\"pelnjetag\"}";

        HttpClient httpClient
            = response -> Mono.just(new MockHttpResponse(response, 200, responseStr.getBytes(StandardCharsets.UTF_8)));
        NetAppFilesManager manager = NetAppFilesManager.configure()
            .withHttpClient(httpClient)
            .authenticate(tokenRequestContext -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)),
                new AzureProfile("", "", AzureEnvironment.AZURE));

        SnapshotPolicy response = manager.snapshotPolicies()
            .define("fwyfwlwxjwet")
            .withRegion("epttaqu")
            .withExistingNetAppAccount("ophzfylsgcrp", "bcunezzceze")
            .withTags(mapOf("gehkfkimrtixokff", "wemxswvruunzz", "qwhix", "yinljqe"))
            .withHourlySchedule(new HourlySchedule().withSnapshotsToKeep(1236951545)
                .withMinute(551396932)
                .withUsedBytes(454317782337035543L))
            .withDailySchedule(new DailySchedule().withSnapshotsToKeep(544384144)
                .withHour(899185817)
                .withMinute(96806467)
                .withUsedBytes(1825722132494562073L))
            .withWeeklySchedule(new WeeklySchedule().withSnapshotsToKeep(307394034)
                .withDay("zjkjexfdeqv")
                .withHour(1342941735)
                .withMinute(1297569440)
                .withUsedBytes(2837842827640230428L))
            .withMonthlySchedule(new MonthlySchedule().withSnapshotsToKeep(1992665604)
                .withDaysOfMonth("f")
                .withHour(1109570414)
                .withMinute(1864709283)
                .withUsedBytes(6471426075457466306L))
            .withEnabled(false)
            .create();

        Assertions.assertEquals("ziqgfuh", response.location());
        Assertions.assertEquals("ruswhv", response.tags().get("czznvfbycjsxj"));
        Assertions.assertEquals(1462697228, response.hourlySchedule().snapshotsToKeep());
        Assertions.assertEquals(1843786712, response.hourlySchedule().minute());
        Assertions.assertEquals(5127922681099994380L, response.hourlySchedule().usedBytes());
        Assertions.assertEquals(1313193442, response.dailySchedule().snapshotsToKeep());
        Assertions.assertEquals(1117030946, response.dailySchedule().hour());
        Assertions.assertEquals(1305077670, response.dailySchedule().minute());
        Assertions.assertEquals(8132569337795273227L, response.dailySchedule().usedBytes());
        Assertions.assertEquals(1617896998, response.weeklySchedule().snapshotsToKeep());
        Assertions.assertEquals("h", response.weeklySchedule().day());
        Assertions.assertEquals(884114239, response.weeklySchedule().hour());
        Assertions.assertEquals(1075398674, response.weeklySchedule().minute());
        Assertions.assertEquals(1841698857513805001L, response.weeklySchedule().usedBytes());
        Assertions.assertEquals(732294522, response.monthlySchedule().snapshotsToKeep());
        Assertions.assertEquals("lxqzvn", response.monthlySchedule().daysOfMonth());
        Assertions.assertEquals(1381551038, response.monthlySchedule().hour());
        Assertions.assertEquals(1534178183, response.monthlySchedule().minute());
        Assertions.assertEquals(8510007533425740916L, response.monthlySchedule().usedBytes());
        Assertions.assertEquals(false, response.enabled());
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
