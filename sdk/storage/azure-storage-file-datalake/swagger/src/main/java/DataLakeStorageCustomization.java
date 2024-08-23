// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.JavadocCustomization;
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
 * Customization class for File DataLake Storage.
 */
public class DataLakeStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models  = customization.getPackage("com.azure.storage.file.datalake.implementation.models");

        //customizing BlobHierarchyListSegment
        ClassCustomization blobHierarchy = models.getClass("BlobHierarchyListSegment");
        blobHierarchy.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(blobHierarchy.getClassName()).get();

            replaceMethodToXml(clazz,
                "{\n" +
                "rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Blobs\" : rootElementName;\n" +
                    "        xmlWriter.writeStartElement(rootElementName);\n" +
                    "        if (this.blobPrefixes != null) {\n" +
                    "            for (BlobPrefix element : this.blobPrefixes) {\n" +
                    "                xmlWriter.writeXml(element, \"BlobPrefix\");\n" +
                    "            }\n" +
                    "        }\n" +
                    "        if (this.blobItems != null) {\n" +
                    "            for (BlobItemInternal element : this.blobItems) {\n" +
                    "                xmlWriter.writeXml(element, \"Blob\");\n" +
                    "            }\n" +
                    "        }\n" +
                    "        return xmlWriter.writeEndElement();\n" +
                "}"
            );

            replaceMethodFromXml(clazz,
                "{\n" +
                "String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Blobs\" : rootElementName;\n" +
                    "        return xmlReader.readObject(finalRootElementName, reader -> {\n" +
                    "            BlobHierarchyListSegment deserializedBlobHierarchyListSegment = new BlobHierarchyListSegment();\n" +
                    "            while (reader.nextElement() != XmlToken.END_ELEMENT) {\n" +
                    "                QName elementName = reader.getElementName();\n" +
                    "\n" +
                    "                if (\"BlobPrefix\".equals(elementName.getLocalPart())) {\n" +
                    "                    if (deserializedBlobHierarchyListSegment.blobPrefixes == null) {\n" +
                    "                        deserializedBlobHierarchyListSegment.blobPrefixes = new ArrayList<>();\n" +
                    "                    }\n" +
                    "                    deserializedBlobHierarchyListSegment.blobPrefixes\n" +
                    "                        .add(BlobPrefix.fromXml(reader, \"BlobPrefix\"));\n" +
                    "                } else if (\"Blob\".equals(elementName.getLocalPart())) {\n" +
                    "                    if (deserializedBlobHierarchyListSegment.blobItems == null) {\n" +
                    "                        deserializedBlobHierarchyListSegment.blobItems = new ArrayList<>();\n" +
                    "                    }\n" +
                    "                    deserializedBlobHierarchyListSegment.blobItems\n" +
                    "                        .add(BlobItemInternal.fromXml(reader, \"Blob\"));\n" +
                    "                } else {\n" +
                    "                    reader.skipElement();\n" +
                    "                }\n" +
                    "            }\n" +
                    "\n" +
                    "            return deserializedBlobHierarchyListSegment;\n" +
                    "        });\n" +
                    "}"

            );
        });

        //customizing Path
        ClassCustomization path = models.getClass("Path");
        path.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(path.getClassName()).get();

            replaceMethodToJson(clazz,
                "{\n" +
                    "        jsonWriter.writeStartObject();\n" +
                    "        jsonWriter.writeStringField(\"name\", this.name);\n" +
                    "        if (isDirectory != null) {\n" +
                    "            jsonWriter.writeStringField(\"isDirectory\", String.valueOf(this.isDirectory));\n" +
                    "        }\n" +
                    "        jsonWriter.writeStringField(\"lastModified\", this.lastModified);\n" +
                    "        if (contentLength != null) {\n" +
                    "            jsonWriter.writeStringField(\"contentLength\", String.valueOf(this.contentLength));\n" +
                    "        }\n" +
                    "        jsonWriter.writeStringField(\"owner\", this.owner);\n" +
                    "        jsonWriter.writeStringField(\"group\", this.group);\n" +
                    "        jsonWriter.writeStringField(\"permissions\", this.permissions);\n" +
                    "        jsonWriter.writeStringField(\"EncryptionScope\", this.encryptionScope);\n" +
                    "        jsonWriter.writeStringField(\"creationTime\", this.creationTime);\n" +
                    "        jsonWriter.writeStringField(\"expiryTime\", this.expiryTime);\n" +
                    "        jsonWriter.writeStringField(\"EncryptionContext\", this.encryptionContext);\n" +
                    "        jsonWriter.writeStringField(\"etag\", this.eTag);\n" +
                    "        return jsonWriter.writeEndObject();\n" +
                    "    }"
            );

            replaceMethodFromJson(clazz,
                "{\n" +
                "        return jsonReader.readObject(reader -> {\n" +
                "            Path deserializedPath = new Path();\n" +
                "            while (reader.nextToken() != JsonToken.END_OBJECT) {\n" +
                "                String fieldName = reader.getFieldName();\n" +
                "                reader.nextToken();\n" +
                "\n" +
                "                if (\"name\".equals(fieldName)) {\n" +
                "                    deserializedPath.name = reader.getString();\n" +
                "                } else if (\"isDirectory\".equals(fieldName)) {\n" +
                "                    JsonToken token = reader.currentToken();\n" +
                "                    if (token == JsonToken.STRING) {\n" +
                "                        deserializedPath.isDirectory = Boolean.parseBoolean(reader.getString());\n" +
                "                    } else if (token == JsonToken.BOOLEAN) {\n" +
                "                        deserializedPath.isDirectory = reader.getBoolean();\n" +
                "                    } else if (token == JsonToken.NULL) {\n" +
                "                        deserializedPath.isDirectory = null;\n" +
                "                    } else {\n" +
                "                        throw new IllegalStateException(\"Invalid token, expected one of STRING, BOOLEAN, or NULL. Was \" + token);\n" +
                "                    }\n" +
                "                } else if (\"lastModified\".equals(fieldName)) {\n" +
                "                    deserializedPath.lastModified = reader.getString();\n" +
                "                } else if (\"contentLength\".equals(fieldName)) {\n" +
                "                    JsonToken token = reader.currentToken();\n" +
                "                    if (token == JsonToken.STRING) {\n" +
                "                        deserializedPath.contentLength = Long.parseLong(reader.getString());\n" +
                "                    } else if (token == JsonToken.NUMBER) {\n" +
                "                        deserializedPath.contentLength = reader.getLong();\n" +
                "                    } else if (token == JsonToken.NULL) {\n" +
                "                        deserializedPath.contentLength = null;\n" +
                "                    } else {\n" +
                "                        throw new IllegalStateException(\"Invalid token, expected one of STRING, NUMBER, or NULL. Was \" + token);\n" +
                "                    }\n" +
                "                } else if (\"owner\".equals(fieldName)) {\n" +
                "                    deserializedPath.owner = reader.getString();\n" +
                "                } else if (\"group\".equals(fieldName)) {\n" +
                "                    deserializedPath.group = reader.getString();\n" +
                "                } else if (\"permissions\".equals(fieldName)) {\n" +
                "                    deserializedPath.permissions = reader.getString();\n" +
                "                } else if (\"EncryptionScope\".equals(fieldName)) {\n" +
                "                    deserializedPath.encryptionScope = reader.getString();\n" +
                "                } else if (\"creationTime\".equals(fieldName)) {\n" +
                "                    deserializedPath.creationTime = reader.getString();\n" +
                "                } else if (\"expiryTime\".equals(fieldName)) {\n" +
                "                    deserializedPath.expiryTime = reader.getString();\n" +
                "                } else if (\"EncryptionContext\".equals(fieldName)) {\n" +
                "                    deserializedPath.encryptionContext = reader.getString();\n" +
                "                } else if (\"etag\".equals(fieldName)) {\n" +
                "                    deserializedPath.eTag = reader.getString();\n" +
                "                } else {\n" +
                "                    reader.skipChildren();\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            return deserializedPath;\n" +
                "        });\n" +
                "    }"
            );
        });

        JavadocCustomization setActiveJavadoc = path.getMethod("fromJson").getJavadoc();
        setActiveJavadoc.addThrows("IllegalStateException", "If a token is not an allowed type.");

        updateImplToMapInternalException(customization.getPackage("com.azure.storage.file.datalake.implementation"));
    }

    private static void replaceMethodToJson(ClassOrInterfaceDeclaration clazz, String newBody) {
        MethodDeclaration method = clazz.getMethodsBySignature("toJson", "JsonWriter").get(0);
        method.setBody(StaticJavaParser.parseBlock(newBody));
    }

    private static void replaceMethodFromJson(ClassOrInterfaceDeclaration clazz, String newBody) {
        MethodDeclaration method = clazz.getMethodsBySignature("fromJson", "JsonReader").get(0);
        method.setBody(StaticJavaParser.parseBlock(newBody));
    }

    private static void replaceMethodToXml(ClassOrInterfaceDeclaration clazz, String newBody) {
        MethodDeclaration method = clazz.getMethodsBySignature("toXml", "XmlWriter", "String").get(0);
        method.setBody(StaticJavaParser.parseBlock(newBody));
    }

    private static void replaceMethodFromXml(ClassOrInterfaceDeclaration clazz, String newBody) {
        MethodDeclaration method = clazz.getMethodsBySignature("fromXml", "XmlReader", "String").get(0);
        method.setBody(StaticJavaParser.parseBlock(newBody));
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to {@code .onErrorMap(ModelHelper::mapToDataLakeStorageException} to
     * handle mapping DataLakeStorageExceptionInternal to DataLakeStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * DataLakeStorageExceptionInternal and rethrows
     * {@code (DataLakeStorageException) ModelHelper.mapToDataLakeStorageException(e)}. Or, for void methods wrap the last
     * statement.
     *
     * @param implPackage The implementation package.
     */
    private static void updateImplToMapInternalException(PackageCustomization implPackage) {
        List<String> implsToUpdate = Arrays.asList("FileSystemsImpl", "PathsImpl", "ServicesImpl");
        for (String implToUpdate : implsToUpdate) {
            implPackage.getClass(implToUpdate).customizeAst(ast -> {
                ast.addImport("com.azure.storage.file.datalake.implementation.util.ModelHelper");
                ast.addImport("com.azure.storage.file.datalake.models.DataLakeStorageException");
                ast.addImport("com.azure.storage.file.datalake.implementation.models.DataLakeStorageExceptionInternal");
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
            + ".onErrorMap(ModelHelper::mapToDataLakeStorageException)" + originalReturnStatement.substring(insertionPoint)
            + ";";
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
            "throw (DataLakeStorageException) ModelHelper.mapToDataLakeStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("DataLakeStorageExceptionInternal")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        body.getStatements().set(body.getStatements().size() - 1, tryCatchMap);
    }
}
