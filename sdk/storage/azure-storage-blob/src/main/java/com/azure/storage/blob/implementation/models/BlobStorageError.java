// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.implementation.models;

import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.xml.XmlReader;
import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlToken;
import com.azure.xml.XmlWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Objects;

/**
 * Represents an error response returned by the Azure Storage Blob service.
 */
public final class BlobStorageError implements XmlSerializable<BlobStorageError> {
    private BlobErrorCode errorCode;
    private String message;
    private String queryParameterName;
    private String queryParameterValue;
    private String reason;
    private String extendedErrorDetail;

    private BlobStorageError() {
    }

    /**
     * Gets the error code returned by the Azure Storage Blob service.
     *
     * @return The error code.
     */
    public BlobErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the error message returned by the Azure Storage Blob service.
     *
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        rootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "Error" : rootElementName;
        xmlWriter.writeStartElement(rootElementName);
        xmlWriter.writeStringElement("Code", Objects.toString(errorCode, null));
        xmlWriter.writeStringElement("Message", this.message);
        xmlWriter.writeStringElement("QueryParameterName", this.queryParameterName);
        xmlWriter.writeStringElement("QueryParameterValue", this.queryParameterValue);
        xmlWriter.writeStringElement("Reason", this.reason);
        xmlWriter.writeStringElement("ExtendedErrorDetail", this.extendedErrorDetail);
        return xmlWriter.writeEndElement();
    }

    /**
     * Reads an instance of BlobStorageError from the XmlReader.
     *
     * @param xmlReader The XmlReader being read.
     * @return An instance of BlobStorageError if the XmlReader was pointing to an instance of it, or null if it was
     * pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the BlobStorageError.
     */
    public static BlobStorageError fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    /**
     * Reads an instance of BlobStorageError from the XmlReader.
     *
     * @param xmlReader The XmlReader being read.
     * @param rootElementName Optional root element name to override the default defined by the model. Used to support
     * cases where the model can deserialize from different root element names.
     * @return An instance of BlobStorageError if the XmlReader was pointing to an instance of it, or null if it was
     * pointing to XML null.
     * @throws XMLStreamException If an error occurs while reading the BlobStorageError.
     */
    public static BlobStorageError fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        String finalRootElementName = CoreUtils.isNullOrEmpty(rootElementName) ? "Error" : rootElementName;
        return xmlReader.readObject(finalRootElementName, reader -> {
            BlobStorageError deserializedStorageError = new BlobStorageError();
            while (reader.nextElement() != XmlToken.END_ELEMENT) {
                QName elementName = reader.getElementName();

                if ("Code".equals(elementName.getLocalPart())) {
                    deserializedStorageError.errorCode = BlobErrorCode.fromString(reader.getStringElement());
                } else if ("Message".equals(elementName.getLocalPart())) {
                    deserializedStorageError.message = reader.getStringElement();
                } else if ("QueryParameterName".equals(elementName.getLocalPart())) {
                    deserializedStorageError.queryParameterName = reader.getStringElement();
                } else if ("QueryParameterValue".equals(elementName.getLocalPart())) {
                    deserializedStorageError.queryParameterValue = reader.getStringElement();
                } else if ("Reason".equals(elementName.getLocalPart())) {
                    deserializedStorageError.reason = reader.getStringElement();
                } else if ("ExtendedErrorDetail".equals(elementName.getLocalPart())) {
                    deserializedStorageError.extendedErrorDetail = reader.getStringElement();
                }
            }

            return deserializedStorageError;
        });
    }
}
