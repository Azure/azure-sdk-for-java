// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.namespace.QName;

public class ResponseLink implements XmlSerializable<ResponseLink> {
    private String href;
    private String rel;

    /**
     * Get the href property: The URL of the GET request.
     *
     * @return the href value.
     */
    public String getHref() {
        return this.href;
    }

    /**
     * Set the href property: The URL of the GET request.
     *
     * @param href the href value to set.
     * @return the ResponseLink object itself.
     */
    public ResponseLink setHref(String href) {
        this.href = href;
        return this;
    }

    /**
     * Get the rel property: What the link href is relative to.
     *
     * @return the rel value.
     */
    public String getRel() {
        return this.rel;
    }

    /**
     * Set the rel property: What the link href is relative to.
     *
     * @param rel the rel value to set.
     * @return the ResponseLink object itself.
     */
    public ResponseLink setRel(String rel) {
        this.rel = rel;
        return this;
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter) {
        xmlWriter.writeStartSelfClosingElement("link");
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringAttribute("rel", rel);
        xmlWriter.writeStringAttribute("href", href);

        return xmlWriter.flush();
    }

    public static ResponseLink fromXml(XmlReader xmlReader) {
        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            xmlReader.nextElement();
        }

        if (xmlReader.currentToken() != XmlToken.START_ELEMENT) {
            throw new IllegalStateException("Illegal start of XML deserialization. "
                + "Expected 'XmlToken.START_ELEMENT' but it was: 'XmlToken." + xmlReader.currentToken() + "'.");
        }

        QName qName = xmlReader.getElementName();
        if (!"link".equals(qName.getLocalPart())
            || !"http://www.w3.org/2005/Atom".equals(qName.getNamespaceURI())) {
            throw new IllegalStateException("Expected XML element to be 'link' in namespace "
                + "'http://www.w3.org/2005/Atom' but it was: "
                + "{'" + qName.getNamespaceURI() + "'}'" + qName.getLocalPart() + "'.");
        }

        String rel = xmlReader.getAttributeStringValue(null, "rel");
        String href = xmlReader.getAttributeStringValue(null, "href");

        while (xmlReader.currentToken() != XmlToken.END_ELEMENT) {
            xmlReader.nextElement();
        }

        return new ResponseLink().setHref(href).setRel(rel);
    }
}
