// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.slf4j.Logger;

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
}
