// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

import java.util.function.Supplier;

public class SearchServiceCustomizations extends Customization {
    private static final String INDEXES_MODEL_PACKAGE = "com.azure.search.documents.indexes.models";
    private static final String INPUT_FIELD_MAPPING_ENTRY = "InputFieldMappingEntry";

    private static final String INDEXES_IMPLEMENTATION_MODEL_PACKAGE =
        "com.azure.search.documents.indexes.implementation.models";

    public void customize(LibraryCustomization libraryCustomization) {
//        customizeIndexesModelPackage(libraryCustomization.getPackage(INDEXES_MODEL_PACKAGE));
        customizeIndexesModelsPackage(libraryCustomization.getPackage(INDEXES_MODEL_PACKAGE));
    }

    /*
     * Customizes the model types in 'com.azure.search.documents.indexes.models',
     */
    private void customizeIndexesModelsPackage(PackageCustomization packageCustomization) {
        customizeInputFieldMappingEntry(() -> packageCustomization.getClass(INPUT_FIELD_MAPPING_ENTRY));
    }

    private void customizeInputFieldMappingEntry(Supplier<ClassCustomization> classCustomizationSupplier) {
        ClassCustomization classCustomization = classCustomizationSupplier.get();
        classCustomization.getMethod("setInputs")
            .addAnnotation("JsonSetter");

        classCustomization = classCustomizationSupplier.get();
        classCustomization.addMethod("public InputFieldMappingEntry setInputs(InputFieldMappingEntry... inputs) {\n" +
            "    this.inputs = (inputs == null) ? null : Arrays.asList(inputs);\n" +
            "    return this;\n" +
            "}")
            .getJavadoc()
            .setDescription("Set the inputs property: The recursive inputs used when creating a complex type.")
            .setParam("inputs", "the inputs value to set.")
            .setReturn("the InputFieldMappingEntry object itself.");
    }

//    private void customizeIndexesModelPackage(PackageCustomization packageCustomization) {
//        customizeAnalyzeRequest(packageCustomization.getClass("AnalyzeRequest"));
//    }
//
//    private void customizeAnalyzeRequest(ClassCustomization classCustomization) {
//        classCustomization.getProperty("analyzer")
//            .rename("analyzerName");
//
//        classCustomization.getProperty("tokenizer")
//            .rename("tokenizerName");
//
//        classCustomization.addMethod("public AnalyzeRequest(String text, LexicalAnalyzerName analyzerName) {" +
//            "    this.text = text;" +
//            "    this.analyzerName = analyzerName;" +
//            "    this.tokenizerName = null;" +
//            "}")
//            .getJavadoc()
//            .setDescription("Constructs a new AnalyzeTextOptions object.")
//            .setParam("text", "The text to break into tokens.")
//            .setParam("analyzerName", "The name of the analyzer to use to break the given text.");
//
//        classCustomization.addMethod("public AnalyzeRequest(String text, LexicalTokenizerName tokenizerName) {" +
//            "    this.text = text;" +
//            "    this.tokenizerName = tokenizerName;" +
//            "    this.analyzerName = null;" +
//            "}")
//            .getJavadoc()
//            .setDescription("Constructs a new AnalyzeTextOptions object.")
//            .setParam("text", "The text to break into tokens.")
//            .setParam("tokenizerName", "The name of the tokenizer to use to break the given text.");
//
//        classCustomization = classCustomization.rename("AnalyzeTextOptions");
//    }
}
