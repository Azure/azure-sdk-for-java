// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.models;

import com.azure.xml.XmlSerializable;
import com.azure.xml.XmlWriter;

import javax.xml.stream.XMLStreamException;

/**
 * Represents an error response returned by the Azure Storage Blob service.
 */
public final class BlobStorageError implements XmlSerializable<BlobStorageError> {
    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String s) throws XMLStreamException {
        return null;
    }
}
