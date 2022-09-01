package com.azure.communication.identity;

import org.junit.jupiter.params.provider.Arguments;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TokenCustomExpirationTimeHelper {

    static Stream<Arguments> getValidExpirationTimes() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of("MinValidCustomExpiration", Duration.ofMinutes(60)));
        argumentsList.add(Arguments.of("MaxValidCustomExpiration", Duration.ofMinutes(1440)));

        return argumentsList.stream();
    }

    static Stream<Arguments> getInvalidExpirationTimes() {
        List<Arguments> argumentsList = new ArrayList<>();
        argumentsList.add(Arguments.of("MaxInvalidCustomExpiration", Duration.ofMinutes(59)));
        argumentsList.add(Arguments.of("MinInvalidCustomExpiration", Duration.ofMinutes(1441)));

        return argumentsList.stream();
    }
}
