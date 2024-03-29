// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.frontdoor.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.frontdoor.fluent.models.LatencyScorecardInner;
import com.azure.resourcemanager.frontdoor.models.LatencyMetric;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class LatencyScorecardInnerTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        LatencyScorecardInner model =
            BinaryData
                .fromString(
                    "{\"properties\":{\"id\":\"rlazszrnw\",\"name\":\"indfpwpjyl\",\"description\":\"tlhflsjcdhszf\",\"endpointA\":\"fbgofeljagrqmqh\",\"endpointB\":\"vriiio\",\"startDateTimeUTC\":\"2021-04-25T18:11:53Z\",\"endDateTimeUTC\":\"2021-08-27T10:03:09Z\",\"country\":\"fk\",\"latencyMetrics\":[{\"name\":\"ex\",\"endDateTimeUTC\":\"wueluqhhahhxv\",\"aValue\":41.38071,\"bValue\":99.721016,\"delta\":54.870827,\"deltaPercent\":0.074237585,\"aCLower95CI\":85.248795,\"aHUpper95CI\":61.13008,\"bCLower95CI\":89.108795,\"bUpper95CI\":78.10289},{\"name\":\"qs\",\"endDateTimeUTC\":\"qxujxukndxd\",\"aValue\":84.70417,\"bValue\":35.546135,\"delta\":47.985943,\"deltaPercent\":54.662663,\"aCLower95CI\":41.437172,\"aHUpper95CI\":46.247612,\"bCLower95CI\":21.394676,\"bUpper95CI\":94.70884},{\"name\":\"whbotzingamv\",\"endDateTimeUTC\":\"ho\",\"aValue\":41.892742,\"bValue\":85.72325,\"delta\":94.45262,\"deltaPercent\":97.52175,\"aCLower95CI\":24.885792,\"aHUpper95CI\":24.78965,\"bCLower95CI\":77.53663,\"bUpper95CI\":46.93581}]},\"location\":\"nwcvtbvkayhmtnv\",\"tags\":{\"cjaesgvvs\":\"atkzwpcnpw\",\"wygzlvdnkfxusem\":\"cyajguqf\",\"pfcqdp\":\"wzrmuh\"},\"id\":\"qxqvpsvuoymgc\",\"name\":\"elvezrypq\",\"type\":\"mfe\"}")
                .toObject(LatencyScorecardInner.class);
        Assertions.assertEquals("nwcvtbvkayhmtnv", model.location());
        Assertions.assertEquals("atkzwpcnpw", model.tags().get("cjaesgvvs"));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        LatencyScorecardInner model =
            new LatencyScorecardInner()
                .withLocation("nwcvtbvkayhmtnv")
                .withTags(mapOf("cjaesgvvs", "atkzwpcnpw", "wygzlvdnkfxusem", "cyajguqf", "pfcqdp", "wzrmuh"))
                .withLatencyMetrics(Arrays.asList(new LatencyMetric(), new LatencyMetric(), new LatencyMetric()));
        model = BinaryData.fromObject(model).toObject(LatencyScorecardInner.class);
        Assertions.assertEquals("nwcvtbvkayhmtnv", model.location());
        Assertions.assertEquals("atkzwpcnpw", model.tags().get("cjaesgvvs"));
    }

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
