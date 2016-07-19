/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network;

/**
 * The possible network protocols supported by Azure.
 */
public enum Protocol {
    /**
     * The TCP protocol.
     */
    TCP(SecurityRuleProtocol.TCP),

    /**
     * The UDP protocol.
     */
    UDP(SecurityRuleProtocol.UDP),

    /**
     * Any protocol.
     */
    ANY(SecurityRuleProtocol.ASTERISK);

    private final String name;
    Protocol(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Converts the string used by Azure into the corresponding constant, if any.
     * @param s the string used by Azure to convert to a constant
     * @return the identified constant, or null if not supported
     */
    public static Protocol fromString(String s) {
        for (Protocol protocol : Protocol.values()) {
            if (protocol.name.equalsIgnoreCase(s)) {
                return protocol;
            }
        }
        return null;
    }
}
