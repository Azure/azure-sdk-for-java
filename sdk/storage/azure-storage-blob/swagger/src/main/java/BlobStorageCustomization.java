
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;

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

            ClassOrInterfaceDeclaration clazz = ast.getClassByName("PageList").get();

            clazz.getMethodsByName("getNextMarker").get(0).setModifiers(com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
            clazz.getMethodsByName("setNextMarker").get(0).setModifiers(com.github.javaparser.ast.Modifier.Keyword.PRIVATE);

            // Add Accessor to PageList
            clazz.setMembers(clazz.getMembers().addFirst(StaticJavaParser.parseBodyDeclaration(String.join("\n",
                "static {",
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
                "}"
            ))));
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
        ClassCustomization queryFormat = implementationModels.getClass("QueryFormat");
        customizeQueryFormat(queryFormat);

        //BlobHierarchyListSegment
        ClassCustomization blobHierarchyListSegment = implementationModels.getClass("BlobHierarchyListSegment");
        customizeBlobHierarchyListSegment(blobHierarchyListSegment);

        //BlobFlatListSegment
        ClassCustomization blobFlatListSegment = implementationModels.getClass("BlobFlatListSegment");
        customizeBlobFlatListSegment(blobFlatListSegment);

        //BlobSignedIdentifierWrapper
        ClassCustomization blobSignedIdentifierWrapper = implementationModels.getClass("BlobSignedIdentifierWrapper");
        customizeBlobSignedIdentifierWrapper(blobSignedIdentifierWrapper);

    }
    private static void customizeQueryFormat(ClassCustomization classCustomization) {
        String fileContent = classCustomization.getEditor().getFileContent(classCustomization.getFileName());
        fileContent = fileContent.replace("xmlWriter.nullElement(\"ParquetTextConfiguration\", this.parquetTextConfiguration);",
            "xmlWriter.writeStartElement(\"ParquetTextConfiguration\").writeEndElement();");
        fileContent = fileContent.replace("deserializedQueryFormat.parquetTextConfiguration = reader.null;",
            "deserializedQueryFormat.parquetTextConfiguration = new Object();\nxmlReader.skipElement();");
        classCustomization.getEditor().replaceFile(classCustomization.getFileName(), fileContent);
    }

    private static void customizeBlobHierarchyListSegment(ClassCustomization classCustomization){
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("toXml", "XmlWriter", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Blobs\" : rootElementName;",
                    "xmlWriter.writeStartElement(rootElementName);",
                    "if (this.blobPrefixes != null) {",
                    "    for (BlobPrefixInternal element : this.blobPrefixes) {",
                    "        xmlWriter.writeXml(element, \"BlobPrefix\");",
                    "    }",
                    "}",
                    "if (this.blobItems != null) {",
                    "    for (BlobItemInternal element : this.blobItems) {",
                    "        xmlWriter.writeXml(element, \"Blob\");",
                    "    }",
                    "}",
                    "return xmlWriter.writeEndElement();",
                    "}"
                )));

            clazz.getMethodsBySignature("fromXml", "XmlReader", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(String.join("\n",
                    "{",
                    "String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Blobs\" : rootElementName;",
                    "return xmlReader.readObject(finalRootElementName, reader -> {",
                    "    BlobHierarchyListSegment deserializedBlobHierarchyListSegment",
                    "        = new BlobHierarchyListSegment();",
                    "    while (reader.nextElement() != XmlToken.END_ELEMENT) {",
                    "        QName elementName = reader.getElementName();",
                    "",
                    "        if (\"BlobPrefix\".equals(elementName.getLocalPart())) {",
                    "            if (deserializedBlobHierarchyListSegment.blobPrefixes == null) {",
                    "                deserializedBlobHierarchyListSegment.blobPrefixes = new ArrayList<>();",
                    "            }",
                    "            deserializedBlobHierarchyListSegment.blobPrefixes",
                    "                .add(BlobPrefixInternal.fromXml(reader, \"BlobPrefix\"));",
                    "        } else if (\"Blob\".equals(elementName.getLocalPart())) {",
                    "            if (deserializedBlobHierarchyListSegment.blobItems == null) {",
                    "                deserializedBlobHierarchyListSegment.blobItems = new ArrayList<>();",
                    "            }",
                    "            deserializedBlobHierarchyListSegment.blobItems.add(BlobItemInternal.fromXml(reader, \"Blob\"));",
                    "        } else {",
                    "            reader.skipElement();",
                    "        }",
                    "    }",
                    "",
                    "    return deserializedBlobHierarchyListSegment;",
                    "});",
                    "}"
                )));
        });
    }

    private static void customizeBlobFlatListSegment(ClassCustomization classCustomization){
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(classCustomization.getClassName()).get();

            clazz.getMethodsBySignature("toXml", "XmlWriter", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(
                    "{\n" +
                    "rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Blobs\" : rootElementName;" +
                        "        xmlWriter.writeStartElement(rootElementName);\n" +
                        "        if (this.blobItems != null) {\n" +
                        "            for (BlobItemInternal element : this.blobItems) {\n" +
                        "                xmlWriter.writeXml(element, \"Blob\");\n" +
                        "            }\n" +
                        "        }\n" +
                        "        return xmlWriter.writeEndElement();\n" +
                        "}"
                ));

            clazz.getMethodsBySignature("fromXml", "XmlReader", "String").get(0)
                .setBody(StaticJavaParser.parseBlock(
                    "{\n" +
                    "String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? \"Blobs\" : rootElementName;\n" +
                    "        return xmlReader.readObject(finalRootElementName, reader -> {\n" +
                    "            BlobFlatListSegment deserializedBlobFlatListSegment = new BlobFlatListSegment();\n" +
                    "            while (reader.nextElement() != XmlToken.END_ELEMENT) {\n" +
                    "                QName elementName = reader.getElementName();\n" +
                    "                if (\"Blob\".equals(elementName.getLocalPart())) {\n" +
                    "                    if (deserializedBlobFlatListSegment.blobItems == null) {\n" +
                    "                        deserializedBlobFlatListSegment.blobItems = new ArrayList<>();\n" +
                    "                    }\n" +
                    "                    deserializedBlobFlatListSegment.blobItems.add(BlobItemInternal.fromXml(reader, \"Blob\"));\n" +
                    "                } else {\n" +
                    "                    reader.skipElement();\n" +
                    "                }\n" +
                    "            }\n" +
                    "\n" +
                    "            return deserializedBlobFlatListSegment;\n" +
                    "        });\n" +
                    "}"
                ));
        });
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
}
