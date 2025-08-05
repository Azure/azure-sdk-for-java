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

import java.util.Arrays;

import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * Contains customizations for Azure Key Vault's Certificates code generation.
 */
public class CertificatesCustomizations extends Customization {
    private static final String ROOT_FILE_PATH = "src/main/java/com/azure/v2/security/keyvault/certificates/";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        removeFiles(customization.getRawEditor());
        moveListResultFiles(customization);
        customizeError(customization);
        customizeCertificateKeyUsage(customization);
        customizeServiceVersion(customization);
        customizeModuleInfo(customization.getRawEditor());
    }

    private static void removeFiles(Editor editor) {
        editor.removeFile(ROOT_FILE_PATH + "CertificateClient.java");
        editor.removeFile(ROOT_FILE_PATH + "CertificateClientBuilder.java");
        editor.removeFile(ROOT_FILE_PATH + "KeyVaultServiceVersion.java");
        editor.removeFile(ROOT_FILE_PATH + "implementation/implementation/models/package-info.java");
    }

    private static void moveListResultFiles(LibraryCustomization customization) {
        moveSingleFile(customization,
            "com.azure.v2.security.keyvault.certificates.implementation.implementation.models",
            "com.azure.v2.security.keyvault.certificates.implementation.models", "DeletedCertificateListResult");
        moveSingleFile(customization,
            "com.azure.v2.security.keyvault.certificates.implementation.implementation.models",
            "com.azure.v2.security.keyvault.certificates.implementation.models", "CertificateListResult");
        moveSingleFile(customization,
            "com.azure.v2.security.keyvault.certificates.implementation.implementation.models",
            "com.azure.v2.security.keyvault.certificates.implementation.models", "CertificateIssuerListResult");

        // Update imports statements for moved classes in impl client.
        String classPath = ROOT_FILE_PATH + "implementation/CertificateClientImpl.java";
        Editor editor = customization.getRawEditor();
        String newFileContent = editor.getFileContent(classPath)
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
            .replace("import " + oldPackage + "." + className.replace("ListResult", "Item") + ";\n", "");

        // Write file with new contents.
        editor.addFile(newClassPath, newFileContent);

        // Remove old file.
        editor.removeFile(oldClassPath);
    }

    private static void customizeError(LibraryCustomization customization) {
        String implModelsDirectory = ROOT_FILE_PATH + "implementation/models/";
        String oldClassName = "KeyVaultErrorError";
        String modelsDirectory = ROOT_FILE_PATH + "models/";
        String newClassName = "CertificateOperationError";

        // Rename KeyVaultErrorError to CertificateOperationError and move it to the public models package.
        String fileContent = customization.getRawEditor().getFileContent(implModelsDirectory + oldClassName + ".java");

        customization.getRawEditor().removeFile(implModelsDirectory + oldClassName + ".java");

        fileContent = fileContent.replace(oldClassName, newClassName)
            .replace("com.azure.v2.security.keyvault.certificates.implementation.models",
                "com.azure.v2.security.keyvault.certificates.models");
        customization.getRawEditor().addFile(modelsDirectory + newClassName + ".java", fileContent);

        // The class is used in CertificateOperation, which is in the implementation package.
        // Add an import to handle the class moving.
        customization.getClass("com.azure.v2.security.keyvault.certificates.implementation.models", "CertificateOperation")
            .customizeAst(ast ->
                ast.addImport("com.azure.v2.security.keyvault.certificates.models." + newClassName)
                    .getClassByName("CertificateOperation")
                    .ifPresent(clazz -> {
                        clazz.getFieldByName("error").ifPresent(field -> field.getVariable(0).setType(newClassName));
                        clazz.getMethodsByName("getError").forEach(method -> method.setType(newClassName));
                        clazz.getMethodsByName("setError").forEach(method -> method.getParameter(0).setType(newClassName));
                        clazz.getMethodsByName("fromJson").forEach(method -> method.getBody().ifPresent(body ->
                            method.setBody(StaticJavaParser.parseBlock(body.toString().replace(oldClassName, newClassName)))));
                    }));
    }

    private static void customizeCertificateKeyUsage(LibraryCustomization customization) {
        customization.getClass("com.azure.v2.security.keyvault.certificates.models", "CertificateKeyUsage")
            .customizeAst(ast -> ast.getClassByName("CertificateKeyUsage")
                .flatMap(clazz -> clazz.getFieldByName("C_RLSIGN"))
                .ifPresent(f -> f.getVariable(0).setName("CRL_SIGN")));
    }

    private static void customizeServiceVersion(LibraryCustomization customization) {
        CompilationUnit compilationUnit = new CompilationUnit();

        compilationUnit.addOrphanComment(new LineComment(" Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment(" Licensed under the MIT License."));
        compilationUnit.addOrphanComment(new LineComment(" Code generated by Microsoft (R) TypeSpec Code Generator."));

        compilationUnit.setPackageDeclaration("com.azure.v2.security.keyvault.certificates")
            .addImport("io.clientcore.core.http.models.ServiceVersion");

        EnumDeclaration enumDeclaration = compilationUnit.addEnum("CertificateServiceVersion", Modifier.Keyword.PUBLIC)
            .addImplementedType("ServiceVersion")
            .setJavadocComment("The versions of Azure Key Vault Certificates supported by this client library.");

        for (String version : Arrays.asList("7.0", "7.1", "7.2", "7.3", "7.4", "7.5")) {
            enumDeclaration.addEnumConstant("V" + version.replace('.', '_'))
                .setJavadocComment("Service version {@code " + version + "}.")
                .addArgument(new StringLiteralExpr(version));
        }

        enumDeclaration.addField("String", "version", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

        enumDeclaration.addConstructor().addParameter("String", "version")
            .setBody(StaticJavaParser.parseBlock("{ this.version = version; }"));

        enumDeclaration.addMethod("getVersion", Modifier.Keyword.PUBLIC)
            .setType("String")
            .setJavadocComment("{@inheritDoc}")
            .addMarkerAnnotation("Override")
            .setBody(StaticJavaParser.parseBlock("{ return this.version; }"));

        enumDeclaration.addMethod("getLatest", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType("CertificateServiceVersion")
            .setJavadocComment(new Javadoc(parseText("Gets the latest service version supported by this client library."))
                .addBlockTag("return", "The latest {@link CertificateServiceVersion}."))
            .setBody(StaticJavaParser.parseBlock("{ return V7_5; }"));

        customization.getRawEditor()
            .addFile(ROOT_FILE_PATH + "CertificateServiceVersion.java",
                compilationUnit.toString());

        String fileName = ROOT_FILE_PATH + "implementation/CertificateClientImpl.java";
        String fileContent = customization.getRawEditor().getFileContent(fileName)
            .replace("KeyVaultServiceVersion", "CertificateServiceVersion");

        customization.getRawEditor().replaceFile(fileName, fileContent);
    }

    private static void customizeModuleInfo(Editor editor) {
        editor.replaceFile("src/main/java/module-info.java", String.join("\n",
            "// Copyright (c) Microsoft Corporation. All rights reserved.",
            "// Licensed under the MIT License.",
            "",
            "module com.azure.v2.security.keyvault.certificates {",
            "    requires transitive com.azure.v2.core;",
            "",
            "    exports com.azure.v2.security.keyvault.certificates;",
            "    exports com.azure.v2.security.keyvault.certificates.models;",
            "}"));
    }
}
