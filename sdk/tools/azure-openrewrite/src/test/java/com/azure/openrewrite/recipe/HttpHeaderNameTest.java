// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

@Disabled("Incorrect tests. Need to look into.")
public class HttpHeaderNameTest extends RecipeTestBase {
    /**
     * HttpHeaderNameTest tests the recipe that changes
     * com.azure.core.http.HttpHeaderName to io.clientcore.core.http.models.HttpHeaderName.
     */


    /* Test to make sure HttpHeaderName type is changed */
    @Test
    public void testHeaderNameTypeChanged() {
        @Language("java") String before = "";
        before += "public class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.HttpHeaderName h = new com.azure.core.http.HttpHeaderName();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "";
        after += "public class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.http.models.HttpHeaderName h = new io.clientcore.core.http.models.HttpHeaderName();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /* Test to make sure HttpHeaderName import is changed */
    @Test
    public void testHeaderNameImportChanged() {
        @Language("java") String before = "import com.azure.core.http.HttpHeaderName;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    HttpHeaderName h = new HttpHeaderName();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.HttpHeaderName;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    HttpHeaderName h = new HttpHeaderName();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}
