// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.xml;

import javax.xml.stream.XMLStreamException;

import static com.azure.xml.AzureXmlTestUtils.getRootElementName;

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
    public XmlWriter toXml(XmlWriter xmlWriter) throws XMLStreamException {
        return toXml(xmlWriter, null);
    }

    @Override
    public XmlWriter toXml(XmlWriter xmlWriter, String rootElementName) throws XMLStreamException {
        xmlWriter.writeStartSelfClosingElement(getRootElementName(rootElementName, "link"));
        xmlWriter.writeNamespace("http://www.w3.org/2005/Atom");
        xmlWriter.writeStringAttribute("rel", rel);
        xmlWriter.writeStringAttribute("href", href);

        return xmlWriter.flush();
    }

    public static ResponseLink fromXml(XmlReader xmlReader) throws XMLStreamException {
        return fromXml(xmlReader, null);
    }

    public static ResponseLink fromXml(XmlReader xmlReader, String rootElementName) throws XMLStreamException {
        return xmlReader.readObject("http://www.w3.org/2005/Atom", getRootElementName(rootElementName, "link"),
            reader -> {
                String rel = xmlReader.getStringAttribute(null, "rel");
                String href = xmlReader.getStringAttribute(null, "href");

                while (xmlReader.currentToken() != XmlToken.END_ELEMENT) {
                    xmlReader.skipElement();
                }

                return new ResponseLink().setHref(href).setRel(rel);
            });
    }
}
