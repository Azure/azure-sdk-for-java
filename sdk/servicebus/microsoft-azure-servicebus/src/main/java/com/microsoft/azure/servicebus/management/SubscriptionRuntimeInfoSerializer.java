// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.primitives.MessagingEntityNotFoundException;
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

public class SubscriptionRuntimeInfoSerializer {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SubscriptionRuntimeInfoSerializer.class);

    static SubscriptionRuntimeInfo parseFromContent(String topicPath, String xml) throws MessagingEntityNotFoundException {
        try {
            DocumentBuilderFactory dbf = SerializerUtil.getDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            if ("entry".equals(doc.getTagName())) {
                return parseFromEntry(topicPath, doc);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.info("Exception while parsing response.", e);
            }

            if (TRACE_LOGGER.isDebugEnabled()) {
                TRACE_LOGGER.debug("XML which failed to parse: \n %s", xml);
            }
        }

        throw new MessagingEntityNotFoundException("Subscription was not found");
    }

    private static SubscriptionRuntimeInfo parseFromEntry(String topicPath, Node xEntry) {
        SubscriptionRuntimeInfo runtimeInfo = null;
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "title":
                        runtimeInfo = new SubscriptionRuntimeInfo(topicPath, element.getFirstChild().getNodeValue());
                        break;
                    case "content":
                        NodeList qdNodes = element.getFirstChild().getChildNodes();
                        for (int j = 0; j < qdNodes.getLength(); j++) {
                            node = qdNodes.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) node;
                                switch (element.getTagName()) {
                                    case "AccessedAt":
                                        runtimeInfo.setAccessedAt(Instant.parse(element.getFirstChild().getNodeValue()));
                                        break;
                                    case "CreatedAt":
                                        runtimeInfo.setCreatedAt(Instant.parse(element.getFirstChild().getNodeValue()));
                                        break;
                                    case "UpdatedAt":
                                        runtimeInfo.setUpdatedAt(Instant.parse(element.getFirstChild().getNodeValue()));
                                        break;
                                    case "MessageCount":
                                        runtimeInfo.setMessageCount(Long.parseLong(element.getFirstChild().getNodeValue()));
                                        break;
                                    case "CountDetails":
                                        runtimeInfo.setMessageCountDetails(new MessageCountDetails());
                                        NodeList mcDetails = element.getChildNodes();
                                        for (int k = 0; k < mcDetails.getLength(); k++) {
                                            Node node2 = mcDetails.item(k);
                                            if (node2.getNodeType() == Node.ELEMENT_NODE) {
                                                element = (Element) node2;
                                                String localName = element.getTagName().substring(element.getTagName().indexOf(':') + 1);
                                                switch (localName) {
                                                    case "ActiveMessageCount":
                                                        runtimeInfo.getMessageCountDetails().setActiveMessageCount(Long.parseLong(element.getFirstChild().getNodeValue()));
                                                        break;
                                                    case "DeadLetterMessageCount":
                                                        runtimeInfo.getMessageCountDetails().setDeadLetterMessageCount(Long.parseLong(element.getFirstChild().getNodeValue()));
                                                        break;
                                                    case "ScheduledMessageCount":
                                                        runtimeInfo.getMessageCountDetails().setScheduledMessageCount(Long.parseLong(element.getFirstChild().getNodeValue()));
                                                        break;
                                                    case "TransferMessageCount":
                                                        runtimeInfo.getMessageCountDetails().setTransferMessageCount(Long.parseLong(element.getFirstChild().getNodeValue()));
                                                        break;
                                                    case "TransferDeadLetterMessageCount":
                                                        runtimeInfo.getMessageCountDetails().setTransferDeadLetterMessageCount(Long.parseLong(element.getFirstChild().getNodeValue()));
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
                        break;
                    default:
                        break;
                }
            }
        }

        return runtimeInfo;
    }
}
