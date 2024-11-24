// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;

/**
 * This customizes the generated Azure Deidentification client. The following changes are made by this customization:
 * <li>Mark the listJobs() overload with continuationToken parameter in DeidentificationClient as private.</li>
 * <li>Mark the listJobs() overload with continuationToken parameter in DeidentificationAsyncClient as privater.</li>
 * <li>Mark the listJobDocuments() overload with continuationToken parameter in DeidentificationClient as private.</li>
 * <li>Mark the listJobDocuments() overload with continuationToken parameter in DeidentificationAsyncClient as private.</li>
 * <li>Rename the 'name' parameter in listJobDocuments() to 'jobName'.</li>
 */
public class ListJobsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization models = libraryCustomization.getPackage("com.azure.health.deidentification");

        ClassCustomization deidentificationClientClass = models.getClass("DeidentificationClient");
        customizeClassByMarkingContinuationTokenOverloadsPrivate(deidentificationClientClass);
        customizeClassByRenamingJobNameParameter(deidentificationClientClass);
        //customizeJavadocByRenamingJobNameParameter(deidentificationClientClass, false);

        ClassCustomization deidentificationAsyncClientClass = models.getClass("DeidentificationAsyncClient");
        customizeClassByMarkingContinuationTokenOverloadsPrivate(deidentificationAsyncClientClass);
        customizeClassByRenamingJobNameParameter(deidentificationAsyncClientClass);
        //customizeJavadocByRenamingJobNameParameter(deidentificationAsyncClientClass, true);
    }

    private static void customizeClassByMarkingContinuationTokenOverloadsPrivate(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("listJobs", "String").get(0).setModifiers(Modifier.Keyword.PRIVATE);
            clazz.getMethodsBySignature("listJobDocuments", "String", "String").get(0).setModifiers(Modifier.Keyword.PRIVATE);
        });
    }

    private static void customizeJavadocByRenamingJobNameParameter(ClassCustomization classCustomization, Boolean customizeAsync) {
        if (customizeAsync) {
            JavadocCustomization listJobDocumentsAsyncJavadoc1 = classCustomization.getMethod("public PagedFlux<BinaryData> listJobDocuments(String jobName, RequestOptions requestOptions)").getJavadoc();
            listJobDocumentsAsyncJavadoc1.setParam("jobName", "The name of a job.");
            listJobDocumentsAsyncJavadoc1.removeParam("name");

            JavadocCustomization listJobDocumentsAsyncJavadoc2 = classCustomization.getMethod("public PagedFlux<DeidentificationDocumentDetails> listJobDocuments(String jobName)").getJavadoc();
            listJobDocumentsAsyncJavadoc2.setParam("jobName", "The name of a job.");
            listJobDocumentsAsyncJavadoc2.removeParam("name");
        }
        else {
            JavadocCustomization listJobDocumentsJavadoc1 = classCustomization.getMethod("public PagedIterable<BinaryData> listJobDocuments(String jobName, RequestOptions requestOptions)").getJavadoc();
            listJobDocumentsJavadoc1.setParam("jobName", "The name of a job.");
            listJobDocumentsJavadoc1.removeParam("name");

            JavadocCustomization listJobDocumentsJavadoc2 = classCustomization.getMethod("public PagedIterable<DeidentificationDocumentDetails> listJobDocuments(String jobName)").getJavadoc();
            listJobDocumentsJavadoc2.setParam("jobName", "The name of a job.");
            listJobDocumentsJavadoc2.removeParam("name");
        }
    }

    private static void customizeClassByRenamingJobNameParameter(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsByName("listJobDocuments").forEach(method -> {
                method.getParameterByName("name").ifPresent(parameter -> parameter.setName("jobName"));
                method.getBody().ifPresent(body -> {
                    String updatedBodySyncOnly = body.asBlockStmt().toString().replace("listJobDocuments(name, requestOptions)", "listJobDocuments(jobName, requestOptions)");
                    String updatedBody = updatedBodySyncOnly.replace("listJobDocumentsAsync(name, requestOptions)", "listJobDocumentsAsync(jobName, requestOptions)");
                    method.setBody(StaticJavaParser.parseBlock(updatedBody));
                });
            });
        });
    }
}
