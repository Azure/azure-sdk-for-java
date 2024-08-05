// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import com.azure.xml.implementation.aalto.WFCException;

public interface IllegalCharHandler {

    char convertIllegalChar(int invalidChar) throws WFCException;
}
