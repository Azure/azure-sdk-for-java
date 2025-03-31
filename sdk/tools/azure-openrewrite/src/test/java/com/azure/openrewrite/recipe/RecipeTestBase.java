// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.openrewrite.recipe;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

public abstract class RecipeTestBase implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources(
            "com.azure.openrewrite.migrateToVNext",
                "com.azure.openrewrite.recipes.migrateAzureCore",
                "com.azure.openrewrite.recipes.migrateAzureStorageBlob"
                )
            .typeValidationOptions(TypeValidation.none());
    }
}
