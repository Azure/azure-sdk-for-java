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
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static javax.xml.parsers.DocumentBuilderFactory.newInstance;

class SubscriptionDescriptionSerializer {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SubscriptionDescriptionSerializer.class);

    static String serialize(SubscriptionDescription subscriptionDescription) throws ServiceBusException {
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

        Element sdElement = doc.createElementNS(ManagementClientConstants.SB_NS, "SubscriptionDescription");
        contentElement.appendChild(sdElement);

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "LockDuration")
                        .appendChild(doc.createTextNode(subscriptionDescription.lockDuration.toString())).getParentNode());

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "RequiresSession")
                        .appendChild(doc.createTextNode(Boolean.toString(subscriptionDescription.requiresSession))).getParentNode());

        if (subscriptionDescription.defaultMessageTimeToLive.compareTo(ManagementClientConstants.MAX_DURATION) < 0) {
            sdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "DefaultMessageTimeToLive")
                            .appendChild(doc.createTextNode(subscriptionDescription.defaultMessageTimeToLive.toString())).getParentNode());
        }

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "DeadLetteringOnMessageExpiration")
                        .appendChild(doc.createTextNode(Boolean.toString(subscriptionDescription.enableDeadLetteringOnMessageExpiration))).getParentNode());

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "DeadLetteringOnFilterEvaluationExceptions")
                        .appendChild(doc.createTextNode(Boolean.toString(subscriptionDescription.enableDeadLetteringOnFilterEvaluationException))).getParentNode());

        if (subscriptionDescription.defaultRule != null) {
            sdElement.appendChild(RuleDescriptionSerializer.serializeRule(doc, subscriptionDescription.defaultRule, "DefaultRuleDescription"));
        }

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "MaxDeliveryCount")
                        .appendChild(doc.createTextNode(Integer.toString(subscriptionDescription.maxDeliveryCount))).getParentNode());

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "EnableBatchedOperations")
                        .appendChild(doc.createTextNode(Boolean.toString(subscriptionDescription.enableBatchedOperations))).getParentNode());

        sdElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "Status")
                        .appendChild(doc.createTextNode(subscriptionDescription.status.name())).getParentNode());

        if (subscriptionDescription.forwardTo != null) {
            sdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "ForwardTo")
                            .appendChild(doc.createTextNode(subscriptionDescription.forwardTo)).getParentNode());
        }

        if (subscriptionDescription.userMetadata != null) {
            sdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "UserMetadata")
                            .appendChild(doc.createTextNode(subscriptionDescription.userMetadata)).getParentNode());
        }

        if (subscriptionDescription.autoDeleteOnIdle.compareTo(ManagementClientConstants.MAX_DURATION) < 0) {
            sdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "AutoDeleteOnIdle")
                            .appendChild(doc.createTextNode(subscriptionDescription.autoDeleteOnIdle.toString())).getParentNode());
        }

        if (subscriptionDescription.forwardDeadLetteredMessagesTo != null) {
            sdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "ForwardDeadLetteredMessagesTo")
                            .appendChild(doc.createTextNode(subscriptionDescription.forwardDeadLetteredMessagesTo)).getParentNode());
        }

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

    static List<SubscriptionDescription> parseCollectionFromContent(String topicName, String xml) {
        ArrayList<SubscriptionDescription> subList = new ArrayList<>();
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
                    subList.add(parseFromEntry(topicName, node));
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

        return subList;
    }

    static SubscriptionDescription parseFromContent(String topicName, String xml) throws MessagingEntityNotFoundException {
        try {
            DocumentBuilderFactory dbf = SerializerUtil.getDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            if ("entry".equals(doc.getTagName())) {
                return parseFromEntry(topicName, doc);
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

    private static SubscriptionDescription parseFromEntry(String topicName, Node xEntry) {
        SubscriptionDescription sd = null;
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "title":
                        sd = new SubscriptionDescription(topicName, element.getFirstChild().getNodeValue());
                        break;
                    case "content":
                        NodeList qdNodes = element.getFirstChild().getChildNodes();
                        for (int j = 0; j < qdNodes.getLength(); j++) {
                            node = qdNodes.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) node;
                                switch (element.getTagName()) {
                                    case "RequiresSession":
                                        sd.requiresSession = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "DeadLetteringOnMessageExpiration":
                                        sd.enableDeadLetteringOnMessageExpiration = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "DeadLetteringOnFilterEvaluationExceptions":
                                        sd.enableDeadLetteringOnFilterEvaluationException = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "LockDuration":
                                        sd.lockDuration = Duration.parse(element.getFirstChild().getNodeValue());
                                        break;
                                    case "DefaultMessageTimeToLive":
                                        sd.defaultMessageTimeToLive = Duration.parse(element.getFirstChild().getNodeValue());
                                        break;
                                    case "MaxDeliveryCount":
                                        sd.maxDeliveryCount = Integer.parseInt(element.getFirstChild().getNodeValue());
                                        break;
                                    case "EnableBatchedOperations":
                                        sd.enableBatchedOperations = Boolean.parseBoolean(element.getFirstChild().getNodeValue());
                                        break;
                                    case "Status":
                                        sd.status = EntityStatus.valueOf(element.getFirstChild().getNodeValue());
                                        break;
                                    case "AutoDeleteOnIdle":
                                        sd.autoDeleteOnIdle = Duration.parse(element.getFirstChild().getNodeValue());
                                        break;
                                    case "UserMetadata":
                                        sd.userMetadata = element.getFirstChild().getNodeValue();
                                        break;
                                    case "ForwardTo":
                                        Node fwd = element.getFirstChild();
                                        if (fwd != null) {
                                            sd.forwardTo = fwd.getNodeValue();
                                        }
                                        break;
                                    case "ForwardDeadLetteredMessagesTo":
                                        Node fwdDlq = element.getFirstChild();
                                        if (fwdDlq != null) {
                                            sd.forwardDeadLetteredMessagesTo = fwdDlq.getNodeValue();
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

        return sd;
    }

    static void normalizeDescription(SubscriptionDescription subscriptionDescription, URI baseAddress) {
        if (subscriptionDescription.getForwardTo() != null) {
            subscriptionDescription.setForwardTo(normalizeForwardToAddress(subscriptionDescription.getForwardTo(), baseAddress));
        }

        if (subscriptionDescription.getForwardDeadLetteredMessagesTo() != null) {
            subscriptionDescription.setForwardDeadLetteredMessagesTo(normalizeForwardToAddress(subscriptionDescription.getForwardDeadLetteredMessagesTo(), baseAddress));
        }
    }

    private static String normalizeForwardToAddress(String forwardTo, URI baseAddress) {
        try {
            new URI(forwardTo);
            return forwardTo;
        } catch (URISyntaxException e) {
            return baseAddress.resolve(forwardTo).toString();
        }
    }
}
