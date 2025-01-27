package com.azure.openrewrite.storage;

import org.openrewrite.java.ChangeType;
import org.openrewrite.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageBlobMigrationRecipes extends Recipe {

    private static final Map<String, String> CHANGE_TYPE;

    static {
        CHANGE_TYPE = new HashMap<>();
        CHANGE_TYPE.put("com.azure.storage.blob.BlobClient", "com.azure.storage.v2.blob.BlobClient");
        CHANGE_TYPE.put("com.azure.storage.blob.BlobServiceClientBuilder", "com.azure.v2.storage.blob.BlobServiceClientBuilder");
        CHANGE_TYPE.put("com.azure.storage.blob.BlobContainerClient", "com.azure.v2.storage.blob.BlobContainerClient");
        CHANGE_TYPE.put("com.azure.storage.blob.BlobServiceClient", "com.azure.v2.storage.blob.BlobServiceClient");
        CHANGE_TYPE.put("com.azure.storage.blob.BlobClientBuilder", "com.azure.v2.storage.blob.BlobClientBuilder");
        CHANGE_TYPE.put("com.azure.storage.blob.models.BlobStorageException", "com.azure.v2.storage.blob.models.BlobStorageException");
        CHANGE_TYPE.put("com.azure.storage.blob.models.ListBlobsOptions", "com.azure.v2.storage.blob.models.ListBlobsOptions");
        CHANGE_TYPE.put("com.azure.storage.blob.specialized.BlockBlobClient", "com.azure.v2.storage.blob.specialized.BlockBlobClient");
    }

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
        List<Recipe> recipes = new ArrayList<>();
        CHANGE_TYPE.forEach((k, v) -> {
            recipes.add(new ChangeType(k, v, Boolean.FALSE));
        });
        return recipes;
    }

}
