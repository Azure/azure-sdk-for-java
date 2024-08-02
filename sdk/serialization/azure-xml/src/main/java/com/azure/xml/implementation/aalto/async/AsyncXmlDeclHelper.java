// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.async;

import com.azure.xml.implementation.aalto.in.PName;
import com.azure.xml.implementation.aalto.in.PName1;
import com.azure.xml.implementation.aalto.in.PName2;
import com.azure.xml.implementation.aalto.in.PName3;

/**
 * Helper class that contains specialized decode methods for pseudo-attributes contained
 * in XML declaration.
 */
public class AsyncXmlDeclHelper {
    private final static int QUAD_XML = asciiQuads("xml")[0];

    private final static int QUAD_VERSION1, QUAD_VERSION2;
    static {
        int[] q = asciiQuads("version");
        QUAD_VERSION1 = q[0];
        QUAD_VERSION2 = q[1];
    }

    private final static int QUAD_STANDALONE1, QUAD_STANDALONE2, QUAD_STANDALONE3;
    static {
        int[] q = asciiQuads("standalone");
        QUAD_STANDALONE1 = q[0];
        QUAD_STANDALONE2 = q[1];
        QUAD_STANDALONE3 = q[2];
    }

    private final static int QUAD_ENCODING1, QUAD_ENCODING2;
    static {
        int[] q = asciiQuads("encoding");
        QUAD_ENCODING1 = q[0];
        QUAD_ENCODING2 = q[1];
    }

    // // // Actual pre-defined PNames

    private final static PName1 NAME_XML = new PName1("xml", null, "xml", 0, QUAD_XML);
    private final static PName2 NAME_VERSION = new PName2("version", null, "version", 0, QUAD_VERSION1, QUAD_VERSION2);
    private final static PName3 NAME_STANDALONE
        = new PName3("standalone", null, "standalone", 0, QUAD_STANDALONE1, QUAD_STANDALONE2, QUAD_STANDALONE3);
    private final static PName2 NAME_ENCODING
        = new PName2("encoding", null, "encoding", 0, QUAD_ENCODING1, QUAD_ENCODING2);

    public static PName find(int quad1) {
        if (quad1 == QUAD_XML) {
            return NAME_XML;
        }
        return null;
    }

    public static PName find(int quad1, int quad2) {
        if (quad1 == QUAD_VERSION1) {
            if (quad2 == QUAD_VERSION2) {
                return NAME_VERSION;
            }
        } else if (quad1 == QUAD_ENCODING1) {
            if (quad2 == QUAD_ENCODING2) {
                return NAME_ENCODING;
            }
        }
        return null;
    }

    public static PName find(int quad1, int quad2, int quad3) {
        if (quad1 == QUAD_STANDALONE1) {
            if (quad2 == QUAD_STANDALONE2) {
                if (quad3 == QUAD_STANDALONE3) {
                    return NAME_STANDALONE;
                }
            }
        }
        return null;
    }

    static int[] asciiQuads(String word) {
        int blen = word.length();
        int[] result = new int[(blen + 3) / 4];
        for (int i = 0; i < blen; ++i) {
            int x = word.charAt(i);

            if (++i < blen) {
                x = (x << 8) | word.charAt(i);
                if (++i < blen) {
                    x = (x << 8) | word.charAt(i);
                    if (++i < blen) {
                        x = (x << 8) | word.charAt(i);
                    }
                }
            }
            result[i / 4] = x;
        }
        return result;
    }
}
