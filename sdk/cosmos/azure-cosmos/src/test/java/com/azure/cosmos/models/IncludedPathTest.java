// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.DataType;
import com.azure.cosmos.implementation.Index;
import com.azure.cosmos.implementation.RangeIndex;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(includedPathIndexes).hasSize(2);
        assertThat(((RangeIndex) includedPathIndexes.get(0)).getDataType()).isEqualTo(DataType.STRING);
        assertThat(((RangeIndex) includedPathIndexes.get(0)).getPrecision()).isEqualTo(-1);
        assertThat(((RangeIndex) includedPathIndexes.get(1)).getDataType()).isEqualTo(DataType.NUMBER);
        assertThat(((RangeIndex) includedPathIndexes.get(1)).getPrecision()).isEqualTo(-1);

        assertThat(includedPath.getPath()).isEqualTo(path);
    }
}
