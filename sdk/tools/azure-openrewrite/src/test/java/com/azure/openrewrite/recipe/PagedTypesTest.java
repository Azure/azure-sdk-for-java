// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.openrewrite.recipe;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;

import static org.openrewrite.java.Assertions.java;

public class PagedTypesTest extends RecipeTestBase {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("com.azure.openrewrite.recipe.azure.core.http.rest");
    }

    @Test
    public void addsStringContinuationTokenType() {
        rewriteRun(java("import com.azure.core.http.rest.PagedIterable;\n"
            + "import com.azure.core.http.rest.PagedResponse;\n\n"
            + "import java.util.List;\n\n"
            + "class PagingSample {\n"
            + "    PagedIterable<String> values;\n"
            + "    PagedResponse<String> page;\n"
            + "    List<PagedIterable<String>> nestedValues;\n"
            + "}\n", "import io.clientcore.core.http.paging.PagedIterable;\n"
                + "import io.clientcore.core.http.paging.PagedResponse;\n\n"
                + "import java.util.List;\n\n"
                + "class PagingSample {\n"
                + "    PagedIterable<String, String> values;\n"
                + "    PagedResponse<String, String> page;\n"
                + "    List<PagedIterable<String, String>> nestedValues;\n"
                + "}\n"));
    }

    @Test
    public void leavesAlreadyGenericClientCoreTypeUnchanged() {
        rewriteRun(java("import io.clientcore.core.http.paging.PagedIterable;\n\n"
            + "class PagingSample {\n"
            + "    PagedIterable<String, ContinuationToken> values;\n"
            + "    static class ContinuationToken { }\n"
            + "}\n"));
    }
}
