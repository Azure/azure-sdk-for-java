package com.microsoft.azure.servicebus.management;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.w3c.dom.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuthorizationRuleSerializer {
    static Element serializeRules(List<AuthorizationRule> authorizationRules, Document doc) throws ServiceBusException {
        if (authorizationRules == null) {
            return null;
        }

        Element rules = doc.createElementNS(ManagementClientConstants.SB_NS, "AuthorizationRules");
        for (AuthorizationRule rule : authorizationRules) {
            rules.appendChild(serializeRule(rule, doc));
        }

        return rules;
    }

    private static Element serializeRule(AuthorizationRule authRule, Document doc) {
        if (authRule instanceof SharedAccessAuthorizationRule) {
            return serializeSasRule((SharedAccessAuthorizationRule) authRule, doc);
        }

        return null;
    }

    private static Element serializeSasRule(SharedAccessAuthorizationRule sasRule, Document doc) {
        Element authRule = doc.createElementNS(ManagementClientConstants.SB_NS, "AuthorizationRule");
        authRule.setAttributeNS(ManagementClientConstants.XML_SCHEMA_INSTANCE_NS, "type", "SharedAccessAuthorizationRule");

        authRule.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "ClaimType")
                        .appendChild(doc.createTextNode(sasRule.getClaimType())).getParentNode());

        authRule.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "ClaimValue")
                        .appendChild(doc.createTextNode(sasRule.getClaimValue())).getParentNode());

        if (sasRule.getRights() != null) {
            Element rights = doc.createElementNS(ManagementClientConstants.SB_NS, "Rights");
            authRule.appendChild(rights);
            for (AccessRights right : sasRule.getRights()) {
                rights.appendChild(
                        doc.createElementNS(ManagementClientConstants.SB_NS, "AccessRights")
                                .appendChild(doc.createTextNode(right.name())).getParentNode());
            }
        }

        authRule.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "KeyName")
                        .appendChild(doc.createTextNode(sasRule.getKeyName())).getParentNode());

        authRule.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "PrimaryKey")
                        .appendChild(doc.createTextNode(sasRule.getPrimaryKey())).getParentNode());

        authRule.appendChild(
                doc.createElementNS(ManagementClientConstants.SB_NS, "SecondaryKey")
                        .appendChild(doc.createTextNode(sasRule.getSecondaryKey())).getParentNode());

        return authRule;
    }

    static List<AuthorizationRule> parseAuthRules(Element xEntry) {
        ArrayList<AuthorizationRule> rules = null;
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && ((Element) node).getTagName().equalsIgnoreCase("AuthorizationRule")) {
                if (rules == null) {
                    rules = new ArrayList<>();
                }
                rules.add(parseAuthRule((Element) node));
            }
        }

        return rules;
    }

    private static AuthorizationRule parseAuthRule(Element xEntry) {
        NamedNodeMap attributes = xEntry.getAttributes();
        if (attributes == null || attributes.getLength() < 1) {
            return null;
        }

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
            case "SharedAccessAuthorizationRule":
                return parseSasAuthRule(xEntry);
            default:
                return null;
        }
    }

    private static AuthorizationRule parseSasAuthRule(Element xEntry) {
        SharedAccessAuthorizationRule rule = new SharedAccessAuthorizationRule();
        NodeList nList = xEntry.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                switch (element.getTagName()) {
                    case "CreatedTime":
                        rule.setCreatedTime(Instant.parse(element.getFirstChild().getNodeValue()));
                        break;
                    case "ModifiedTime":
                        rule.setModifiedTime(Instant.parse(element.getFirstChild().getNodeValue()));
                        break;
                    case "KeyName":
                        rule.setKeyName(element.getFirstChild().getNodeValue());
                        break;
                    case "PrimaryKey":
                        rule.setPrimaryKey(element.getFirstChild().getNodeValue());
                        break;
                    case "SecondaryKey":
                        rule.setSecondaryKey(element.getFirstChild().getNodeValue());
                        break;
                    case "Rights":
                        ArrayList<AccessRights> rights = new ArrayList<>();
                        NodeList rightsList = element.getChildNodes();
                        for (int j = 0; j < rightsList.getLength(); j++) {
                            Node rightNode = rightsList.item(j);
                            if (rightNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element rightElement = (Element) rightNode;
                                rights.add(AccessRights.valueOf(rightElement.getFirstChild().getNodeValue()));
                            }
                        }
                        rule.setRights(rights);
                        break;
                }
            }
        }

        return rule;
    }

    static boolean equals(List<AuthorizationRule> first, List<AuthorizationRule> second) {
        if ((first == null && second != null) || (first != null && second == null)) {
            return false;
        }

        if (first == null) {
            return true;
        }

        if (first.size() != second.size()) {
            return false;
        }

        HashMap<String, AuthorizationRule> cnt = new HashMap<>();
        for (AuthorizationRule rule : first)
        {
            cnt.put(rule.getKeyName(), rule);
        }

        for (AuthorizationRule otherRule : second)
        {
            AuthorizationRule firstRule = cnt.get(otherRule.getKeyName());
            if (firstRule == null || !firstRule.equals(otherRule)) {
                return false;
            }
        }

        return true;
    }
}
