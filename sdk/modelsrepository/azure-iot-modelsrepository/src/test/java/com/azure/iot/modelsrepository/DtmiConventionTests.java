package com.azure.iot.modelsrepository;

import com.azure.iot.modelsrepository.implementation.DtmiConventions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DtmiConventionTests {

    @ParameterizedTest
    @CsvSource({
        "dtmi:com:Example:Model;1, dtmi/com/example/model-1.json",
        "dtmi:com:example:Model;1, dtmi/com/example/model-1.json",
        "dtmi:com:example:Model:1, ",
        "'',",
        ","
    })
    public void dtmiToPathTest(String input, String expected) {
        String actualValue = DtmiConventions.dtmiToPath(input);
        Assertions.assertEquals(expected, actualValue);
    }

    @ParameterizedTest
    @CsvSource({
        "dtmi:com:example:Thermostat;1, dtmi/com/example/thermostat-1.json, https://localhost/repository",
        "dtmi:com:example:Model;1, dtmi/com/example/model-1.json, C:\\fakeRegistry",
        "dtmi:com:example:Thermostat;1, dtmi/com/example/thermostat-1.json, /me/fakeRegistry",
        "dtmi:com:example:Thermostat:1, ,https://localhost/repository",
        "dtmi:com:example:Thermostat:1, ,/me/fakeRegistry"
    })
    public void dtmiToQualifiedPathTests(String dtmi, String expectedPath, String repository) {

        if (System.getProperty("os.name").startsWith("Windows")) {
            repository = repository.replace("\\", "/");
        }

        if (expectedPath == null || expectedPath.isEmpty()) {
            String finalRepository = repository;
            Assertions.assertThrows(IllegalArgumentException.class, () -> DtmiConventions.dtmiToQualifiedPath(dtmi, finalRepository, false));
            return;
        }

        String modelPath = DtmiConventions.dtmiToQualifiedPath(dtmi, repository, false);
        Assertions.assertEquals(modelPath, repository.concat("/").concat(expectedPath));

        String expandedModelPath = DtmiConventions.dtmiToQualifiedPath(dtmi, repository, true);
        Assertions.assertEquals(expandedModelPath, repository.concat("/").concat(expectedPath.replace(".json", ".expanded.json")));
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
