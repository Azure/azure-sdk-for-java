// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * This customizes the generated Azure Deidentification client. The following changes are made by this customization:
 * <li>Mark the listJobs() overload with continuationToken parameter in DeidentificationClient as private.</li>
 * <li>Mark the listJobs() overload with continuationToken parameter in DeidentificationAsyncClient as privater.</li>
 * <li>Mark the listJobDocuments() overload with continuationToken parameter in DeidentificationClient as private.</li>
 * <li>Mark the listJobDocuments() overload with continuationToken parameter in DeidentificationAsyncClient as private.</li>
 */
public class ListJobsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization models = libraryCustomization.getPackage("com.azure.health.deidentification");

        ClassCustomization deidentificationClientClass = models.getClass("DeidentificationClient");
        customizeClassByMarkingContinuationTokenOverloadsPrivate(deidentificationClientClass);

        ClassCustomization deidentificationAsyncClientClass = models.getClass("DeidentificationAsyncClient");
        customizeClassByMarkingContinuationTokenOverloadsPrivate(deidentificationAsyncClientClass);
    }

    private static void customizeClassByMarkingContinuationTokenOverloadsPrivate(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("listJobs", "String").get(0).setModifiers(Modifier.Keyword.PRIVATE);
            clazz.getMethodsBySignature("listJobDocuments", "String", "String").get(0).setModifiers(Modifier.Keyword.PRIVATE);
        });
    }
}
