// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;

class NamespaceInfoSerializer {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(NamespaceInfoSerializer.class);

    static NamespaceInfo parseFromContent(String xml) throws ServiceBusException {
        try {
            DocumentBuilderFactory dbf = SerializerUtil.getDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            if ("entry".equals(doc.getTagName())) {
                return parseFromEntry(doc);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.info("Exception while parsing response.", e);
            }

            if (TRACE_LOGGER.isDebugEnabled()) {
                TRACE_LOGGER.debug("XML which failed to parse: \n %s", xml);
            }
        }

        throw new ServiceBusException(false, "Unable to deserialize the content.");
    }

    private static NamespaceInfo parseFromEntry(Node xEntry) {
        NamespaceInfo namespaceInfo = new NamespaceInfo();
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "content":
                        NodeList nsInfoNodes = element.getFirstChild().getChildNodes();
                        for (int j = 0; j < nsInfoNodes.getLength(); j++) {
                            node = nsInfoNodes.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) node;
                                switch (element.getTagName()) {
                                    case "CreatedTime":
                                        namespaceInfo.setCreatedAt(Instant.parse(element.getFirstChild().getNodeValue()));
                                        break;
                                    case "ModifiedTime":
                                        namespaceInfo.setModifiedAt(Instant.parse(element.getFirstChild().getNodeValue()));
                                        break;
                                    case "Name":
                                        namespaceInfo.setName(element.getFirstChild().getNodeValue());
                                        break;
                                    case "Alias":
                                        namespaceInfo.setAlias(element.getFirstChild().getNodeValue());
                                        break;
                                    case "NamespaceType":
                                        String namespaceType = element.getFirstChild().getNodeValue();
                                        switch (namespaceType) {
                                            case "Mixed":
                                                namespaceInfo.setNamespaceType(NamespaceType.Mixed);
                                                break;
                                            case "Messaging":
                                                namespaceInfo.setNamespaceType(NamespaceType.ServiceBus);
                                                break;
                                            default:
                                                namespaceInfo.setNamespaceType(NamespaceType.Unknown);
                                                break;
                                        }
                                        break;
                                    case "MessagingSKU":
                                        try {
                                            namespaceInfo.setNamespaceSku(NamespaceSku.valueOf(element.getFirstChild().getNodeValue()));
                                        } catch (IllegalArgumentException ignored) {
                                            namespaceInfo.setNamespaceSku(NamespaceSku.Unknown);
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        return namespaceInfo;
    }
}
