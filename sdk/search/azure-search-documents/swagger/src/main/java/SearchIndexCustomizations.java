// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

public class SearchIndexCustomizations extends Customization {
    private static final String VARARG_METHOD_TEMPLATE =
        "this.%s = (%s == null) ? null : java.util.Arrays.asList(%s);\n"
            + "return this;\n";

    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.search.documents.implementation.models";
    private static final String MODELS = "com.azure.search.documents.models";

    // Classes
    private static final String SEARCH_OPTIONS = "SearchOptions";
    private static final String AUTOCOMPLETE_OPTIONS = "AutocompleteOptions";
    private static final String SUGGEST_OPTIONS = "SuggestOptions";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
        customizeImplementationModelsPackage(libraryCustomization.getPackage(IMPLEMENTATION_MODELS));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeAutocompleteOptions(packageCustomization.getClass(AUTOCOMPLETE_OPTIONS));
        customizeSuggestOptions(packageCustomization.getClass(SUGGEST_OPTIONS));
    }

    private void customizeAutocompleteOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isUseFuzzyMatching").rename("useFuzzyMatching");
        classCustomization.getMethod("setSearchFields").replaceParameters("String... searchFields")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "searchFields", "searchFields", "searchFields"));
    }

    private void customizeSuggestOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isUseFuzzyMatching").rename("useFuzzyMatching");
        classCustomization.getMethod("setOrderBy").replaceParameters("String... orderBy")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "orderBy", "orderBy", "orderBy"));
        classCustomization.getMethod("setSearchFields").replaceParameters("String... searchFields")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "searchFields", "searchFields", "searchFields"));
        classCustomization.getMethod("setSelect").replaceParameters("String... select")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "select", "select", "select"));
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
        customizeSearchOptions(packageCustomization.getClass(SEARCH_OPTIONS));
    }

    private void customizeSearchOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isIncludeTotalCount").rename("isTotalCountIncluded");

        classCustomization.getMethod("setFacets").replaceParameters("String... facets")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "facets", "facets", "facets"));

        classCustomization.getMethod("setOrderBy").replaceParameters("String... orderBy")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "orderBy", "orderBy", "orderBy"));

        classCustomization.getMethod("setSearchFields").replaceParameters("String... searchFields")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "searchFields", "searchFields", "searchFields"));

        classCustomization.getMethod("setSelect").replaceParameters("String... select")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "select", "select", "select"));

        classCustomization.getMethod("setHighlightFields").replaceParameters("String... highlightFields")
            .replaceBody(String.format(VARARG_METHOD_TEMPLATE, "highlightFields", "highlightFields", "highlightFields"));

        // Can't be done right now as setScoringParameters uses String.
//        // Scoring parameters are slightly different as code generates them as String.
//        classCustomization.getMethod("setScoringParameters").replaceParameters("ScoringParameter... scoringParameters")
//            .replaceBody(
//                "this.scoringParameters = (scoringParameters == null)"
//                    + "    ? null"
//                    + "    : java.util.Arrays.stream(scoringParameters)"
//                    + "        .map(ScoringParameter::toString)"
//                    + "        .collect(java.util.stream.Collectors.toList());"
//                    + "return this;");
//
//        classCustomization.getMethod("getScoringParameters")
//            .setReturnType("List<ScoringParameter>",
//                "this.scoringParameters.stream().map(ScoringParameter::new).collect(java.util.stream.Collectors.toList())");
    }
}
