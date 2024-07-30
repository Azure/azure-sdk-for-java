// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.utils;

import com.azure.ai.vision.face.FaceServiceVersion;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.azure.core.test.TestBase.getHttpClients;

public class TestUtils {
    public static final String DISPLAY_NAME_WITH_ARGUMENTS = "[" + ParameterizedTest.INDEX_PLACEHOLDER + "] "
        + ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;

    public static final String EMPTY_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String FAKE_TOKEN = "FAKE_TOKEN";

    public static Stream<Arguments> createCombinationWithHttpClientsAndServiceVersions(Object[]... arrays) {
        return createCombination(getHttpClients().toArray(), getServiceVersions(), arrays);
    }

    public static Stream<Arguments> createCombination(Object[]... arrays) {
        Stream<Object[]> parentStream = prepareContainer(arrays.length);
        Stream<Object[]> returnSteam = createCombination(parentStream, arrays, 0, 0);

        return returnSteam.map(Arguments::of);
    }




    public static <TCommand> Stream<Arguments> createCombinationWithClientArguments(
            Stream<Triple<String, FaceServiceVersion, TCommand>> clientArguments, Object[]... arrays) {
        Stream<Object[]> parentStream = prepareContainer(arrays.length + 3);
        Stream<Object[]> combinationStream = parentStream.flatMap(e1 -> clientArguments.map(triple -> {
            e1[0] = triple.getLeft();
            e1[1] = triple.getMiddle();
            e1[2] = triple.getRight();
            return e1;
        }));

        combinationStream = createCombination(combinationStream, arrays, 0, 3);
        return combinationStream.map(objects -> Arguments.of(Arrays.copyOf(objects, objects.length)));
    }

    public static Stream<Arguments> createCombination(Stream<?> initStream, Object[]... arrays) {
        Stream<Object[]> parentStream = prepareContainer(arrays.length + 1);
        Stream<Object[]> combinationStream = parentStream.flatMap(e1 -> initStream.map(e2 -> {
            e1[0] = e2;
            return e1;
        }));

        combinationStream = createCombination(combinationStream, arrays, 0, 1);
        return combinationStream.map(objects -> Arguments.of(Arrays.copyOf(objects, objects.length)));
    }

    private static Stream<Object[]> createCombination(
            Stream<Object[]> parentStream, Object[][] arrays, final int groupIndex, final int bufferIndex) {
        if (groupIndex >= arrays.length) {
            return parentStream;
        }

        Stream<Object[]> combinedStream = parentStream.flatMap(e1 -> Arrays.stream(arrays[groupIndex]).map(e2 -> {
            e1[bufferIndex] = e2;
            return e1;
        }));

        return  createCombination(combinedStream, arrays, groupIndex + 1, bufferIndex + 1);
    }

    private static Stream<Object[]> prepareContainer(int length) {
        Object[] buffer = new Object[length];
        return Arrays.stream(new Object[][]{buffer});
    }

    public static FaceServiceVersion[] getServiceVersions() {
        return FaceServiceVersion.values();
    }

    static Stream<Arguments> getHttpClientsAndServiceVersions() {
        return createCombination(getHttpClients().toArray(), getServiceVersions());
    }
}
