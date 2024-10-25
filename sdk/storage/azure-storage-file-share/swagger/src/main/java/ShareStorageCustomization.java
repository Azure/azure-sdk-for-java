// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");

        ClassCustomization shareTokenIntent = models.getClass("ShareTokenIntent");
        shareTokenIntent.getJavadoc().setDescription("The request intent specifies requests that are intended for " +
            "backup/admin type operations, meaning that all file/directory ACLs are bypassed and full permissions are " +
            "granted. User must also have required RBAC permission.");

        models.getClass("AccessRight").rename("ShareFileHandleAccessRights");

        customizeShareFileRangeList(models.getClass("ShareFileRangeList"));
        customizeFilesAndDirectoriesListSegment(
            customization.getPackage("com.azure.storage.file.share.implementation.models")
                .getClass("FilesAndDirectoriesListSegment"));

        updateImplToMapInternalException(customization.getPackage("com.azure.storage.file.share.implementation"));
    }

    // ShareFileRangeList has special serialization behaviors which Swagger cannot define correctly. It has a single
    // outer XML element "<Ranges>" which can contain a list of both "<Range>" and "<ClearRange>" elements,
    // intermixed. Swagger isn't capable of defining this, so it ends up thinking there are two separate lists wrapped
    // with "<Ranges>" and "<ClearRanges>". We need to manually correct this.
    private static void customizeShareFileRangeList(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("toXml", "XmlWriter", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Ranges\" : rootElementName;",
                    "xmlWriter.writeStartElement(rootElementName);",
                    "if (this.ranges != null) {",
                    "    for (FileRange element : this.ranges) {",
                    "        xmlWriter.writeXml(element, \"Range\");",
                    "    }",
                    "}",
                    "if (this.clearRanges != null) {",
                    "    for (ClearRange element : this.clearRanges) {",
                    "        xmlWriter.writeXml(element, \"ClearRange\");",
                    "    }",
                    "}",
                    "return xmlWriter.writeEndElement();",
                    "}"
                )));

            clazz.getMethodsBySignature("fromXml", "XmlReader", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Ranges\" : rootElementName;",
                    "return xmlReader.readObject(finalRootElementName, reader -> {",
                    "    ShareFileRangeList deserializedShareFileRangeList = new ShareFileRangeList();",
                    "    while (reader.nextElement() != XmlToken.END_ELEMENT) {",
                    "        QName elementName = reader.getElementName();",
                    "",
                    "        if (\"Range\".equals(elementName.getLocalPart())) {",
                    "            if (deserializedShareFileRangeList.ranges == null) {",
                    "                deserializedShareFileRangeList.ranges = new ArrayList<>();",
                    "            }",
                    "            deserializedShareFileRangeList.ranges.add(FileRange.fromXml(reader, \"Range\"));",
                    "        } else if (\"ClearRange\".equals(elementName.getLocalPart())) {",
                    "            if (deserializedShareFileRangeList.clearRanges == null) {",
                    "                deserializedShareFileRangeList.clearRanges = new ArrayList<>();",
                    "            }",
                    "            deserializedShareFileRangeList.clearRanges.add(ClearRange.fromXml(reader, \"ClearRange\"));",
                    "        } else {",
                    "            reader.skipElement();",
                    "        }",
                    "    }",
                    "",
                    "    return deserializedShareFileRangeList;",
                    "});",
                    "}"
                )));
        });
    }

    // FilesAndDirectoriesListSegment has special serialization behaviors which Swagger cannot define correctly. It has
    // a single outer XML element "<Entries>" which can contain a list of both "<Directory>" and "<File>" elements,
    // intermixed. Swagger isn't capable of defining this, so it ends up thinking there are two separate lists wrapped
    // with "<Directories>" and "<Files>". We need to manually correct this.
    private static void customizeFilesAndDirectoriesListSegment(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("toXml", "XmlWriter", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Entries\" : rootElementName;",
                    "xmlWriter.writeStartElement(rootElementName);",
                    "if (this.directoryItems != null) {",
                    "    for (DirectoryItem element : this.directoryItems) {",
                    "        xmlWriter.writeXml(element, \"Directory\");",
                    "    }",
                    "}",
                    "if (this.fileItems != null) {",
                    "    for (FileItem element : this.fileItems) {",
                    "        xmlWriter.writeXml(element, \"File\");",
                    "    }",
                    "}",
                    "return xmlWriter.writeEndElement();",
                    "}"
                )));

            clazz.getMethodsBySignature("fromXml", "XmlReader", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Entries\" : rootElementName;",
                    "return xmlReader.readObject(finalRootElementName, reader -> {",
                    "    FilesAndDirectoriesListSegment deserializedFilesAndDirectoriesListSegment",
                    "        = new FilesAndDirectoriesListSegment();",
                    "    while (reader.nextElement() != XmlToken.END_ELEMENT) {",
                    "        QName elementName = reader.getElementName();",
                    "",
                    "        if (\"Directory\".equals(elementName.getLocalPart())) {",
                    "            if (deserializedFilesAndDirectoriesListSegment.directoryItems == null) {",
                    "                deserializedFilesAndDirectoriesListSegment.directoryItems = new ArrayList<>();",
                    "            }",
                    "            deserializedFilesAndDirectoriesListSegment.directoryItems",
                    "                .add(DirectoryItem.fromXml(reader, \"Directory\"));",
                    "        } else if (\"File\".equals(elementName.getLocalPart())) {",
                    "            if (deserializedFilesAndDirectoriesListSegment.fileItems == null) {",
                    "                deserializedFilesAndDirectoriesListSegment.fileItems = new ArrayList<>();",
                    "            }",
                    "            deserializedFilesAndDirectoriesListSegment.fileItems.add(FileItem.fromXml(reader, \"File\"));",
                    "        } else {",
                    "            reader.skipElement();",
                    "        }",
                    "    }",
                    "",
                    "    return deserializedFilesAndDirectoriesListSegment;",
                    "});",
                    "}"
                )));
        });
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to
     * {@code .onErrorMap(ShareStorageExceptionInternal.class, ModelHelper::mapToShareStorageException)} to handle
     * mapping ShareStorageExceptionInternal to ShareStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * ShareStorageExceptionInternal and rethrows {@code ModelHelper.mapToShareStorageException(e)}. Or, for void
     * methods wrap the last statement.
     *
     * @param implPackage The implementation package.
     */
    private static void updateImplToMapInternalException(PackageCustomization implPackage) {
        List<String> implsToUpdate = Arrays.asList("DirectoriesImpl", "FilesImpl", "ServicesImpl", "SharesImpl");
        for (String implToUpdate : implsToUpdate) {
            implPackage.getClass(implToUpdate).customizeAst(ast -> {
                ast.addImport("com.azure.storage.file.share.implementation.util.ModelHelper");
                ast.addImport("com.azure.storage.file.share.models.ShareStorageException");
                ast.addImport("com.azure.storage.file.share.implementation.models.ShareStorageExceptionInternal");
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
            + ".onErrorMap(ShareStorageExceptionInternal.class, ModelHelper::mapToShareStorageException)"
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
        BlockStmt body = method.getBody().get();

        // Turn the last statement into a BlockStmt that will be used as the try block.
        BlockStmt tryBlock = new BlockStmt(new NodeList<>(body.getStatement(body.getStatements().size() - 1)));
        BlockStmt catchBlock = new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement(
            "throw ModelHelper.mapToShareStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("ShareStorageExceptionInternal")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        body.getStatements().set(body.getStatements().size() - 1, tryCatchMap);
    }
}
