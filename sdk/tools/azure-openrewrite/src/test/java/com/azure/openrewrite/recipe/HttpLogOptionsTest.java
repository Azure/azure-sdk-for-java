// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

// TODO: fix these tests to reflect current api
public class HttpLogOptionsTest extends RecipeTestBase {
    /**
     * HttpLogOptionsTest tests the recipe that changes
     * com.azure.core.http.policy.HttpLogDetailLevel to io.clientcore.core.http.models.HttpLogOptions.HttpLogDetailLevel
     * and com.azure.core.http.policy.HttpLogOptions to io.clientcore.core.http.models.HttpLogOptions
     *
     */

    /* Test to make sure HttpLogOptions and HttpLogDetailLevel imports are changed*/
    @Disabled("These tests were written before clientcore structure was finalized. Need to be redone to reflect the current api")
    @Test
    public void testHttpLogOptionsLogLevelImportsChanged() {
        @Language("java") String before = "import com.azure.core.http.policy.HttpLogOptions;";
        before += "\nimport com.azure.core.http.policy.HttpLogDetailLevel;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    HttpLogOptions h = new HttpLogOptions();h.setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.HttpLogOptions;";
        after += "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    HttpLogOptions h = new HttpLogOptions();h.setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before, after)
        );
    }


    /* Test to make sure HttpLogOptions and HttpLogDetailLevel type is changed*/

    @Disabled("These tests were written before clientcore structure was finalized. Need to be redone to reflect the current api")
    @Test
    public void testHttpLogOptionsLogLevelTypesChanged() {
        @Language("java") String before = "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.policy.HttpLogOptions h = new com.azure.core.http.policy.HttpLogOptions();h.setLogLevel(com.azure.core.http.policy.HttpLogDetailLevel.BODY_AND_HEADERS);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n     io.clientcore.core.http.models.HttpLogOptions h = new io.clientcore.core.http.models.HttpLogOptions();h.setLogLevel( io.clientcore.core.http.models.HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before, after)
        );
    }

}
