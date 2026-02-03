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
public class ClientLoggerTest extends RecipeTestBase {
    /**
     * ClientLoggerTest tests the recipe that changes
     * com.azure.core.util.logging.ClientLogger to io.clientcore.core.util.ClientLogger.
     */

    /* Test to make sure ClientLogger import is changed */
    @Disabled("These tests were written before clientcore structure was finalized. Need to be redone to reflect the current api")
    @Test
    public void testClientLoggerWithImport() {
        @Language("java") String before = "import com.azure.core.util.logging.ClientLogger;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    ClientLogger c = new ClientLogger(Testing.class);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.util.ClientLogger;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    ClientLogger c = new ClientLogger(Testing.class);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before, after)
        );
    }

    /* Test to make sure ClientLogger type is changed */
    @Disabled("These tests were written before clientcore structure was finalized. Need to be redone to reflect the current api")
    @Test
    public void testClientLoggerWithFullyQualifiedName() {
        @Language("java") String before = "public class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.logging.ClientLogger c = new com.azure.core.util.logging.ClientLogger(Testing.class);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "public class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.util.ClientLogger c = new io.clientcore.core.util.ClientLogger(Testing.class);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before, after)
        );
    }

}
