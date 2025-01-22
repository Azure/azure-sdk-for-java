package com.azure.openrewrite.recipe;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class CoreUtilsTest implements RewriteTest {
    /**
     * Test migrations from
     * com.azure.core.util.CoreUtils to com.azure.core.v2.util.CoreUtils
     * @author Jessica Lang
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.rewrite.java.core.MigrateAzureCoreSamplesToAzureCoreV2");
    }

    /* Tests that CoreUtil import is changed */
    @Test
    public void testConfigurationImportChanged() {
        @Language("java") String before = "import com.azure.core.util.CoreUtils;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    CoreUtils cu = new CoreUtils();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import com.azure.core.v2.util.CoreUtils;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    CoreUtils cu = new CoreUtils();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /* Tests that CoreUtils type is changed */
    @Test
    public void testConfigurationTypeChanged() {
        @Language("java") String before = "";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.util.CoreUtils cu = new com.azure.core.util.CoreUtils();";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "";
        after += "public class Testing {";
        after += "\n  public Testing(){";
        after += "\n    com.azure.core.v2.util.CoreUtils cu = new com.azure.core.v2.util.CoreUtils();";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

}

