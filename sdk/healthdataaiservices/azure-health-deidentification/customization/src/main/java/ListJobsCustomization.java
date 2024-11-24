// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.*;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.JavadocBlockTag;
import org.slf4j.Logger;

/**
 * This customizes the generated Azure Deidentification client. The following changes are made by this customization:
 * <li>Mark the listJobs() overload with continuationToken parameter in DeidentificationClient as private.</li>
 * <li>Mark the listJobs() overload with continuationToken parameter in DeidentificationAsyncClient as privater.</li>
 * <li>Mark the listJobDocuments() overload with continuationToken parameter in DeidentificationClient as private.</li>
 * <li>Mark the listJobDocuments() overload with continuationToken parameter in DeidentificationAsyncClient as private.</li>
 * <li>Rename the 'name' parameter in listJobDocuments() to 'jobName'.</li>
 * <li>Rename the 'name' parameter in the JavaDoc for listJobDocuments() to 'jobName'.</li>
 */
public class ListJobsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization models = libraryCustomization.getPackage("com.azure.health.deidentification");

        ClassCustomization deidentificationClientClass = models.getClass("DeidentificationClient");
        customizeClassByMarkingContinuationTokenOverloadsPrivate(deidentificationClientClass);
        customizeClassByRenamingJobNameParameter(deidentificationClientClass);
        customizeJavadocByRenamingJobNameParameter(deidentificationClientClass);

        ClassCustomization deidentificationAsyncClientClass = models.getClass("DeidentificationAsyncClient");
        customizeClassByMarkingContinuationTokenOverloadsPrivate(deidentificationAsyncClientClass);
        customizeClassByRenamingJobNameParameter(deidentificationAsyncClientClass);
        customizeJavadocByRenamingJobNameParameter(deidentificationAsyncClientClass);
    }

    private static void customizeClassByMarkingContinuationTokenOverloadsPrivate(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("listJobs", "String").get(0).setModifiers(Modifier.Keyword.PRIVATE);
            clazz.getMethodsBySignature("listJobDocuments", "String", "String").get(0).setModifiers(Modifier.Keyword.PRIVATE);
        });
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

    private static void customizeJavadocByRenamingJobNameParameter(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            MethodDeclaration listJobDocsString = clazz.getMethodsBySignature("listJobDocuments", "String").get(0);
            listJobDocsString.getJavadoc().ifPresent(javadoc ->
            {
                // Remove name parameter
                javadoc.getBlockTags().removeIf(javadocBlockTag -> javadocBlockTag.toString().contains("The name of a job."));
                // Add jobName parameter at beginning
                JavadocBlockTag jobNameParam = new JavadocBlockTag("param", "jobName The name of a job.");
                javadoc.getBlockTags().add(0, jobNameParam);
                listJobDocsString.setJavadocComment(javadoc);
            });

            MethodDeclaration listJobDocsStringRequestOptions = clazz.getMethodsBySignature("listJobDocuments", "String", "RequestOptions").get(0);
            listJobDocsStringRequestOptions.getJavadoc().ifPresent(javadoc ->
            {
                // Remove name parameter
                javadoc.getBlockTags().removeIf(javadocBlockTag -> javadocBlockTag.toString().contains("The name of a job."));
                // Add jobName parameter at beginning
                JavadocBlockTag jobNameParam = new JavadocBlockTag("param", "jobName The name of a job.");
                javadoc.getBlockTags().add(0, jobNameParam);
                listJobDocsStringRequestOptions.setJavadocComment(javadoc);
            });
        });
    }
}
