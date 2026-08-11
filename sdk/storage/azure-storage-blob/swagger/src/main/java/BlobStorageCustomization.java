// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Customization class for Blob Storage.
 */
public class BlobStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Implementation models customizations
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.blob.implementation.models");

        // Models customizations
        PackageCustomization models = customization.getPackage("com.azure.storage.blob.models");

        models.getClass("PageList").customizeAst(ast -> {
            ast.addImport("com.azure.storage.blob.implementation.models.PageListHelper");

            ast.getClassByName("PageList").ifPresent(clazz -> {
                clazz.getMethodsByName("getNextMarker").forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE));
                clazz.getMethodsByName("setNextMarker").forEach(method -> method.setModifiers(Modifier.Keyword.PRIVATE));

                // Add Accessor to PageList
                clazz.setMembers(clazz.getMembers()
                    .addFirst(StaticJavaParser.parseBodyDeclaration("static {"
                        + "PageListHelper.setAccessor(new PageListHelper.PageListAccessor() {"
                        + "    @Override"
                        + "    public String getNextMarker(PageList pageList) {"
                        + "        return pageList.getNextMarker();"
                        + "    }"
                        + "    @Override"
                        + "    public PageList setNextMarker(PageList pageList, String marker) {"
                        + "        return pageList.setNextMarker(marker);"
                        + "    }"
                        + "}); }")));
            });
        });

        models.getClass("BlobContainerEncryptionScope").customizeAst(ast -> ast.getClassByName("BlobContainerEncryptionScope")
            .ifPresent(clazz -> clazz.getMethodsByName("isEncryptionScopeOverridePrevented").forEach(method -> {
                method.setType("boolean");
                // Wrap the existing return expression (ex, if "return foo;", "foo" is the expression) with
                // "Boolean.TRUE.equals".
                modifyReturnExpression(method, exprString -> "Boolean.TRUE.equals(" + exprString + ")");
            })));

        models.getClass("BlobContainerItemProperties").customizeAst(ast -> ast.getClassByName("BlobContainerItemProperties")
            .ifPresent(clazz -> {
                clazz.getMethodsByName("isEncryptionScopeOverridePrevented").forEach(method -> {
                    method.setType("boolean");
                    // Wrap the existing return expression (ex, if "return foo;", "foo" is the expression) with
                    // "Boolean.TRUE.equals".
                    modifyReturnExpression(method, exprString -> "Boolean.TRUE.equals(" + exprString + ")");
                });

                clazz.getMethodsByName("setIsImmutableStorageWithVersioningEnabled")
                    .forEach(method -> method.setName("setImmutableStorageWithVersioningEnabled"));

                clazz.getMethodsByName("setEncryptionScopeOverridePrevented")
                    .forEach(method -> method.getParameter(0).setType("boolean"));
            }));

        // Block - Generator
        models.getClass("Block").customizeAst(ast -> ast.getClassByName("Block").ifPresent(clazz -> {
            clazz.getMethodsByName("getSizeInt").forEach(method -> method.setName("getSize")
                .addMarkerAnnotation("Deprecated")
                .setType("int")
                .setBody(StaticJavaParser.parseBlock("{ return (int) this.sizeLong; }"))
                .getJavadoc().ifPresent(javadoc -> method.setJavadocComment(javadoc
                    .addBlockTag("deprecated", "Use {@link #getSizeLong()}"))));

            clazz.getMethodsByName("setSizeInt").forEach(method -> {
                method.setName("setSize")
                    .addMarkerAnnotation("Deprecated")
                    .getJavadoc().ifPresent(javadoc -> method.setJavadocComment(javadoc
                        .addBlockTag("deprecated", "Use {@link #setSizeLong(long)}")));

                // Update the return from "this" to "this.setSizeLong(sizeInt)"
                modifyReturnExpression(method, exprString -> exprString + ".setSizeLong(sizeInt)");
            });
        }));

        // BlobErrorCode
        // Fix typo
        String blobErrorCodeFile = "src/main/java/com/azure/storage/blob/models/BlobErrorCode.java";
        String blobErrorCodeFileContent = customization.getRawEditor().getFileContent(blobErrorCodeFile);
        blobErrorCodeFileContent = blobErrorCodeFileContent.replace("SnaphotOperationRateExceeded", "SnapshotOperationRateExceeded");
        customization.getRawEditor().replaceFile(blobErrorCodeFile, blobErrorCodeFileContent);
        // deprecate
        models.getClass("BlobErrorCode").customizeAst(ast -> ast.getClassByName("BlobErrorCode").ifPresent(clazz -> {
            clazz.getFieldByName("SNAPHOT_OPERATION_RATE_EXCEEDED").ifPresent(field -> field.addMarkerAnnotation("Deprecated")
                .getJavadoc().ifPresent(javadoc -> field.setJavadocComment(javadoc
                    .addBlockTag("deprecated", "Please use {@link BlobErrorCode#SNAPSHOT_OPERATION_RATE_EXCEEDED}"))));

            clazz.getFieldByName("INCREMENTAL_COPY_OF_ERALIER_VERSION_SNAPSHOT_NOT_ALLOWED").ifPresent(field ->
                field.addMarkerAnnotation("Deprecated").getJavadoc().ifPresent(javadoc -> field.setJavadocComment(
                    javadoc.addBlockTag("deprecated",
                        "Please use {@link BlobErrorCode#INCREMENTAL_COPY_OF_EARLIER_SNAPSHOT_NOT_ALLOWED}"))));

            clazz.getFieldByName("INCREMENTAL_COPY_OF_EARLIER_VERSION_SNAPSHOT_NOT_ALLOWED").ifPresent(field ->
                field.addMarkerAnnotation("Deprecated").getJavadoc().ifPresent(javadoc -> field.setJavadocComment(
                    javadoc.addBlockTag("deprecated",
                        "Please use {@link BlobErrorCode#INCREMENTAL_COPY_OF_EARLIER_SNAPSHOT_NOT_ALLOWED}"))));
        }));

        //QueryFormat
        customizeQueryFormat(implementationModels.getClass("QueryFormat"));

        updateImplToMapInternalException(customization.getPackage("com.azure.storage.blob.implementation"));

        implementationModels.getClass("QueryRequest").customizeAst(ast -> ast.getClassByName("QueryRequest").ifPresent(clazz -> {
            clazz.getFieldByName("queryType").ifPresent(field -> field.removeModifier(Modifier.Keyword.FINAL));
            clazz.addMethod("setQueryType", Modifier.Keyword.PUBLIC)
                .setType("QueryRequest")
                .addParameter("String", "queryType")
                .setBody(StaticJavaParser.parseBlock("{ this.queryType = queryType; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Set the queryType property: Required. The type of the provided query expression."))
                    .addBlockTag("param", "queryType", "the queryType value to set.")
                    .addBlockTag("return", "the QueryRequest object itself."));
        }));

        implementationModels.getClass("BlobSignedIdentifierWrapper").customizeAst(ast -> ast.getClassByName("BlobSignedIdentifierWrapper")
            .ifPresent(clazz -> {
                Javadoc baseJavadoc = new Javadoc(JavadocDescription.parseText("Reads an instance of BlobSignedIdentifierWrapper from the XmlReader."))
                    .addBlockTag("param", "xmlReader", "The XmlReader being read.")
                    .addBlockTag("return", "An instance of BlobSignedIdentifierWrapper if the XmlReader was pointing "
                        + "to an instance of it, or null if it was pointing to XML null.")
                    .addBlockTag("throws", "XMLStreamException", "If an error occurs while reading the BlobSignedIdentifierWrapper.");

                clazz.getMethodsBySignature("fromXml", "XmlReader")
                    .forEach(method -> method.setJavadocComment(baseJavadoc));

                clazz.getMethodsBySignature("fromXml", "XmlReader", "String").forEach(method -> {
                    baseJavadoc.getBlockTags().add(1, JavadocBlockTag.createParamBlockTag("rootElementName",
                        "Optional root element name to override the default defined by the model. Used to support "
                            + "cases where the model can deserialize from different root element names."));
                    method.setJavadocComment(baseJavadoc);
                });
            }));
    }

    private static void modifyReturnExpression(MethodDeclaration method, Function<String, String> modifier) {
        method.getBody().flatMap(body -> body.getStatements().stream().filter(Statement::isReturnStmt)
                .map(Statement::asReturnStmt).findFirst())
            .ifPresent(statement -> {
                String replace = modifier.apply(statement.getExpression().get().toString());
                statement.setExpression(StaticJavaParser.parseExpression(replace));
            });
    }

    private static void customizeQueryFormat(ClassCustomization classCustomization) {
        String fileContent = classCustomization.getEditor().getFileContent(classCustomization.getFileName());
        fileContent = fileContent.replace("xmlWriter.nullElement(\"ParquetTextConfiguration\", this.parquetTextConfiguration);",
            "xmlWriter.writeStartElement(\"ParquetTextConfiguration\").writeEndElement();");
        fileContent = fileContent.replace("deserializedQueryFormat.parquetTextConfiguration = reader.null;",
            "deserializedQueryFormat.parquetTextConfiguration = new Object();\nxmlReader.skipElement();");
        classCustomization.getEditor().replaceFile(classCustomization.getFileName(), fileContent);
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to
     * {@code .onErrorMap(BlobStorageExceptionInternal.class, ModelHelper::mapToBlobStorageException)} to handle
     * mapping BlobStorageExceptionInternal to BlobStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * BlobStorageExceptionInternal and rethrows {@code ModelHelper.mapToBlobStorageException(e)}. Or, for void methods
     * wrap the last statement.
     *
     * @param implPackage The implementation package.
     */
    private static void updateImplToMapInternalException(PackageCustomization implPackage) {
        List<String> implsToUpdate = Arrays.asList("AppendBlobsImpl", "BlobsImpl", "BlockBlobsImpl", "ContainersImpl",
            "PageBlobsImpl", "ServicesImpl");
        for (String implToUpdate : implsToUpdate) {
            implPackage.getClass(implToUpdate).customizeAst(ast -> {
                ast.addImport("com.azure.storage.blob.implementation.util.ModelHelper");
                ast.addImport("com.azure.storage.blob.models.BlobStorageException");
                ast.addImport("com.azure.storage.blob.implementation.models.BlobStorageExceptionInternal");
                ast.getClassByName(implToUpdate).ifPresent(clazz -> {
                    clazz.getFields();

                    clazz.getMethods().forEach(methodDeclaration -> {
                        Type returnType = methodDeclaration.getType();
                        // The way code generation works we only need to update the methods that have a class return type.
                        // As non-class return types, such as "void", call into the Response<Void> methods.
                        if (!returnType.isClassOrInterfaceType()) {
                            return;
                        }

                        ClassOrInterfaceType returnTypeClass = returnType.asClassOrInterfaceType();
                        String returnTypeName = returnTypeClass.getNameAsString();
                        if (returnTypeName.equals("PagedFlux") || returnTypeName.equals("PagedIterable")
                            || returnTypeName.equals("PollerFlux") || returnTypeName.equals("SyncPoller")) {
                            return;
                        }

                        if (returnTypeName.equals("Mono") || returnTypeName.equals("Flux")) {
                            addErrorMappingToAsyncMethod(methodDeclaration);
                        } else {
                            addErrorMappingToSyncMethod(methodDeclaration);
                        }
                    });
                });
            });
        }
    }

    private static void addErrorMappingToAsyncMethod(MethodDeclaration method) {
        BlockStmt body = method.getBody().get();

        // Bit of hack to insert the 'onErrorMap' in the right location.
        // Unfortunately, 'onErrorMap' returns <T> which for some calls breaks typing, such as Void -> Object or
        // PagedResponse -> PagedResponseBase. So, 'onErrorMap' needs to be inserted after the first method call.
        // To do this, we track the first found '(' and the associated closing ')' to insert 'onErrorMap' after the ')'.
        // So, 'service.methodCall(parameters).map()' becomes 'service.methodCall(parameters).onErrorMap().map()'.
        String originalReturnStatement = body.getStatement(body.getStatements().size() - 1).asReturnStmt()
            .getExpression().get().toString();
        int insertionPoint = findAsyncOnErrorMapInsertionPoint(originalReturnStatement);
        String newReturnStatement = "return " + originalReturnStatement.substring(0, insertionPoint)
            + ".onErrorMap(BlobStorageExceptionInternal.class, ModelHelper::mapToBlobStorageException)"
            + originalReturnStatement.substring(insertionPoint) + ";";
        try {
            Statement newReturn = StaticJavaParser.parseStatement(newReturnStatement);
            body.getStatements().set(body.getStatements().size() - 1, newReturn);
        } catch (ParseProblemException ex) {
            throw new RuntimeException("Failed to parse: " + newReturnStatement, ex);
        }
    }

    private static int findAsyncOnErrorMapInsertionPoint(String returnStatement) {
        int openParenthesis = 0;
        int closeParenthesis = 0;
        for (int i = 0; i < returnStatement.length(); i++) {
            char c = returnStatement.charAt(i);
            if (c == '(') {
                openParenthesis++;
            } else if (c == ')') {
                closeParenthesis++;
                if (openParenthesis == closeParenthesis) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    private static void addErrorMappingToSyncMethod(MethodDeclaration method) {
        // Turn the entire method into a BlockStmt that will be used as the try block.
        BlockStmt tryBlock = method.getBody().get();
        BlockStmt catchBlock = new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement(
            "throw ModelHelper.mapToBlobStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("BlobStorageExceptionInternal")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        method.setBody(new BlockStmt(new NodeList<>(tryCatchMap)));
    }
}
