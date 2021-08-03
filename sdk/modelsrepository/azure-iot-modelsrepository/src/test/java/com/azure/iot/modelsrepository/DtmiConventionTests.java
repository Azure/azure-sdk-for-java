// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

class DtmiConventionTests {

    @ParameterizedTest
    @CsvSource({
        "dtmi:com:Example:Model;1, dtmi/com/example/model-1.json",
        "dtmi:com:example:Model;1, dtmi/com/example/model-1.json",
        "dtmi:com:example:Model:1, ",
        "'',",
        ","
    })
    public void dtmiToPathTest(String input, String expected) {

        if (expected == null) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> DtmiConventions.dtmiToPath(input));
            return;
        }

        String actualValue = DtmiConventions.dtmiToPath(input);
        Assertions.assertEquals(expected, actualValue);
    }

    @ParameterizedTest
    @CsvSource({
        "https://localhost/repository/, https://localhost/repository/dtmi/com/example/thermostat-1.json",
        "https://localhost/REPOSITORY,  https://localhost/REPOSITORY/dtmi/com/example/thermostat-1.json",
        "file:///path/to/repository/,   file:///path/to/repository/dtmi/com/example/thermostat-1.json",
        "file://path/to/RepoSitory,     file://path/to/RepoSitory/dtmi/com/example/thermostat-1.json",
        "C:/path/to/repository/,        C:/path/to/repository/dtmi/com/example/thermostat-1.json",
        "//server//repository,          //server//repository/dtmi/com/example/thermostat-1.json"
    })
    public void getModelUriTests(String repository, String expectedUri) {
        final String dtmi = "dtmi:com:example:Thermostat;1";

        URI repositoryUri = TestHelper.convertToUri(repository);

        if (expectedUri == null || expectedUri.isEmpty()) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> DtmiConventions.getModelUri(dtmi, repositoryUri, false));
            return;
        }

        URI modelUri = DtmiConventions.getModelUri(dtmi, repositoryUri, false);
        Assertions.assertEquals(expectedUri, modelUri.toString());
    }

    @ParameterizedTest
    @CsvSource({
        "dtmi:com:example:Thermostat;1, true",
        "dtmi:contoso:scope:entity;2, true",
        "dtmi:com:example:Thermostat:1, false",
        "dtmi:com:example::Thermostat;1, false",
        "com:example:Thermostat;1, false",
        "'', false",
        "null, false"
    })
    public void isValidDtmi(String dtmi, boolean expected) {
        Assertions.assertEquals(expected, DtmiConventions.isValidDtmi(dtmi));
    }
}
