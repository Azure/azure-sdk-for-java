package com.azure.openrewrite.storage;


import org.openrewrite.Recipe;

import java.util.Arrays;
import java.util.List;

public class StorageMigrationRecipes extends Recipe {

    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public List<Recipe> getRecipeList() {
        return Arrays.asList(
            new StorageBlobMigrationRecipes()
        );
    }
}
