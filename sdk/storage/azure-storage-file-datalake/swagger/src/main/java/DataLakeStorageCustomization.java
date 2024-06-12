// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.JavadocCustomization;
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
}
