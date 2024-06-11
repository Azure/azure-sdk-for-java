// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

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

            updateMethodXml(clazz, "toXml",
                "if (this.blobPrefixes != null) {\n" +
                    "            xmlWriter.writeStartElement(\"BlobPrefixes\");\n" + //removing
                    "            for (BlobPrefix element : this.blobPrefixes) {\n" +
                    "                xmlWriter.writeXml(element, \"BlobPrefix\");\n" +
                    "            }\n" +
                    "            xmlWriter.writeEndElement();\n" + //removing
                    "        }\n" +
                    "        if (this.blobItems != null) {\n" +
                    "            xmlWriter.writeStartElement(\"BlobItems\");\n" + //removing
                    "            for (BlobItemInternal element : this.blobItems) {\n" +
                    "                xmlWriter.writeXml(element, \"Blob\");\n" +
                    "            }\n" +
                    "            xmlWriter.writeEndElement();\n" + //removing
                    "        }",
                "if (this.blobPrefixes != null) {\n" +
                    "            for (BlobPrefix element : this.blobPrefixes) {\n" +
                    "                xmlWriter.writeXml(element, \"BlobPrefix\");\n" +
                    "            }\n" +
                    "        }\n" +
                    "        if (this.blobItems != null) {\n" +
                    "            for (BlobItemInternal element : this.blobItems) {\n" +
                    "                xmlWriter.writeXml(element, \"Blob\");\n" +
                    "            }\n" +
                    "        }"
            );

            updateMethodXml(clazz, "fromXml",
                "if (\"BlobPrefixes\".equals(elementName.getLocalPart())) {\n" + //removing
                    "                    while (reader.nextElement() != XmlToken.END_ELEMENT) {\n" + //removing
                    "                        elementName = reader.getElementName();\n" + //removing
                    "                        if (\"BlobPrefix\".equals(elementName.getLocalPart())) {\n" +
                    "                            if (deserializedBlobHierarchyListSegment.blobPrefixes == null) {\n" +
                    "                                deserializedBlobHierarchyListSegment.blobPrefixes = new ArrayList<>();\n" +
                    "                            }\n" +
                    "                            deserializedBlobHierarchyListSegment.blobPrefixes\n" +
                    "                                .add(BlobPrefix.fromXml(reader, \"BlobPrefix\"));\n" +
                    "                        } else {\n" + //removing
                    "                            reader.skipElement();\n" + //removing
                    "                        }\n" + //removing
                    "                    }\n" + //removing
                    "                } else if (\"BlobItems\".equals(elementName.getLocalPart())) {\n" + //removing
                    "                    while (reader.nextElement() != XmlToken.END_ELEMENT) {\n" + //removing
                    "                        elementName = reader.getElementName();\n" + //removing
                    "                        if (\"Blob\".equals(elementName.getLocalPart())) {\n" +
                    "                            if (deserializedBlobHierarchyListSegment.blobItems == null) {\n" +
                    "                                deserializedBlobHierarchyListSegment.blobItems = new ArrayList<>();\n" +
                    "                            }\n" +
                    "                            deserializedBlobHierarchyListSegment.blobItems\n" +
                    "                                .add(BlobItemInternal.fromXml(reader, \"Blob\"));\n" +
                    "                        } else {\n" + //removing
                    "                            reader.skipElement();\n" + //removing
                    "                        }\n" + //removing
                    "                    }\n" + //removing
                    "                }",
                "if (\"BlobPrefix\".equals(elementName.getLocalPart())) {\n" +
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
                    "                }"
            );
        });

        //customizing Path
        ClassCustomization path = models.getClass("Path");
        path.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(blobHierarchy.getClassName()).get();

            updateMethodJson(clazz, "toJson",
                "jsonWriter.writeNumberField(\"contentLength\", this.contentLength);", //changing to string
                "if (contentLength != null) {\n" +
                    "            jsonWriter.writeStringField(\"contentLength\", String.valueOf(this.contentLength));\n" +
                    "        }"
            );

            updateMethodJson(clazz, "fromJson",
                "if (\"name\".equals(fieldName)) {\n" +
                    "                    deserializedPath.name = reader.getString();\n" +
                    "                } else if (\"isDirectory\".equals(fieldName)) {\n" + //adding general id
                    "                    deserializedPath.isDirectory = reader.getNullable(JsonReader::getBoolean);\n" +
                    "                } else if (\"lastModified\".equals(fieldName)) {\n" +
                    "                    deserializedPath.lastModified = reader.getString();\n" +
                    "                } else if (\"contentLength\".equals(fieldName)) {\n" + //adding general id
                    "                    deserializedPath.contentLength = reader.getNullable(JsonReader::getLong);\n" +
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
                    "                }",
                "if (\"name\".equals(fieldName)) {\n" +
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
                    "                        throw new IllegalStateException(\"Invalid token, expected one of STRING, NUMBER, or NULL. Was \" + token);\n" +
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
                    "                }"
            );
        });
    }

    private static void updateMethodJson(ClassOrInterfaceDeclaration clazz, String methodName, String find, String replace) {
        MethodDeclaration method = clazz.getMethodsByName(methodName).get(0);
        String body = method.getBody().get().toString().replace(find, replace);
        method.setBody(StaticJavaParser.parseBlock(body));
    }

    private static void updateMethodXml(ClassOrInterfaceDeclaration clazz, String methodName, String find, String replace) {
        MethodDeclaration method = clazz.getMethodsByName(methodName).get(1);
        String body = method.getBody().get().toString().replace(find, replace);
        method.setBody(StaticJavaParser.parseBlock(body));
    }
}
