// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import org.testng.annotations.Test;

import java.util.Collection;

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
        Collection<Index> indexes = path.indexes();
        assertThat(indexes).hasSize(2);
        assertThat(indexes).usingFieldByFieldElementComparator().contains(Index.Range(DataType.STRING, -1));
        assertThat(indexes).usingFieldByFieldElementComparator().contains(Index.Range(DataType.NUMBER, -1));
    }
}