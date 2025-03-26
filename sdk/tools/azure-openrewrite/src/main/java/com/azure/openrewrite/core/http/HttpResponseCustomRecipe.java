package com.azure.openrewrite.core.http;

import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;

public class HttpResponseCustomRecipe extends Recipe {

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "HttpResponseCustomRecipe";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "The custom recipe for HttpResponse";
    }


}
