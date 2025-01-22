package com.azure.openrewrite.recipe;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class HttpHeaderNameTest implements RewriteTest {
    /**
     * HttpHeaderNameTest tests the recipe that changes
     * com.azure.core.http.HttpHeaderName to io.clientcore.core.http.models.HttpHeaderName.
     * @author Ali Soltanian Fard Jahromi
     */

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.openrewrite.migrateToVNext");
    }

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
