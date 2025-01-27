// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.migration.storage.blob;


import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class AzureStorageMigrationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml","com.azure.openrewrite.migrateToVNext");
    }
}
