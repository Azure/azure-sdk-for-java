// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * Contains customizations for Azure Key Vault Administration code generation.
 */
public class AdministrationCustomizations extends Customization {
    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/v2/security/keyvault/administration/";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeFiles(customization.getRawEditor());
        moveListResultFiles(customization);
        customizeKeyVaultRoleScope(customization);
        customizeServiceVersion(customization);
        customizeImplClients(customization);
        customizeModuleInfo(customization.getRawEditor());
    }

    private static void removeFiles(Editor editor) {
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultAdministrationClient.java");
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultAdministrationClientBuilder.java");
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultServiceVersion.java");
        editor.removeFile(ROOT_FILE_PATH + "RoleAssignmentsClient.java");
        editor.removeFile(ROOT_FILE_PATH + "RoleDefinitionsClient.java");
        editor.removeFile(ROOT_FILE_PATH + "implementation/implementation/models/package-info.java");
    }

    private static void moveListResultFiles(LibraryCustomization customization) {
        moveSingleFile(customization,
            "com.azure.v2.security.keyvault.administration.implementation.implementation.models",
            "com.azure.v2.security.keyvault.administration.implementation.models", "RoleAssignmentListResult");
        moveSingleFile(customization,
            "com.azure.v2.security.keyvault.administration.implementation.implementation.models",
            "com.azure.v2.security.keyvault.administration.implementation.models", "RoleDefinitionListResult");

        // Update imports statements for moved classes in impl client.
        String classPath = ROOT_FILE_PATH + "implementation/KeyVaultAdministrationClientImpl.java";
        Editor editor = customization.getRawEditor();
        String newFileContent = editor.getFileContent(classPath)
            .replace("implementation.implementation", "implementation");

        editor.replaceFile(classPath, newFileContent);

        // Update imports statements for moved classes in impl client.
        classPath = ROOT_FILE_PATH + "implementation/RoleAssignmentsImpl.java";
        newFileContent = editor.getFileContent(classPath)
            .replace("implementation.implementation", "implementation");

        editor.replaceFile(classPath, newFileContent);

        // Update imports statements for moved classes in impl client.
        classPath = ROOT_FILE_PATH + "implementation/RoleDefinitionsImpl.java";
        newFileContent = editor.getFileContent(classPath)
            .replace("implementation.implementation", "implementation");

        editor.replaceFile(classPath, newFileContent);
    }

    private static void moveSingleFile(LibraryCustomization customization, String oldPackage, String newPackage,
        String className) {

        Editor editor = customization.getRawEditor();
        String oldClassPath = "src/main/java/" + oldPackage.replace('.', '/') + "/" + className + ".java";
        String newClassPath = "src/main/java/" + newPackage.replace('.', '/') + "/" + className + ".java";

        // Update the package declaration.
        customization.getPackage(oldPackage)
            .getClass(className)
            .customizeAst(ast -> ast.getPackageDeclaration()
                .ifPresent(packageDeclaration -> packageDeclaration.setName(newPackage)));

        // Remove unnecessary import statement.
        String newFileContent = editor.getFileContent(oldClassPath)
            .replace("import " + oldPackage + "." + className.replace("ListResult", "") + ";\n", "");

        // Write file with new contents.
        editor.addFile(newClassPath, newFileContent);

        // Remove old file.
        editor.removeFile(oldClassPath);
    }

    private static void customizeImplClients(LibraryCustomization customization) {
        customization
            .getClass("com.azure.v2.security.keyvault.administration.implementation", "KeyVaultAdministrationClientImpl")
            .customizeAst(ast ->
                ast.getClassByName("KeyVaultAdministrationClientImpl")
                    .ifPresent(clazz -> {
                        clazz.addMethod("fullBackupOperationWithResponse", Modifier.Keyword.PUBLIC)
                            .setType("Response<FullBackupOperation>")
                            .addParameter("SASTokenParameter", "azureStorageBlobContainerUri")
                            .addParameter("RequestContext", "requestContext")
                            .setJavadocComment(new Javadoc(
                                parseText("Creates a full backup using a user-provided SAS token to an Azure blob storage container."))
                                    .addBlockTag("param", "azureStorageBlobContainerUri", "Azure blob shared access signature token pointing "
                                        + "to a valid Azure blob container where full backup needs to be stored. This token needs to be "
                                        + "valid for at least next 24 hours from the time of making this call.")
                                    .addBlockTag("param", "requestContext", "The context to configure the HTTP request before HTTP client sends it.")
                                    .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                                    .addBlockTag("throws", "HttpResponseException", "thrown if the service returns an error.")
                                    .addBlockTag("throws", "RuntimeException", "all other wrapped checked exceptions if the request fails to be sent.")
                                    .addBlockTag("return", "full backup operation."))
                            .setBody(StaticJavaParser.parseBlock("{ final String contentType = \"application/json\";"
                                + "final String accept = \"application/json\";"
                                + "return service.fullBackup(this.getVaultBaseUrl(), this.getServiceVersion().getVersion(), contentType, accept, "
                                + "azureStorageBlobContainerUri, requestContext); }"));

                        clazz.addMethod("fullRestoreOperationWithResponse", Modifier.Keyword.PUBLIC)
                            .setType("Response<RestoreOperation>")
                            .addParameter("RestoreOperationParameters", "restoreBlobDetails")
                            .addParameter("RequestContext", "requestContext")
                            .setJavadocComment(new Javadoc(
                                parseText("Restores all key materials using the SAS token pointing to a previously stored Azure Blob storage backup folder."))
                                    .addBlockTag("param", "restoreBlobDetails", "The Azure blob SAS token pointing to a folder where the previous successful "
                                        + "full backup was stored.")
                                    .addBlockTag("param", "requestContext", "The context to configure the HTTP request before HTTP client sends it.")
                                    .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                                    .addBlockTag("throws", "HttpResponseException", "thrown if the service returns an error.")
                                    .addBlockTag("throws", "RuntimeException", "all other wrapped checked exceptions if the request fails to be sent.")
                                    .addBlockTag("return", "restore operation."))
                            .setBody(StaticJavaParser.parseBlock("{ final String contentType = \"application/json\";"
                                + "final String accept = \"application/json\";"
                                + "return service.fullRestoreOperation(this.getVaultBaseUrl(), this.getServiceVersion().getVersion(), contentType, accept, "
                                + "restoreBlobDetails, requestContext); }"));

                        clazz.addMethod("selectiveKeyRestoreOperationWithResponse", Modifier.Keyword.PUBLIC)
                            .setType("Response<SelectiveKeyRestoreOperation>")
                            .addParameter("String", "keyName")
                            .addParameter("SelectiveKeyRestoreOperationParameters", "restoreBlobDetails")
                            .addParameter("RequestContext", "requestContext")
                            .setJavadocComment(new Javadoc(
                                parseText("Restores all key versions of a given key using user supplied SAS token pointing to a previously stored Azure Blob storage backup folder."))
                                    .addBlockTag("param", "keyName", "The name of the key to be restored from the user supplied backup.")
                                    .addBlockTag("param", "restoreBlobDetails", "The Azure blob SAS token pointing to a folder where the previous successful "
                                        + "full backup was stored.")
                                    .addBlockTag("param", "requestContext", "The context to configure the HTTP request before HTTP client sends it.")
                                    .addBlockTag("throws", "IllegalArgumentException", "thrown if parameters fail the validation.")
                                    .addBlockTag("throws", "HttpResponseException", "thrown if the service returns an error.")
                                    .addBlockTag("throws", "RuntimeException", "all other wrapped checked exceptions if the request fails to be sent.")
                                    .addBlockTag("return", "selective Key Restore operation."))
                            .setBody(StaticJavaParser.parseBlock("{ final String contentType = \"application/json\";"
                                + "final String accept = \"application/json\";"
                                + "return service.selectiveKeyRestoreOperation(this.getVaultBaseUrl(), this.getServiceVersion().getVersion(), keyName, "
                                + "contentType, accept, restoreBlobDetails, requestContext); }"));
                    }));
    }

    private static void customizeKeyVaultRoleScope(LibraryCustomization customization) {
        customization
            .getClass("com.azure.v2.security.keyvault.administration.models", "KeyVaultRoleScope")
            .customizeAst(ast ->
                ast.addImport(IllegalArgumentException.class)
                    .addImport(URL.class)
                    .addImport(MalformedURLException.class)
                    .getClassByName("KeyVaultRoleScope")
                    .ifPresent(clazz -> {
                        clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                            .setType("KeyVaultRoleScope")
                            .addParameter("String", "url")
                            .setJavadocComment(new Javadoc(
                                parseText("Creates or finds a {@link KeyVaultRoleScope} from its string representation."))
                                    .addBlockTag("param", "url", "A string representing a URL containing the name of the scope to look for.")
                                    .addBlockTag("return", "The corresponding {@link KeyVaultRoleScope}.")
                                    .addBlockTag("throws", "IllegalArgumentException", "If the given {@code url} is malformed."))
                            .setBody(StaticJavaParser.parseBlock("{ try { return fromValue(new URL(url).getPath()); }"
                                + "catch (MalformedURLException e) { throw new IllegalArgumentException(e); } }"));

                        clazz.addMethod("fromUrl", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                            .setType("KeyVaultRoleScope")
                            .addParameter("URL", "url")
                            .setJavadocComment(new Javadoc(
                                parseText("Creates or finds a {@link KeyVaultRoleScope} from its string representation."))
                                    .addBlockTag("param", "url", "A URL containing the name of the scope to look for.")
                                    .addBlockTag("return", "The corresponding {@link KeyVaultRoleScope}."))
                            .setBody(StaticJavaParser.parseBlock("{ return fromValue(url.getPath()); }"));
                    }));
    }

    private static void customizeServiceVersion(LibraryCustomization customization) {
        CompilationUnit compilationUnit = new CompilationUnit();

        compilationUnit.addOrphanComment(new LineComment(" Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment(" Licensed under the MIT License."));
        compilationUnit.addOrphanComment(new LineComment(" Code generated by Microsoft (R) TypeSpec Code Generator."));

        compilationUnit.setPackageDeclaration("com.azure.v2.security.keyvault.administration")
            .addImport("io.clientcore.core.http.models.ServiceVersion");

        EnumDeclaration enumDeclaration = compilationUnit.addEnum("KeyVaultAdministrationServiceVersion", Modifier.Keyword.PUBLIC)
            .addImplementedType("ServiceVersion")
            .setJavadocComment("The versions of Azure Key Vault supported by this client library.");

        for (String version : Arrays.asList("7.2", "7.3", "7.4", "7.5")) {
            enumDeclaration.addEnumConstant("V" + version.replace('.', '_'))
                .setJavadocComment("Service version {@code " + version + "}.")
                .addArgument(new StringLiteralExpr(version));
        }

        enumDeclaration.addField("String", "version", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

        enumDeclaration.addConstructor()
            .addParameter("String", "version")
            .setBody(StaticJavaParser.parseBlock("{ this.version = version; }"));

        enumDeclaration.addMethod("getVersion", Modifier.Keyword.PUBLIC)
            .setType("String")
            .setJavadocComment("{@inheritDoc}")
            .addMarkerAnnotation("Override")
            .setBody(StaticJavaParser.parseBlock("{ return this.version; }"));

        enumDeclaration.addMethod("getLatest", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType("KeyVaultAdministrationServiceVersion")
            .setJavadocComment(new Javadoc(
                parseText("Gets the latest service version supported by this client library."))
                    .addBlockTag("return", "The latest {@link KeyVaultAdministrationServiceVersion}."))
            .setBody(StaticJavaParser.parseBlock("{ return V7_5; }"));

        customization.getRawEditor()
            .addFile(ROOT_FILE_PATH + "KeyVaultAdministrationServiceVersion.java",
                compilationUnit.toString());

        for (String impl : Arrays.asList("KeyVaultAdministrationClientImpl", "RoleAssignmentsImpl", "RoleDefinitionsImpl")) {
            String fileName = ROOT_FILE_PATH + "implementation/" + impl + ".java";
            String fileContent = customization.getRawEditor().getFileContent(fileName)
                .replace("KeyVaultServiceVersion", "KeyVaultAdministrationServiceVersion");

            customization.getRawEditor().replaceFile(fileName, fileContent);
        }
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java", String.join("\n",
            "// Copyright (c) Microsoft Corporation. All rights reserved.",
            "// Licensed under the MIT License.",
            "",
            "module com.azure.v2.security.keyvault.administration {",
            "    requires transitive com.azure.v2.core;",
            "",
            "    exports com.azure.v2.security.keyvault.administration;",
            "    exports com.azure.v2.security.keyvault.administration.models;",
            "}"));
    }
}
