// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import com.azure.xml.implementation.aalto.WFCException;

public interface IllegalCharHandler {

    char convertIllegalChar(int invalidChar) throws WFCException;

    public static class ReplacingIllegalCharHandler implements IllegalCharHandler, XmlConsts {

        private final char replacedChar;

        public ReplacingIllegalCharHandler(final char replacedChar) {
            this.replacedChar = replacedChar;
        }

        @Override
        public char convertIllegalChar(int invalidChar) throws WFCException {
            return replacedChar;
        }

    }
}
