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
public class UtilConfigurationTest extends RecipeTestBase {

    /* Testing ChangeType recipe for changing import */
    @Test
    public void testConfigurationWithImport() {
        @Language("java") String before = "import com.azure.core.util.Configuration;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    Configuration c = new Configuration();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.configuration.Configuration;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    Configuration c = new Configuration();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before, after)
        );
    }

    /* Testing ChangeType recipe for changing type */
    @Test
    public void testConfigurationWithFullyQualifiedName() {
        @Language("java") String before = "public class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.Configuration c = new com.azure.core.util.Configuration();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "public class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.util.configuration.Configuration c = new io.clientcore.core.util.configuration.Configuration();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before, after)
        );
    }

}
