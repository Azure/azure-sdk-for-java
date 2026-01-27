// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

/**
 * Contains customizations for Azure AI Search code generation.
 */
public class SearchCustomizations extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        // Make generated search APIs in SearchClient and SearchAsyncClient non-public
        libraryCustomization.getClass("com.azure.search.documents", "SearchClient")
            .customizeAst(ast -> ast.getClassByName("SearchClient").ifPresent(clazz ->
                clazz.getMethodsByName("searchWithResponse").forEach(MethodDeclaration::setModifiers)));
        libraryCustomization.getClass("com.azure.search.documents", "SearchAsyncClient")
            .customizeAst(ast -> ast.getClassByName("SearchClient").ifPresent(clazz ->
                clazz.getMethodsByName("searchWithResponse").forEach(MethodDeclaration::setModifiers)));
    }
}
