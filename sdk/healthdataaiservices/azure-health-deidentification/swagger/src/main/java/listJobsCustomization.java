// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
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

        ClassCustomization deidentificationClientClass = getClass("DeidentificationClient");
        deidentificationClientClass.getMethodsBySignature("listJobs", "continuationToken").get(0).setModifier(Modifier.Keyword.PRIVATE);
        deidentificationClientClass.getMethodsBySignature("listJobDocuments", "jobName", "continuationToken").get(0).setModifier(Modifier.Keyword.PRIVATE);

        ClassCustomization deidentificationAsyncClientClass = getClass("DeidentificationAsyncClient");
        deidentificationAsyncClientClass.getMethodsBySignature("listJobs", "continuationToken").get(0).setModifier(Modifier.Keyword.PRIVATE);
        deidentificationAsyncClientClass.getMethodsBySignature("listJobDocuments", "jobName", "continuationToken").get(0).setModifier(Modifier.Keyword.PRIVATE);
    }
}
