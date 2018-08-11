package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.primitives.MessagingEntityNotFoundException;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.rules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
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
import java.util.ArrayList;
import java.util.List;

class RuleDescriptionSerializer {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(RuleDescriptionSerializer.class);

    static String serialize(RuleDescription ruleDescription) throws ServiceBusException {
        DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
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
        contentElement.appendChild(serializeRule(doc, ruleDescription, "RuleDescription"));

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

    static Element serializeRule(Document doc, RuleDescription ruleDescription, String rootName) throws ServiceBusException {
        Element rdElement = doc.createElementNS(ManagementClientConstants.SB_NS, rootName);

        if (ruleDescription.getFilter() != null) {
            rdElement.appendChild(serializeFilter(doc, ruleDescription.getFilter()));
        }

        if (ruleDescription.getAction() != null) {
            rdElement.appendChild(serializeRuleAction(doc, ruleDescription.getAction()));
        }

        if (ruleDescription.getName() != null) {
            rdElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "Name")
                            .appendChild(doc.createTextNode(ruleDescription.getName())).getParentNode());
        }

        return rdElement;
    }

    private static Element serializeFilter(Document doc, Filter filter) throws ServiceBusException {
        if (filter instanceof TrueFilter) {
            return serializeSqlFilter(doc, (SqlFilter)filter, "TrueFilter");
        } else if (filter instanceof FalseFilter) {
            return serializeSqlFilter(doc, (SqlFilter)filter, "FalseFilter");
        } else if (filter instanceof SqlFilter) {
            return serializeSqlFilter(doc, (SqlFilter)filter, "SqlFilter");
        } else if (filter instanceof CorrelationFilter) {
            return serializeCorrelationFilter(doc, (CorrelationFilter)filter);
        }

        return null;
    }

    private static Element serializeSqlFilter(Document doc, SqlFilter filter, String filterName) {
        Element filterElement = doc.createElementNS(ManagementClientConstants.SB_NS, "Filter");
        filterElement.setAttributeNS(ManagementClientConstants.XML_SCHEMA_INSTANCE_NS, "type", filterName);
        filterElement.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "SqlExpression")
                        .appendChild(doc.createTextNode(filter.getSqlExpression())).getParentNode());

        return filterElement;
    }

    private static Element serializeCorrelationFilter(Document doc, CorrelationFilter filter) throws ServiceBusException {
        if (filter.getProperties() != null) {
            throw new ServiceBusException(false, new UnsupportedOperationException("Correlation rules with custom properties " +
                    "is not yet implemented with ManagementClient"));
        }

        Element filterElement = doc.createElementNS(ManagementClientConstants.SB_NS, "Filter");
        filterElement.setAttributeNS(ManagementClientConstants.XML_SCHEMA_INSTANCE_NS, "type", "CorrelationFilter");

        if (filter.getCorrelationId() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "CorrelationId")
                            .appendChild(doc.createTextNode(filter.getCorrelationId())).getParentNode());
        }

        if (filter.getMessageId() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "MessageId")
                            .appendChild(doc.createTextNode(filter.getMessageId())).getParentNode());
        }

        if (filter.getTo() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "To")
                            .appendChild(doc.createTextNode(filter.getTo())).getParentNode());
        }

        if (filter.getReplyTo() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "ReplyTo")
                            .appendChild(doc.createTextNode(filter.getReplyTo())).getParentNode());
        }

        if (filter.getLabel() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "Label")
                            .appendChild(doc.createTextNode(filter.getLabel())).getParentNode());
        }

        if (filter.getSessionId() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "SessionId")
                            .appendChild(doc.createTextNode(filter.getSessionId())).getParentNode());
        }

        if (filter.getReplyToSessionId() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "ReplyToSessionId")
                            .appendChild(doc.createTextNode(filter.getReplyToSessionId())).getParentNode());
        }

        if (filter.getContentType() != null) {
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "ContentType")
                            .appendChild(doc.createTextNode(filter.getContentType())).getParentNode());
        }

        // todo: serialize custom parameters.
        return filterElement;
    }

    private static Element serializeRuleAction(Document doc, RuleAction ruleAction) {
        if (ruleAction instanceof SqlRuleAction) {
            Element filterElement = doc.createElementNS(ManagementClientConstants.SB_NS, "Action");
            filterElement.setAttributeNS(ManagementClientConstants.XML_SCHEMA_INSTANCE_NS, "type", "SqlRuleAction");
            filterElement.appendChild(
                    doc.createElementNS(ManagementClientConstants.SB_NS, "SqlExpression")
                            .appendChild(doc.createTextNode(((SqlRuleAction) ruleAction).getSqlExpression())).getParentNode());

            return filterElement;
        } else {
            throw new UnsupportedOperationException("Rule action of type '" + ruleAction.getClass().getName() + "' is not implemented");
        }
    }

    static List<RuleDescription> parseCollectionFromContent(String xml) {
        ArrayList<RuleDescription> ruleList = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            NodeList entries = doc.getChildNodes();
            for (int i = 0; i < entries.getLength(); i++) {
                Node node = entries.item(i);
                if (node.getNodeName().equals("entry")) {
                    ruleList.add(parseFromEntry(node));
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.error("Exception while parsing response.", e);
            }

            if (TRACE_LOGGER.isDebugEnabled()) {
                TRACE_LOGGER.debug("XML which failed to parse: \n %s", xml);
            }
        }

        return ruleList;
    }

    static RuleDescription parseFromContent(String xml) throws MessagingEntityNotFoundException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));
            Element doc = dom.getDocumentElement();
            doc.normalize();
            if (doc.getTagName() == "entry")
                return parseFromEntry(doc);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.error("Exception while parsing response.", e);
            }

            if (TRACE_LOGGER.isDebugEnabled()) {
                TRACE_LOGGER.debug("XML which failed to parse: \n %s", xml);
            }
        }

        throw new MessagingEntityNotFoundException("Rule was not found");
    }

    private static RuleDescription parseFromEntry(Node xEntry) {
        RuleDescription rd = null;
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                switch(element.getTagName())
                {
                    case "title":
                        rd = new RuleDescription(element.getFirstChild().getNodeValue());
                        break;
                    case "content":
                        NodeList qdNodes = element.getFirstChild().getChildNodes();
                        for (int j = 0; j < qdNodes.getLength(); j++)
                        {
                            node = qdNodes.item(j);
                            if (node.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) node;
                                switch (element.getTagName())
                                {
                                    case "Name":
                                        rd.setName(element.getFirstChild().getNodeValue());
                                        break;
                                    case "Filter":
                                        rd.setFilter(parseFilterFromElement(element));
                                        break;
                                    case "Action":
                                        rd.setAction(parseActionFromElement(element));
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        }

        return rd;
    }

    private static Filter parseFilterFromElement(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        String type = null;
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attributeNode = attributes.item(i);
            if (attributeNode.getNodeName().endsWith("type")) {
                type = attributeNode.getNodeValue();
                break;
            }
        }

        if (type == null) {
            return null;
        }

        switch (type) {
            case "SqlFilter":
                return parseSqlFilterFromElement(element);
            case "CorrelationFilter":
                return parseCorrelationFilterFromElement(element);
            case "TrueFilter":
                return new TrueFilter();
            case "FalseFilter":
                return new FalseFilter();
            default:
                return null;
        }
    }

    private static Filter parseSqlFilterFromElement(Element filterElement) {
        NodeList nList = filterElement.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "SqlExpression":
                        return new SqlFilter(element.getFirstChild().getNodeValue());
                }
            }
        }

        return null;
    }

    private static Filter parseCorrelationFilterFromElement(Element filterElement) {
        CorrelationFilter filter = new CorrelationFilter();
        NodeList nList = filterElement.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "CorrelationId":
                        filter.setCorrelationId(element.getFirstChild().getNodeValue());
                        break;
                    case "MessageId":
                        filter.setMessageId(element.getFirstChild().getNodeValue());
                        break;
                    case "To":
                        filter.setTo(element.getFirstChild().getNodeValue());
                        break;
                    case "ReplyTo":
                        filter.setReplyTo(element.getFirstChild().getNodeValue());
                        break;
                    case "Label":
                        filter.setLabel(element.getFirstChild().getNodeValue());
                        break;
                    case "SessionId":
                        filter.setSessionId(element.getFirstChild().getNodeValue());
                        break;
                    case "ReplyToSessionId":
                        filter.setReplyToSessionId(element.getFirstChild().getNodeValue());
                        break;
                    case "ContentType":
                        filter.setContentType(element.getFirstChild().getNodeValue());
                        break;
                    // todo: parse properties
                }
            }
        }

        return filter;
    }

    private static RuleAction parseActionFromElement(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        String type = null;
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attributeNode = attributes.item(i);
            if (attributeNode.getNodeName().endsWith("type")) {
                type = attributeNode.getNodeValue();
                break;
            }
        }

        if (type == null) {
            return null;
        }

        switch (type) {
            case "SqlRuleAction":
                return parseSqlActionFromElement(element);
            default:
                return null;
        }
    }

    private static RuleAction parseSqlActionFromElement(Element filterElement) {
        NodeList nList = filterElement.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "SqlExpression":
                        return new SqlRuleAction(element.getFirstChild().getNodeValue());
                }
            }
        }

        return null;
    }
}
