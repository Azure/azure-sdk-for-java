// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
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
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

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
                    .addFirst(StaticJavaParser.parseBodyDeclaration(String.join("\n", "static {",
                        "    PageListHelper.setAccessor(new PageListHelper.PageListAccessor() {",
                        "        @Override",
                        "        public String getNextMarker(PageList pageList) {",
                        "            return pageList.getNextMarker();",
                        "        }",
                        "",
                        "        @Override",
                        "        public PageList setNextMarker(PageList pageList, String marker) {",
                        "            return pageList.setNextMarker(marker);",
                        "        }",
                        "    });",
                        "}"))));
            });
        });

        ClassCustomization blobContainerEncryptionScope = models.getClass("BlobContainerEncryptionScope");
        blobContainerEncryptionScope.getMethod("isEncryptionScopeOverridePrevented")
            .setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);

        ClassCustomization blobContainerItemProperties = models.getClass("BlobContainerItemProperties");
        blobContainerItemProperties.getMethod("isEncryptionScopeOverridePrevented")
            .setReturnType("boolean", "return Boolean.TRUE.equals(%s);", true);
        blobContainerItemProperties.getMethod("setIsImmutableStorageWithVersioningEnabled")
            .rename("setImmutableStorageWithVersioningEnabled");
        blobContainerItemProperties.getMethod("setEncryptionScopeOverridePrevented")
            .replaceParameters("boolean encryptionScopeOverridePrevented");

        // Block - Generator
        ClassCustomization block = models.getClass("Block");

        block.getMethod("getSizeInt")
            .rename("getSize")
            .addAnnotation("@Deprecated")
            .setReturnType("int", "return (int) this.sizeLong; // return %s;", true)
            .getJavadoc()
            .setDeprecated("Use {@link #getSizeLong()}");

        block.getMethod("setSizeInt")
            .rename("setSize")
            .addAnnotation("@Deprecated")
            .setReturnType("Block", "return %s.setSizeLong((long) sizeInt);", true)
            .getJavadoc()
            .setDeprecated("Use {@link #setSizeLong(long)}");

        // BlobErrorCode
        // Fix typo
        String blobErrorCodeFile = "src/main/java/com/azure/storage/blob/models/BlobErrorCode.java";
        String blobErrorCodeFileContent = customization.getRawEditor().getFileContent(blobErrorCodeFile);
        blobErrorCodeFileContent = blobErrorCodeFileContent.replaceAll("SnaphotOperationRateExceeded", "SnapshotOperationRateExceeded");
        customization.getRawEditor().replaceFile(blobErrorCodeFile, blobErrorCodeFileContent);
        // deprecate
        ClassCustomization blobErrorCode = models.getClass("BlobErrorCode");
        blobErrorCode.getConstant("SNAPHOT_OPERATION_RATE_EXCEEDED")
            .addAnnotation("@Deprecated")
            .getJavadoc()
            .setDeprecated("Please use {@link BlobErrorCode#SNAPSHOT_OPERATION_RATE_EXCEEDED}");

        blobErrorCode.getConstant("INCREMENTAL_COPY_OF_ERALIER_VERSION_SNAPSHOT_NOT_ALLOWED")
            .addAnnotation("@Deprecated")
            .getJavadoc()
            .setDeprecated("Please use {@link BlobErrorCode#INCREMENTAL_COPY_OF_EARLIER_VERSION_SNAPSHOT_NOT_ALLOWED}");

        //QueryFormat
        customizeQueryFormat(implementationModels.getClass("QueryFormat"));

        //BlobSignedIdentifierWrapper
        customizeBlobSignedIdentifierWrapper(implementationModels.getClass("BlobSignedIdentifierWrapper"));

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
    }

    private static void customizeQueryFormat(ClassCustomization classCustomization) {
        String fileContent = classCustomization.getEditor().getFileContent(classCustomization.getFileName());
        fileContent = fileContent.replace("xmlWriter.nullElement(\"ParquetTextConfiguration\", this.parquetTextConfiguration);",
            "xmlWriter.writeStartElement(\"ParquetTextConfiguration\").writeEndElement();");
        fileContent = fileContent.replace("deserializedQueryFormat.parquetTextConfiguration = reader.null;",
            "deserializedQueryFormat.parquetTextConfiguration = new Object();\nxmlReader.skipElement();");
        classCustomization.getEditor().replaceFile(classCustomization.getFileName(), fileContent);
    }

    private static void customizeBlobSignedIdentifierWrapper(ClassCustomization classCustomization) {
        JavadocCustomization javadocfromXml = classCustomization.getMethod("fromXml(XmlReader xmlReader)").getJavadoc();
        javadocfromXml.setDescription("Reads an instance of BlobSignedIdentifierWrapper from the XmlReader.");
        javadocfromXml.setParam("xmlReader", "The XmlReader being read.");
        javadocfromXml.setReturn("An instance of BlobSignedIdentifierWrapper if the XmlReader was pointing to an " +
            "instance of it, or null if it was pointing to XML null.");
        javadocfromXml.addThrows("XMLStreamException", "If an error occurs while reading the BlobSignedIdentifierWrapper.");

        JavadocCustomization javadocfromXmlWithRoot = classCustomization.getMethod("fromXml(XmlReader xmlReader, String rootElementName)").getJavadoc();
        javadocfromXmlWithRoot.setDescription("Reads an instance of BlobSignedIdentifierWrapper from the XmlReader.");
        javadocfromXmlWithRoot.setParam("xmlReader", "The XmlReader being read.");
        javadocfromXmlWithRoot.setParam("rootElementName", "Optional root element name to override the default defined " +
            "by the model. Used to support cases where the model can deserialize from different root element names.");
        javadocfromXmlWithRoot.setReturn("An instance of BlobSignedIdentifierWrapper if the XmlReader was pointing to an " +
            "instance of it, or null if it was pointing to XML null.");
        javadocfromXmlWithRoot.addThrows("XMLStreamException", "If an error occurs while reading the BlobSignedIdentifierWrapper.");
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
