// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * RequestOptionsTest is used to test out the recipe that converts com.azure.core.http.rest.RequestOptions
 * to io.clientcore.core.http.models.RequestOptions.
 */
@Disabled("Incorrect tests. Need to look into.")
public class RequestOptionsTest extends RecipeTestBase {


    /**
     * This test method is used to make sure that the class import for RequestOptions is updated
     */
    @Test
    void testChangeRequestImportWithImport() {
        @Language("java") String before = "import com.azure.core.http.rest.RequestOptions;\n";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    RequestOptions r = new RequestOptions();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.models.RequestOptions;\n";
        after += "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    RequestOptions r = new RequestOptions();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
            java(before, after)
        );
    }

    /**
     * This test method is used to make sure that the class type for RequestOptions is updated
     */
    @Test
    void testChangeRequestImportWithFullyQualifiedName() {
        @Language("java") String before = "public class Testing {";
        before += "\n  public Testing(){";
        before += "\n\tcom.azure.core.http.rest.RequestOptions r = new com.azure.core.http.rest.RequestOptions();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "public class Testing {";
        after += "\n  public Testing(){";
        after += "\n\tio.clientcore.core.http.models.RequestOptions r = new io.clientcore.core.http.models.RequestOptions();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
            java(before, after)
        );
    }
}
