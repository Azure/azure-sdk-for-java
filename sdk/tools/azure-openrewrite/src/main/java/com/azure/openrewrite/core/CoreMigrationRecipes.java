package com.azure.openrewrite.core;

import com.azure.openrewrite.RemoveFixedDelayRecipe;
import com.azure.openrewrite.TypeReferenceRecipe;
import org.openrewrite.Recipe;
import org.openrewrite.java.ChangeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreMigrationRecipes extends Recipe {

    private static final Map<String, String> CHANGE_TYPE;

    static {
        CHANGE_TYPE = new HashMap<>();
        CHANGE_TYPE.put("com.azure.core.http.HttpHeaderName", "io.clientcore.core.http.models.HttpHeaderName");
        CHANGE_TYPE.put("com.azure.core.util.logging.ClientLogger", "io.clientcore.core.instrumentation.logging.ClientLogger");
        CHANGE_TYPE.put("com.azure.core.util.CoreUtils","com.azure.core.v2.util.CoreUtils");
        CHANGE_TYPE.put("com.azure.core.http.policy.KeyCredentialPolicy", "io.clientcore.core.http.pipeline.KeyCredentialPolicy");
        CHANGE_TYPE.put("com.azure.core.credential.KeyCredential", "io.clientcore.core.credential.KeyCredential");
        CHANGE_TYPE.put("com.azure.core.client.traits.KeyCredentialTrait", "io.clientcore.core.models.traits.KeyCredentialTrait");
        CHANGE_TYPE.put("com.azure.core.http.HttpClient", "io.clientcore.core.http.client.HttpClient");
        CHANGE_TYPE.put("com.azure.core.http.HttpPipeline", "io.clientcore.core.http.pipeline.HttpPipeline");
        CHANGE_TYPE.put("com.azure.core.http.HttpPipelineBuilder", "io.clientcore.core.http.pipeline.HttpPipelineBuilder");
        CHANGE_TYPE.put("com.azure.core.http.policy.HttpPipelinePolicy", "io.clientcore.core.http.pipeline.HttpPipelinePolicy");
        CHANGE_TYPE.put("com.azure.core.exception.ClientAuthenticationException", "com.azure.core.v2.exception.ClientAuthenticationException");
        CHANGE_TYPE.put("com.azure.core.exception.ResourceModifiedException", "com.azure.core.v2.exception.ResourceModifiedException");
        CHANGE_TYPE.put("com.azure.core.exception.ResourceNotFoundException", "com.azure.core.v2.exception.ResourceNotFoundException");
        CHANGE_TYPE.put("com.azure.core.exception.HttpResponseException", "io.clientcore.core.http.exception.HttpResponseException");
        CHANGE_TYPE.put("com.azure.core.util.Configuration", "io.clientcore.core.util.configuration.Configuration");
        CHANGE_TYPE.put("com.azure.core.credential.AzureSasCredential", "io.clientcore.core.credential.KeyCredential");
        CHANGE_TYPE.put("com.azure.core.http.policy.RetryPolicy", "io.clientcore.core.http.pipeline.HttpRetryPolicy");

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
        recipes.add(new ResponseRecipe());
        recipes.add(new ContextRecipe());
        recipes.add(new RetryOptionsConstructorRecipe());
        recipes.add(new TypeReferenceRecipe());
        recipes.add(new HttpLogOptionsRecipe()); //TODO: This recipe is bugged, Either adjust the recipe or remove it (jairmyree)
        recipes.add(new RemoveFixedDelayRecipe());
        return recipes;
    }


}
