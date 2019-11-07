// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.primitives.MessagingEntityNotFoundException;
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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

class TopicDescriptionSerializer {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(TopicDescriptionSerializer.class);

    static String serialize(TopicDescription topicDescription) throws ServiceBusException {
        DocumentBuilder dBuilder;
        try {
            DocumentBuilderFactory dbFactory = SerializerUtil.getDocumentBuilderFactory();
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ServiceBusException(false, e);
        }
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElementNS(ManagementClientConstants.ATOM_NS, "entry");
        doc.appendChild(rootElement);

        Element contentElement = doc.createElementNS(ManagementClientConstants.ATOM_NS, "content");
        rootElement.appendChild(contentElement);
        contentElement.setAttribute("type", "application/xml");

        Element tdElement = doc.createElementNS(ManagementClientConstants.SB_NS, "TopicDescription");
        contentElement.appendChild(tdElement);

        tdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "MaxSizeInMegabytes")
                        .appendChild(doc.createTextNode(Long.toString(topicDescription.maxSizeInMB))).getParentNode());

        tdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "RequiresDuplicateDetection")
                        .appendChild(doc.createTextNode(Boolean.toString(topicDescription.requiresDuplicateDetection))).getParentNode());

        if (topicDescription.defaultMessageTimeToLive.compareTo(ManagementClientConstants.MAX_DURATION) < 0) {
            tdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "DefaultMessageTimeToLive")
                            .appendChild(doc.createTextNode(topicDescription.defaultMessageTimeToLive.toString())).getParentNode());
        }

        if (topicDescription.requiresDuplicateDetection && topicDescription.duplicationDetectionHistoryTimeWindow.compareTo(Duration.ZERO) > 0) {
            tdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "DuplicateDetectionHistoryTimeWindow")
                            .appendChild(doc.createTextNode(topicDescription.duplicationDetectionHistoryTimeWindow.toString())).getParentNode());
        }

        tdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "EnableBatchedOperations")
                        .appendChild(doc.createTextNode(Boolean.toString(topicDescription.enableBatchedOperations))).getParentNode());

        if (topicDescription.authorizationRules != null) {
            tdElement.appendChild(AuthorizationRuleSerializer.serializeRules(topicDescription.authorizationRules, doc));
        }

        tdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "Status")
                        .appendChild(doc.createTextNode(topicDescription.status.name())).getParentNode());

        if (topicDescription.userMetadata != null) {
            tdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "UserMetadata")
                            .appendChild(doc.createTextNode(topicDescription.userMetadata)).getParentNode());
        }

        if (topicDescription.autoDeleteOnIdle.compareTo(ManagementClientConstants.MAX_DURATION) < 0) {
            tdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "AutoDeleteOnIdle")
                            .appendChild(doc.createTextNode(topicDescription.autoDeleteOnIdle.toString())).getParentNode());
        }

        tdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "SupportOrdering")
                        .appendChild(doc.createTextNode(Boolean.toString(topicDescription.supportOrdering))).getParentNode());

        tdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "EnablePartitioning")
                        .appendChild(doc.createTextNode(Boolean.toString(topicDescription.enablePartitioning))).getParentNode());

        // Convert dom document to string.
        StringWriter output = new StringWriter();

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(output));
        } catch (TransformerException e) {
            throw new ServiceBusException(false, e);
        }
        return output.toString();
    }

    static List<TopicDescription> parseCollectionFromContent(String xml) {
        ArrayList<TopicDescription> topicList = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = SerializerUtil.getDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            NodeList entries = doc.getChildNodes();
            for (int i = 0; i < entries.getLength(); i++) {
                Node node = entries.item(i);
                if (node.getNodeName().equals("entry")) {
                    topicList.add(parseFromEntry(node));
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.info("Exception while parsing response.", e);
            }

            if (TRACE_LOGGER.isDebugEnabled()) {
                TRACE_LOGGER.debug("XML which failed to parse: \n %s", xml);
            }
        }

        return topicList;
    }

    static TopicDescription parseFromContent(String xml) throws MessagingEntityNotFoundException {
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

        throw new MessagingEntityNotFoundException("Topic was not found");
    }

    private static TopicDescription parseFromEntry(Node xEntry) {
        TopicDescription td = null;
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "title":
                        td = new TopicDescription(element.getFirstChild().getNodeValue());
                        break;
                    case "content":
                        NodeList qdNodes = element.getFirstChild().getChildNodes();
                        for (int j = 0; j < qdNodes.getLength(); j++) {
                            node = qdNodes.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) node;
                                switch (element.getTagName()) {
                                    case "MaxSizeInMegabytes":
                                        td.maxSizeInMB = Long.parseLong(element.getFirstChild().getNodeValue());
                                        break;
                                    case "RequiresDuplicateDetection":
                                        td.requiresDuplicateDetection = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "DuplicateDetectionHistoryTimeWindow":
                                        td.duplicationDetectionHistoryTimeWindow = Duration.parse(element.getFirstChild().getNodeValue());
                                        break;
                                    case "DefaultMessageTimeToLive":
                                        td.defaultMessageTimeToLive = Duration.parse(element.getFirstChild().getNodeValue());
                                        break;
                                    case "EnableBatchedOperations":
                                        td.enableBatchedOperations = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "Status":
                                        td.status = EntityStatus.valueOf(element.getFirstChild().getNodeValue());
                                        break;
                                    case "AutoDeleteOnIdle":
                                        td.autoDeleteOnIdle = Duration.parse(element.getFirstChild().getNodeValue());
                                        break;
                                    case "EnablePartitioning":
                                        td.enablePartitioning = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "UserMetadata":
                                        td.userMetadata = element.getFirstChild().getNodeValue();
                                        break;
                                    case "AuthorizationRules":
                                        td.authorizationRules = AuthorizationRuleSerializer.parseAuthRules(element);
                                        break;
                                    case "SupportOrdering":
                                        td.supportOrdering = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
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

        return td;
    }
}
