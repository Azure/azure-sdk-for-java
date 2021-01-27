// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.search.documents.autorest.customizations;

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

public class SearchDocumentsCustomization extends Customization {
    private static final String INDEXES_MODEL_PACKAGE = "com.azure.search.documents.indexes.implementation.models";

    public void customize(LibraryCustomization libraryCustomization) {
        customizeIndexesModelPackage(libraryCustomization.getPackage(INDEXES_MODEL_PACKAGE));
    }

    private void customizeIndexesModelPackage(PackageCustomization packageCustomization) {
        customizeAnalyzeRequest(packageCustomization.getClass("AnalyzeRequest"));
    }

    private void customizeAnalyzeRequest(ClassCustomization classCustomization) {
        classCustomization.getProperty("analyzer")
            .rename("analyzerName");

        classCustomization.getProperty("tokenizer")
            .rename("tokenizerName");

        classCustomization.addMethod("public AnalyzeRequest(String text, LexicalAnalyzerName analyzerName) {" +
            "    this.text = text;" +
            "    this.analyzerName = analyzerName;" +
            "    this.tokenizerName = null;" +
            "}")
            .getJavadoc()
            .setDescription("Constructs a new AnalyzeTextOptions object.")
            .setParam("text", "The text to break into tokens.")
            .setParam("analyzerName", "The name of the analyzer to use to break the given text.");

        classCustomization.addMethod("public AnalyzeRequest(String text, LexicalTokenizerName tokenizerName) {" +
            "    this.text = text;" +
            "    this.tokenizerName = tokenizerName;" +
            "    this.analyzerName = null;" +
            "}")
            .getJavadoc()
            .setDescription("Constructs a new AnalyzeTextOptions object.")
            .setParam("text", "The text to break into tokens.")
            .setParam("tokenizerName", "The name of the tokenizer to use to break the given text.");

        classCustomization = classCustomization.rename("AnalyzeTextOptions");
    }
}
