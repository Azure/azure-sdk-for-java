// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.perf.models.UserData;
import com.azure.core.perf.models.UserDatabase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataFactory {

    public static UserDatabase generateUserDatabase(long size) {

        // Setup user data with 100 bytes content.
        UserData sampleUserData = new UserData();
        sampleUserData.setActive(true);
        sampleUserData.setAge(47);
        sampleUserData.setBalance("$2000");
        sampleUserData.setCompany("Cueon");
        sampleUserData.setId("72356");
        sampleUserData.setPicture("a.jpg");
        sampleUserData.setEyeColor("brown");
        sampleUserData.setGuid("231151cb-cd5f-4412-8b33-dac78c075e45");
        sampleUserData.setIndex(1);
        sampleUserData.setAbout("Cottage out enabled was");

        int units = (int) size / 100;
        int remainder = (int) size % 100;

        List<UserData> userDataList = Flux.range(0, units).flatMap(integer -> {
            return Mono.just(sampleUserData.clone());
        }).collectList().block();

        if (remainder > 0) {
            UserData userData = new UserData();
            userData.setAbout(genreateRandomString(remainder));
            userDataList.add(userData);
        }

        UserDatabase userDatabase = new UserDatabase();
        userDatabase.setUserList(userDataList);

        return userDatabase;
    }

    private static String genreateRandomString(int targetLength) {
        int begin = 97; // letter 'a'
        int end = 122; // letter 'z'

        return ThreadLocalRandom.current()
            .ints(begin, end + 1)
            .limit(targetLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
