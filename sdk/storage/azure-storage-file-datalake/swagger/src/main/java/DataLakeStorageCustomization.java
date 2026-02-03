// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Customization class for File DataLake Storage.
 */
public class DataLakeStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models  = customization.getPackage("com.azure.storage.file.datalake.implementation.models");

        //customizing Path
        models.getClass("Path").customizeAst(ast -> ast.getClassByName("Path").ifPresent(clazz -> {
            clazz.getMethodsByName("toJson").forEach(method -> method.setBody(StaticJavaParser.parseBlock("{"
                + "jsonWriter.writeStartObject();"
                + "jsonWriter.writeStringField(\"name\", this.name);"
                + "if (isDirectory != null) {"
                + "    jsonWriter.writeStringField(\"isDirectory\", String.valueOf(this.isDirectory));"
                + "}"
                + "jsonWriter.writeStringField(\"lastModified\", this.lastModified);"
                + "if (contentLength != null) {"
                + "    jsonWriter.writeStringField(\"contentLength\", String.valueOf(this.contentLength));"
                + "}"
                + "jsonWriter.writeStringField(\"owner\", this.owner);"
                + "jsonWriter.writeStringField(\"group\", this.group);"
                + "jsonWriter.writeStringField(\"permissions\", this.permissions);"
                + "jsonWriter.writeStringField(\"EncryptionScope\", this.encryptionScope);"
                + "jsonWriter.writeStringField(\"creationTime\", this.creationTime);"
                + "jsonWriter.writeStringField(\"expiryTime\", this.expiryTime);"
                + "jsonWriter.writeStringField(\"EncryptionContext\", this.encryptionContext);"
                + "jsonWriter.writeStringField(\"etag\", this.eTag);"
                + "return jsonWriter.writeEndObject(); }")));

            clazz.getMethodsByName("fromJson").forEach(method -> method.setBody(StaticJavaParser.parseBlock("{"
                + "return jsonReader.readObject(reader -> {"
                + "    Path deserializedPath = new Path();"
                + "    while (reader.nextToken() != JsonToken.END_OBJECT) {"
                + "        String fieldName = reader.getFieldName();"
                + "        reader.nextToken();"
                + "        if (\"name\".equals(fieldName)) {"
                + "            deserializedPath.name = reader.getString();"
                + "        } else if (\"isDirectory\".equals(fieldName)) {"
                + "            JsonToken token = reader.currentToken();"
                + "            if (token == JsonToken.STRING) {"
                + "                deserializedPath.isDirectory = Boolean.parseBoolean(reader.getString());"
                + "            } else if (token == JsonToken.BOOLEAN) {"
                + "                deserializedPath.isDirectory = reader.getBoolean();"
                + "            } else if (token == JsonToken.NULL) {"
                + "                deserializedPath.isDirectory = null;"
                + "            } else {"
                + "                throw new IllegalStateException(\"Invalid token, expected one of STRING, BOOLEAN, or NULL. Was \" + token);"
                + "            }"
                + "        } else if (\"lastModified\".equals(fieldName)) {"
                + "            deserializedPath.lastModified = reader.getString();"
                + "        } else if (\"contentLength\".equals(fieldName)) {"
                + "            JsonToken token = reader.currentToken();"
                + "            if (token == JsonToken.STRING) {"
                + "                deserializedPath.contentLength = Long.parseLong(reader.getString());"
                + "            } else if (token == JsonToken.NUMBER) {"
                + "                deserializedPath.contentLength = reader.getLong();"
                + "            } else if (token == JsonToken.NULL) {"
                + "                deserializedPath.contentLength = null;"
                + "            } else {"
                + "                throw new IllegalStateException(\"Invalid token, expected one of STRING, NUMBER, or NULL. Was \" + token);"
                + "            }"
                + "        } else if (\"owner\".equals(fieldName)) {"
                + "            deserializedPath.owner = reader.getString();"
                + "        } else if (\"group\".equals(fieldName)) {"
                + "            deserializedPath.group = reader.getString();"
                + "        } else if (\"permissions\".equals(fieldName)) {"
                + "            deserializedPath.permissions = reader.getString();"
                + "        } else if (\"EncryptionScope\".equals(fieldName)) {"
                + "            deserializedPath.encryptionScope = reader.getString();"
                + "        } else if (\"creationTime\".equals(fieldName)) {"
                + "            deserializedPath.creationTime = reader.getString();"
                + "        } else if (\"expiryTime\".equals(fieldName)) {"
                + "            deserializedPath.expiryTime = reader.getString();"
                + "        } else if (\"EncryptionContext\".equals(fieldName)) {"
                + "            deserializedPath.encryptionContext = reader.getString();"
                + "        } else if (\"etag\".equals(fieldName)) {"
                + "            deserializedPath.eTag = reader.getString();"
                + "        } else {"
                + "            reader.skipChildren();"
                + "        }"
                + "    }"
                + "    return deserializedPath;"
                + "}); }"))
                .getJavadoc().ifPresent(javadoc -> method.setJavadocComment(javadoc
                    .addBlockTag("throws", "IllegalStateException", "If a token is not an allowed type."))));
        }));

        PackageCustomization implementation = customization.getPackage("com.azure.storage.file.datalake.implementation");
        updateImplToMapInternalException(implementation);
    }

    /**
     * Customizes the implementation classes that will perform calls to the service. The following logic is used:
     * <p>
     * - Check for the return of the method not equaling to PagedFlux, PagedIterable, PollerFlux, or SyncPoller. Those
     * types wrap other APIs and those APIs being update is the correct change.
     * - For asynchronous methods, add a call to
     * {@code .onErrorMap(DataLakeStorageException.class, ModelHelper::mapToDataLakeStorageException)} to
     * handle mapping DataLakeStorageExceptionInternal to DataLakeStorageException.
     * - For synchronous methods, wrap the return statement in a try-catch block that catches
     * DataLakeStorageExceptionInternal and rethrows {@code ModelHelper.mapToDataLakeStorageException(e)}. Or, for void
     * methods wrap the last statement.
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
            + ".onErrorMap(DataLakeStorageExceptionInternal.class, ModelHelper::mapToDataLakeStorageException)"
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
            "throw ModelHelper.mapToDataLakeStorageException(internalException);")));
        Parameter catchParameter = new Parameter().setType("DataLakeStorageExceptionInternal")
            .setName("internalException");
        CatchClause catchClause = new CatchClause(catchParameter, catchBlock);
        TryStmt tryCatchMap = new TryStmt(tryBlock, new NodeList<>(catchClause), null);

        // Replace the last statement with the try-catch block.
        method.setBody(new BlockStmt(new NodeList<>(tryCatchMap)));
    }
}
