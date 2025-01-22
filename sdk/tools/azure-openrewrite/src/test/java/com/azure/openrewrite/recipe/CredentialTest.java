package com.azure.openrewrite.recipe;


import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import static org.openrewrite.java.Assertions.java;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

/**
 * CredentialTest is used to test out the recipe that changes the package name com.azure.core.credential
 * to io.clientcore.core.credential.
 * @author Ali Soltanian Fard Jahromi
 */
public class CredentialTest implements RewriteTest {

    /**
     * This method sets which recipe should be used for testing
     * @param spec stores settings for testing environment; e.g. which recipes to use for testing
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml",
                "com.azure.openrewrite.migrateToVNext");
    }

    /**
     * This test method is used to make sure that the package com.azure.core.credential is changed
     */
    @Test
    void testCredentialPackageNameChange() {
        @Language("java") String before = "import com.azure.core.credential.KeyCredential;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    KeyCredential kc = new KeyCredential(\"<api-key>\");";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.credential.KeyCredential;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    KeyCredential kc = new KeyCredential(\"<api-key>\");";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This test method is used to make sure that the KeyCredential type is changed
     */
    @Test
    void testKeyCredentialChangeNoImport() {
        @Language("java") String before = "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.credential.KeyCredential kc = new com.azure.core.credential.KeyCredential(\"<api-key>\");";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.credential.KeyCredential kc = new io.clientcore.core.credential.KeyCredential(\"<api-key>\");";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This test method is used to make sure that KeyCredentialPolicy type and import is changed
     */
    @Test
    void testKeyCredentialPolicyTypeAndImportChange() {
        @Language("java") String before = "import com.azure.core.http.policy.KeyCredentialPolicy;";
        before += "\npublic class Testing {";
        before += "\n  public Testing(){";
        before += "\n    com.azure.core.http.policy.KeyCredentialPolicy kc = new KeyCredentialPolicy(\"key\", null);";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.http.pipeline.KeyCredentialPolicy;";
        after += "\n\npublic class Testing {";
        after += "\n  public Testing(){";
        after += "\n    io.clientcore.core.http.pipeline.KeyCredentialPolicy kc = new KeyCredentialPolicy(\"key\", null);";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }

    /**
     * This test method is used to make sure that KeyCredentialTrait type and import is changed
     */
    @Test
    void testKeyCredentialTraitTypeAndImportChange() {
        @Language("java") String before = "import com.azure.core.client.traits.KeyCredentialTrait;";
        before += "\npublic class Testing implements KeyCredentialTrait<String>{";
        before += "\n  public Testing(){";
        before += "\n  }";
        before += "\n}";

        @Language("java") String after = "import io.clientcore.core.models.traits.KeyCredentialTrait;";
        after += "\n\npublic class Testing implements KeyCredentialTrait<String> {";
        after += "\n  public Testing(){";
        after += "\n  }";
        after += "\n}";
        rewriteRun(
                java(before,after)
        );
    }
}
