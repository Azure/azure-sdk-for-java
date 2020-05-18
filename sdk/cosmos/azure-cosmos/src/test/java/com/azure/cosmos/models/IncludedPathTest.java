// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class IncludedPathTest {

    @Test(groups = {"unit"})
    public void setAndGetIncludedPath() {
        String path = "/*";
        IncludedPath includedPath = new IncludedPath(path);

        List<Index> indexes = new ArrayList<>();
        indexes.add(Index.range(DataType.STRING, -1));
        indexes.add(Index.range(DataType.NUMBER, -1));
        includedPath.setIndexes(indexes);

        List<Index> includedPathIndexes = new ArrayList<>(includedPath.getIndexes());
        Assertions.assertThat(includedPathIndexes).hasSize(2);
        Assertions.assertThat(((RangeIndex) includedPathIndexes.get(0)).getDataType()).isEqualTo(DataType.STRING);
        Assertions.assertThat(((RangeIndex) includedPathIndexes.get(0)).getPrecision()).isEqualTo(-1);
        Assertions.assertThat(((RangeIndex) includedPathIndexes.get(1)).getDataType()).isEqualTo(DataType.NUMBER);
        Assertions.assertThat(((RangeIndex) includedPathIndexes.get(1)).getPrecision()).isEqualTo(-1);

        Assertions.assertThat(includedPath.getPath()).isEqualTo(path);
    }
}
