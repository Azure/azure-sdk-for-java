// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.UInt128;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DirectContinuationTokenTest {

    @Test(groups = { "unit" })
    public void canParseWithEmptyLastHash() {
        String input = "{\"lastHash\":\"\",\"sourceToken\":\"{\\\"token\\\":\\\"+RID:P7IRAJqz+Ee2FAoAAAAACA==#RT:1#SRC:1#TRC:10#ISV:2#IEO:65551#QCF:7#FPC:AggoAAAAACAAACgAAAAAIAAAKAAAAAAgAAA2ALEUQAIRAAYAMgAADOAAEgAAoD4AJAAKQAyAPAgCAGICMAAAwBgAgAGAAAcAABgDAAAOAwAAEA==\\\",\\\"range\\\":\\\"{\\\\\\\"min\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"max\\\\\\\":\\\\\\\"05C1E0\\\\\\\",\\\\\\\"isMinInclusive\\\\\\\":true,\\\\\\\"isMaxInclusive\\\\\\\":false}\\\"}\"}";

        Utils.ValueHolder<DistinctContinuationToken> result = new Utils.ValueHolder<>();

        assertThat(DistinctContinuationToken.tryParse(input, result)).isEqualTo(true);
    }

    @Test(groups = { "unit" })
    public void canParseWithNullLastHash() {
        String input = "{\"lastHash\":null,\"sourceToken\":\"{\\\"token\\\":\\\"+RID:P7IRAJqz+Ee2FAoAAAAACA==#RT:1#SRC:1#TRC:10#ISV:2#IEO:65551#QCF:7#FPC:AggoAAAAACAAACgAAAAAIAAAKAAAAAAgAAA2ALEUQAIRAAYAMgAADOAAEgAAoD4AJAAKQAyAPAgCAGICMAAAwBgAgAGAAAcAABgDAAAOAwAAEA==\\\",\\\"range\\\":\\\"{\\\\\\\"min\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"max\\\\\\\":\\\\\\\"05C1E0\\\\\\\",\\\\\\\"isMinInclusive\\\\\\\":true,\\\\\\\"isMaxInclusive\\\\\\\":false}\\\"}\"}";

        Utils.ValueHolder<DistinctContinuationToken> result = new Utils.ValueHolder<>();

        assertThat(DistinctContinuationToken.tryParse(input, result)).isEqualTo(true);

        input = "{\"sourceToken\":\"{\\\"token\\\":\\\"+RID:P7IRAJqz+Ee2FAoAAAAACA==#RT:1#SRC:1#TRC:10#ISV:2#IEO:65551#QCF:7#FPC:AggoAAAAACAAACgAAAAAIAAAKAAAAAAgAAA2ALEUQAIRAAYAMgAADOAAEgAAoD4AJAAKQAyAPAgCAGICMAAAwBgAgAGAAAcAABgDAAAOAwAAEA==\\\",\\\"range\\\":\\\"{\\\\\\\"min\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"max\\\\\\\":\\\\\\\"05C1E0\\\\\\\",\\\\\\\"isMinInclusive\\\\\\\":true,\\\\\\\"isMaxInclusive\\\\\\\":false}\\\"}\"}";
        assertThat(DistinctContinuationToken.tryParse(input, result)).isEqualTo(true);
    }

    @Test(groups = { "unit" })
    public void canParseWithValidLastHash() {
        String input = "{\"lastHash\":\"00-00-00-00-00-00-30-39-00-00-00-00-00-00-1A-85\",\"sourceToken\":\"{\\\"token\\\":\\\"+RID:P7IRAJqz+Ee2FAoAAAAACA==#RT:1#SRC:1#TRC:10#ISV:2#IEO:65551#QCF:7#FPC:AggoAAAAACAAACgAAAAAIAAAKAAAAAAgAAA2ALEUQAIRAAYAMgAADOAAEgAAoD4AJAAKQAyAPAgCAGICMAAAwBgAgAGAAAcAABgDAAAOAwAAEA==\\\",\\\"range\\\":\\\"{\\\\\\\"min\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"max\\\\\\\":\\\\\\\"05C1E0\\\\\\\",\\\\\\\"isMinInclusive\\\\\\\":true,\\\\\\\"isMaxInclusive\\\\\\\":false}\\\"}\"}";

        Utils.ValueHolder<DistinctContinuationToken> result = new Utils.ValueHolder<>();

        assertThat(DistinctContinuationToken.tryParse(input, result)).isEqualTo(true);
        assertThat(result.v.getLastHash()).isEqualTo(new UInt128(12345, 6789));
    }

    @Test(groups = { "unit" })
    public void canParseWithInvalidLastHash() {
        String input = "{\"lastHash\":\"12345\",\"sourceToken\":\"{\\\"token\\\":\\\"+RID:P7IRAJqz+Ee2FAoAAAAACA==#RT:1#SRC:1#TRC:10#ISV:2#IEO:65551#QCF:7#FPC:AggoAAAAACAAACgAAAAAIAAAKAAAAAAgAAA2ALEUQAIRAAYAMgAADOAAEgAAoD4AJAAKQAyAPAgCAGICMAAAwBgAgAGAAAcAABgDAAAOAwAAEA==\\\",\\\"range\\\":\\\"{\\\\\\\"min\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"max\\\\\\\":\\\\\\\"05C1E0\\\\\\\",\\\\\\\"isMinInclusive\\\\\\\":true,\\\\\\\"isMaxInclusive\\\\\\\":false}\\\"}\"}";

        Utils.ValueHolder<DistinctContinuationToken> result = new Utils.ValueHolder<>();

        assertThat(DistinctContinuationToken.tryParse(input, result)).isEqualTo(false);
    }
}
