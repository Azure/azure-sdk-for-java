// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.google.common.collect.ArrayListMultimap;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class IncludedPathTest {

    @Test(groups = {"unit"})
    public void deserialize() {
        String json = "{" +
                "  'path': '\\/*'," +
                "  'indexes': [" +
                "    {" +
                "      'kind': 'Range'," +
                "      'dataType': 'String'," +
                "      'precision': -1" +
                "    }," +
                "    {" +
                "      'kind': 'Range'," +
                "      'dataType': 'Number'," +
                "      'precision': -1" +
                "    }" +
                "  ]" +
                "}";
        IncludedPath path = new IncludedPath(json);
        List<Index> indexes = new ArrayList<>(path.getIndexes());
        Assertions.assertThat(indexes).hasSize(2);
        Assertions.assertThat(((RangeIndex) indexes.get(0)).getDataType()).isEqualTo(DataType.STRING);
        Assertions.assertThat(((RangeIndex) indexes.get(0)).getPrecision()).isEqualTo(-1);
        Assertions.assertThat(((RangeIndex) indexes.get(1)).getDataType()).isEqualTo(DataType.NUMBER);
        Assertions.assertThat(((RangeIndex) indexes.get(1)).getPrecision()).isEqualTo(-1);
    }
}
