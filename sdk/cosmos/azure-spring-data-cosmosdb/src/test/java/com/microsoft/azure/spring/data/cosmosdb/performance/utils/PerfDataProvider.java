// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.performance.utils;

import com.microsoft.azure.spring.data.cosmosdb.performance.domain.PerfPerson;
import org.assertj.core.util.Lists;

import java.util.List;
import java.util.UUID;

public class PerfDataProvider {
    public static List<PerfPerson> getPerfData(int count) {
        final List<PerfPerson> personList = Lists.newArrayList();

        for (int i = 1; i <= count; i++) {
            final PerfPerson person = new PerfPerson(randomId(), "fake name-" + randomSuffix());
            personList.add(person);
        }

        return personList;
    }

    public static List<Iterable<PerfPerson>> getMultiPerfData(int size, int listCount) {
        final List<Iterable<PerfPerson>> personList = Lists.newArrayList();

        for (int i = 0; i < listCount; i++) {
            personList.add(getPerfData(size));
        }

        return personList;
    }

    private static String randomId() {
        return UUID.randomUUID().toString().substring(0, 10);
    }

    private static String randomSuffix() {
        // It's not random, but just for distinguishing
        return UUID.randomUUID().toString().substring(0, 4);
    }
}
