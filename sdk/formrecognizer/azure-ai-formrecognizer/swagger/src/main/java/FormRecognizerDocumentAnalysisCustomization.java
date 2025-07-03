// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for Form Recognizer Document
 * Analysis.
 */
public class FormRecognizerDocumentAnalysisCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getPackage("com.azure.ai.formrecognizer.documentanalysis.models")
            .getClass("DocumentBarcodeKind")
            .customizeAst(ast -> ast.getClassByName("DocumentBarcodeKind").ifPresent(clazz -> {
                clazz.getFieldByName("PDF417").ifPresent(field -> field.getVariable(0).setName("PDF_417"));
                clazz.getFieldByName("CODE39").ifPresent(field -> field.getVariable(0).setName("CODE_39"));
                clazz.getFieldByName("CODE128").ifPresent(field -> field.getVariable(0).setName("CODE_128"));
                clazz.getFieldByName("EAN8").ifPresent(field -> field.getVariable(0).setName("EAN_8"));
                clazz.getFieldByName("EAN13").ifPresent(field -> field.getVariable(0).setName("EAN_13"));
                clazz.getFieldByName("CODE93").ifPresent(field -> field.getVariable(0).setName("CODE_93"));
            }));
    }
}
