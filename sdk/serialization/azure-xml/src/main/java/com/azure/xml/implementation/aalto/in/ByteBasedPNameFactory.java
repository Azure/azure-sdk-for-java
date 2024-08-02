// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
/* Woodstox Lite ("wool") XML processor
 *
 * Copyright (c) 2006- Tatu Saloranta, tatu.saloranta@iki.fi
 *
 * Licensed under the License specified in the file LICENSE which is
 * included with the source code.
 * You may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.xml.implementation.aalto.in;

/**
 * Simple factory that can instantiate appropriate {@link PName}
 * instances, given input data to use for construction. The main reason
 * for a factory class here is just to insulate calling code from having
 * to know details of concrete implementations.
 */
public final class ByteBasedPNameFactory {
    /**
     * Can be set to false for debugging (for example, to test memory
     * usage)
     */
    private final static boolean DO_INTERN = true;
    //private final static boolean DO_INTERN = false;

    /*
    /**********************************************************************
    /* Life-cycle
    /**********************************************************************
     */

    private final static ByteBasedPNameFactory sInstance = new ByteBasedPNameFactory();

    private ByteBasedPNameFactory() {
    }

    public static ByteBasedPNameFactory getInstance() {
        return sInstance;
    }

    /*
    /**********************************************************************
    /* Public API
    /**********************************************************************
     */

    public ByteBasedPName constructPName(int hash, String pname, int colonIx, int quad1, int quad2) {
        if (colonIx < 0) { // no prefix, simpler
            String ln = pname;
            if (DO_INTERN) {
                ln = ln.intern();
            }
            if (quad2 == 0) { // one quad only?
                return new PName1(ln, null, ln, hash, quad1);
            }
            return new PName2(ln, null, ln, hash, quad1, quad2);
        }
        /* !!! TODO: cache prefix intern() calls, since they are bound
         * to cluster nicely (and quite often within same thread too)
         */
        String prefix = pname.substring(0, colonIx);
        String ln = pname.substring(colonIx + 1);
        if (DO_INTERN) {
            prefix = prefix.intern();
            ln = ln.intern();
        }

        if (quad2 == 0) { // one quad only?
            return new PName1(pname, prefix, ln, hash, quad1);
        }
        return new PName2(pname, prefix, ln, hash, quad1, quad2);
    }

    public ByteBasedPName constructPName(int hash, String pname, int colonIx, int[] quads, int qlen) {
        if (qlen < 4) { // Need to check for 3 quad one, can do others too
            if (colonIx < 0) { // no prefix
                if (DO_INTERN) {
                    pname = pname.intern();
                }
                if (qlen == 3) {
                    return new PName3(pname, null, pname, hash, quads);
                } else if (qlen == 2) {
                    return new PName2(pname, null, pname, hash, quads[0], quads[1]);
                }
                return new PName1(pname, null, pname, hash, quads[0]);
            }
            String prefix = pname.substring(0, colonIx);
            String ln = pname.substring(colonIx + 1);
            if (DO_INTERN) {
                ln = ln.intern();
                prefix = prefix.intern();
            }
            if (qlen == 3) {
                return new PName3(pname, prefix, ln, hash, quads);
            } else if (qlen == 2) {
                return new PName2(pname, prefix, ln, hash, quads[0], quads[1]);
            }
            return new PName1(pname, prefix, ln, hash, quads[0]);
        }
        // But also need to copy the incoming int buffer:
        int[] buf = new int[qlen];
        System.arraycopy(quads, 0, buf, 0, qlen);
        if (colonIx < 0) { // no prefix, simpler
            if (DO_INTERN) {
                pname = pname.intern();
            }
            return new PNameN(pname, null, pname, hash, buf, qlen);
        }
        /* !!! TODO: cache prefix intern() calls, since they are bound
         * to cluster nicely (and quite often within same thread too)
         */
        String prefix = pname.substring(0, colonIx);
        String ln = pname.substring(colonIx + 1);
        if (DO_INTERN) {
            ln = ln.intern();
            prefix = prefix.intern();
        }
        return new PNameN(pname, prefix, ln, hash, buf, qlen);
    }
}
