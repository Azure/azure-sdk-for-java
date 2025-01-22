// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.migration.core.v2;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class AzureCoreV2MigrationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml","com.azure.opwnrewrite.migrateToVNext");
    }

}
