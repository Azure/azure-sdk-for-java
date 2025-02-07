// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * ContextTest is used to test out the recipe that converts code to use the
 * new clientcore Context class.
 */
public class ContextTest extends RecipeTestBase {
    /**
     * This test method is used to make sure that the Context class is updated
     */
    @Test
    void testImportReplaceContext() {
        @Language("java") String before = "import com.azure.core.util.Context;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){}";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.Context;";
        after += "\npublic class Testing {";
        after += "\n  public Testing(){}";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}
