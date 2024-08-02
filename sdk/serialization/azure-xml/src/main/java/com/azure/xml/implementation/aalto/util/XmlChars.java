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

package com.azure.xml.implementation.aalto.util;

/**
 * Simple utility class that encapsulates logic of determining validity
 * of characters outside basic 7-bit range of Unicode, for XML 1.0
 */
public final class XmlChars {
    /* We don't need full 64k bits... (0x80 - 0x312C) / 32. But to
     * simplify things, let's just include first 0x80 entries in there etc
     */
    final static int SIZE = (0x3140 >> 5); // 32 bits per int

    final static int[] sXml10StartChars = new int[SIZE];
    static {
        /* 23-May-2008, tatus: Why was colon included? Shouldn't, not
         *   valid within names? (except as part separator)
         */
        //SETBITS(sXml10StartChars, 0x3A); // ':'
        SETBITS(sXml10StartChars, 0x41, 0x5A); // 'A-Z'
        SETBITS(sXml10StartChars, 0x5F); // '_'
        SETBITS(sXml10StartChars, 0x61, 0x7A); // 'a-z'

        SETBITS(sXml10StartChars, 0xC0, 0xD6);
        SETBITS(sXml10StartChars, 0xD8, 0xF6);
        SETBITS(sXml10StartChars, 0xF8, 0xFF);
        SETBITS(sXml10StartChars, 0x100, 0x131);
        SETBITS(sXml10StartChars, 0x134, 0x13e);
        SETBITS(sXml10StartChars, 0x141, 0x148);
        SETBITS(sXml10StartChars, 0x14a, 0x17e);
        SETBITS(sXml10StartChars, 0x180, 0x1c3);
        SETBITS(sXml10StartChars, 0x1cd, 0x1f0);
        SETBITS(sXml10StartChars, 0x1f4, 0x1f5);
        SETBITS(sXml10StartChars, 0x1fa, 0x217);
        SETBITS(sXml10StartChars, 0x250, 0x2a8);
        SETBITS(sXml10StartChars, 0x2bb, 0x2c1);
        SETBITS(sXml10StartChars, 0x386);
        SETBITS(sXml10StartChars, 0x388, 0x38a);
        SETBITS(sXml10StartChars, 0x38c);
        SETBITS(sXml10StartChars, 0x38e, 0x3a1);
        SETBITS(sXml10StartChars, 0x3a3, 0x3ce);
        SETBITS(sXml10StartChars, 0x3d0, 0x3d6);
        SETBITS(sXml10StartChars, 0x3da);
        SETBITS(sXml10StartChars, 0x3dc);
        SETBITS(sXml10StartChars, 0x3de);
        SETBITS(sXml10StartChars, 0x3e0);
        SETBITS(sXml10StartChars, 0x3e2, 0x3f3);
        SETBITS(sXml10StartChars, 0x401, 0x40c);
        SETBITS(sXml10StartChars, 0x40e, 0x44f);
        SETBITS(sXml10StartChars, 0x451, 0x45c);
        SETBITS(sXml10StartChars, 0x45e, 0x481);
        SETBITS(sXml10StartChars, 0x490, 0x4c4);
        SETBITS(sXml10StartChars, 0x4c7, 0x4c8);
        SETBITS(sXml10StartChars, 0x4cb, 0x4cc);
        SETBITS(sXml10StartChars, 0x4d0, 0x4eb);
        SETBITS(sXml10StartChars, 0x4ee, 0x4f5);
        SETBITS(sXml10StartChars, 0x4f8, 0x4f9);

        SETBITS(sXml10StartChars, 0x531, 0x556);
        SETBITS(sXml10StartChars, 0x559);
        SETBITS(sXml10StartChars, 0x561, 0x586);
        SETBITS(sXml10StartChars, 0x5d0, 0x5ea);
        SETBITS(sXml10StartChars, 0x5f0, 0x5f2);
        SETBITS(sXml10StartChars, 0x621, 0x63a);
        SETBITS(sXml10StartChars, 0x641, 0x64a);
        SETBITS(sXml10StartChars, 0x671, 0x6b7);
        SETBITS(sXml10StartChars, 0x6ba, 0x6be);
        SETBITS(sXml10StartChars, 0x6c0, 0x6ce);
        SETBITS(sXml10StartChars, 0x6d0, 0x6d3);
        SETBITS(sXml10StartChars, 0x6d5);

        SETBITS(sXml10StartChars, 0x6e5, 0x6e6);
        SETBITS(sXml10StartChars, 0x905, 0x939);
        SETBITS(sXml10StartChars, 0x93d);
        SETBITS(sXml10StartChars, 0x958, 0x961);
        SETBITS(sXml10StartChars, 0x985, 0x98c);
        SETBITS(sXml10StartChars, 0x98f, 0x990);
        SETBITS(sXml10StartChars, 0x993, 0x9a8);
        SETBITS(sXml10StartChars, 0x9aa, 0x9b0);
        SETBITS(sXml10StartChars, 0x9b2);
        SETBITS(sXml10StartChars, 0x9b6, 0x9b9);
        SETBITS(sXml10StartChars, 0x9dc);
        SETBITS(sXml10StartChars, 0x9dd);
        SETBITS(sXml10StartChars, 0x9df, 0x9e1);
        SETBITS(sXml10StartChars, 0x9f0);
        SETBITS(sXml10StartChars, 0x9f1);
        SETBITS(sXml10StartChars, 0xA05, 0xA0A);
        SETBITS(sXml10StartChars, 0xA0F);
        SETBITS(sXml10StartChars, 0xA10);
        SETBITS(sXml10StartChars, 0xA13, 0xA28);
        SETBITS(sXml10StartChars, 0xA2A, 0xA30);
        SETBITS(sXml10StartChars, 0xA32);
        SETBITS(sXml10StartChars, 0xA33);
        SETBITS(sXml10StartChars, 0xA35);
        SETBITS(sXml10StartChars, 0xA36);
        SETBITS(sXml10StartChars, 0xA38);
        SETBITS(sXml10StartChars, 0xA39);
        SETBITS(sXml10StartChars, 0xA59, 0xA5C);
        SETBITS(sXml10StartChars, 0xA5E);
        SETBITS(sXml10StartChars, 0xA72, 0xA74);
        SETBITS(sXml10StartChars, 0xA85, 0xA8B);
        SETBITS(sXml10StartChars, 0xA8D);
        SETBITS(sXml10StartChars, 0xA8F, 0xA91);
        SETBITS(sXml10StartChars, 0xA93, 0xAA8);
        SETBITS(sXml10StartChars, 0xAAA, 0xAB0);
        SETBITS(sXml10StartChars, 0xAB2, 0xAB3);
        SETBITS(sXml10StartChars, 0xAB5, 0xAB9);
        SETBITS(sXml10StartChars, 0xABD);
        SETBITS(sXml10StartChars, 0xAE0);
        SETBITS(sXml10StartChars, 0xB05, 0xB0C);
        SETBITS(sXml10StartChars, 0xB0F);
        SETBITS(sXml10StartChars, 0xB10);
        SETBITS(sXml10StartChars, 0xB13, 0xB28);

        SETBITS(sXml10StartChars, 0xB2A, 0xB30);
        SETBITS(sXml10StartChars, 0xB32);
        SETBITS(sXml10StartChars, 0xB33);
        SETBITS(sXml10StartChars, 0xB36, 0xB39);
        SETBITS(sXml10StartChars, 0xB3D);
        SETBITS(sXml10StartChars, 0xB5C);
        SETBITS(sXml10StartChars, 0xB5D);
        SETBITS(sXml10StartChars, 0xB5F, 0xB61);
        SETBITS(sXml10StartChars, 0xB85, 0xB8A);
        SETBITS(sXml10StartChars, 0xB8E, 0xB90);

        SETBITS(sXml10StartChars, 0xB92, 0xB95);
        SETBITS(sXml10StartChars, 0xB99, 0xB9A);
        SETBITS(sXml10StartChars, 0xB9C);
        SETBITS(sXml10StartChars, 0xB9E);
        SETBITS(sXml10StartChars, 0xB9F);
        SETBITS(sXml10StartChars, 0xBA3);
        SETBITS(sXml10StartChars, 0xBA4);
        SETBITS(sXml10StartChars, 0xBA8, 0xBAA);
        SETBITS(sXml10StartChars, 0xBAE, 0xBB5);
        SETBITS(sXml10StartChars, 0xBB7, 0xBB9);
        SETBITS(sXml10StartChars, 0xC05, 0xC0C);
        SETBITS(sXml10StartChars, 0xC0E, 0xC10);

        SETBITS(sXml10StartChars, 0xC12, 0xC28);
        SETBITS(sXml10StartChars, 0xC2A, 0xC33);
        SETBITS(sXml10StartChars, 0xC35, 0xC39);
        SETBITS(sXml10StartChars, 0xC60);
        SETBITS(sXml10StartChars, 0xC61);
        SETBITS(sXml10StartChars, 0xC85, 0xC8C);
        SETBITS(sXml10StartChars, 0xC8E, 0xC90);
        SETBITS(sXml10StartChars, 0xC92, 0xCA8);
        SETBITS(sXml10StartChars, 0xCAA, 0xCB3);
        SETBITS(sXml10StartChars, 0xCB5, 0xCB9);
        SETBITS(sXml10StartChars, 0xCDE);
        SETBITS(sXml10StartChars, 0xCE0);
        SETBITS(sXml10StartChars, 0xCE1);
        SETBITS(sXml10StartChars, 0xD05, 0xD0C);
        SETBITS(sXml10StartChars, 0xD0E, 0xD10);
        SETBITS(sXml10StartChars, 0xD12, 0xD28);
        SETBITS(sXml10StartChars, 0xD2A, 0xD39);
        SETBITS(sXml10StartChars, 0xD60);
        SETBITS(sXml10StartChars, 0xD61);
        SETBITS(sXml10StartChars, 0xE01, 0xE2E);
        SETBITS(sXml10StartChars, 0xE30);
        SETBITS(sXml10StartChars, 0xE32);
        SETBITS(sXml10StartChars, 0xE33);
        SETBITS(sXml10StartChars, 0xE40, 0xE45);
        SETBITS(sXml10StartChars, 0xE81);
        SETBITS(sXml10StartChars, 0xE82);
        SETBITS(sXml10StartChars, 0xE84);
        SETBITS(sXml10StartChars, 0xE87);
        SETBITS(sXml10StartChars, 0xE88);
        SETBITS(sXml10StartChars, 0xE8A);
        SETBITS(sXml10StartChars, 0xE8D);
        SETBITS(sXml10StartChars, 0xE94, 0xE97);
        SETBITS(sXml10StartChars, 0xE99, 0xE9F);
        SETBITS(sXml10StartChars, 0xEA1, 0xEA3);
        SETBITS(sXml10StartChars, 0xEA5);
        SETBITS(sXml10StartChars, 0xEA7);
        SETBITS(sXml10StartChars, 0xEAA);
        SETBITS(sXml10StartChars, 0xEAB);
        SETBITS(sXml10StartChars, 0xEAD);
        SETBITS(sXml10StartChars, 0xEAE);
        SETBITS(sXml10StartChars, 0xEB0);
        SETBITS(sXml10StartChars, 0xEB2);
        SETBITS(sXml10StartChars, 0xEB3);
        SETBITS(sXml10StartChars, 0xEBD);

        SETBITS(sXml10StartChars, 0xEC0, 0xEC4);
        SETBITS(sXml10StartChars, 0xF40, 0xF47);
        SETBITS(sXml10StartChars, 0xF49, 0xF69);
        SETBITS(sXml10StartChars, 0x10a0, 0x10c5);
        SETBITS(sXml10StartChars, 0x10d0, 0x10f6);
        SETBITS(sXml10StartChars, 0x1100);
        SETBITS(sXml10StartChars, 0x1102, 0x1103);
        SETBITS(sXml10StartChars, 0x1105, 0x1107);
        SETBITS(sXml10StartChars, 0x1109);
        SETBITS(sXml10StartChars, 0x110b, 0x110c);
        SETBITS(sXml10StartChars, 0x110e, 0x1112);
        SETBITS(sXml10StartChars, 0x113c);
        SETBITS(sXml10StartChars, 0x113e);
        SETBITS(sXml10StartChars, 0x1140);
        SETBITS(sXml10StartChars, 0x114c);
        SETBITS(sXml10StartChars, 0x114e);
        SETBITS(sXml10StartChars, 0x1150);
        SETBITS(sXml10StartChars, 0x1154, 0x1155);
        SETBITS(sXml10StartChars, 0x1159);
        SETBITS(sXml10StartChars, 0x115f, 0x1161);
        SETBITS(sXml10StartChars, 0x1163);
        SETBITS(sXml10StartChars, 0x1165);
        SETBITS(sXml10StartChars, 0x1167);
        SETBITS(sXml10StartChars, 0x1169);
        SETBITS(sXml10StartChars, 0x116d, 0x116e);
        SETBITS(sXml10StartChars, 0x1172, 0x1173);
        SETBITS(sXml10StartChars, 0x1175);
        SETBITS(sXml10StartChars, 0x119e);
        SETBITS(sXml10StartChars, 0x11a8);
        SETBITS(sXml10StartChars, 0x11ab);
        SETBITS(sXml10StartChars, 0x11ae, 0x11af);
        SETBITS(sXml10StartChars, 0x11b7, 0x11b8);
        SETBITS(sXml10StartChars, 0x11ba);
        SETBITS(sXml10StartChars, 0x11bc, 0x11c2);
        SETBITS(sXml10StartChars, 0x11eb);
        SETBITS(sXml10StartChars, 0x11f0);
        SETBITS(sXml10StartChars, 0x11f9);
        SETBITS(sXml10StartChars, 0x1e00, 0x1e9b);
        SETBITS(sXml10StartChars, 0x1ea0, 0x1ef9);
        SETBITS(sXml10StartChars, 0x1f00, 0x1f15);
        SETBITS(sXml10StartChars, 0x1f18, 0x1f1d);
        SETBITS(sXml10StartChars, 0x1f20, 0x1f45);
        SETBITS(sXml10StartChars, 0x1f48, 0x1f4d);
        SETBITS(sXml10StartChars, 0x1f50, 0x1f57);
        SETBITS(sXml10StartChars, 0x1f59);
        SETBITS(sXml10StartChars, 0x1f5b);
        SETBITS(sXml10StartChars, 0x1f5d);
        SETBITS(sXml10StartChars, 0x1f5f, 0x1f7d);
        SETBITS(sXml10StartChars, 0x1f80, 0x1fb4);
        SETBITS(sXml10StartChars, 0x1fb6, 0x1fbc);
        SETBITS(sXml10StartChars, 0x1fbe);
        SETBITS(sXml10StartChars, 0x1fc2, 0x1fc4);
        SETBITS(sXml10StartChars, 0x1fc6, 0x1fcc);
        SETBITS(sXml10StartChars, 0x1fd0, 0x1fd3);
        SETBITS(sXml10StartChars, 0x1fd6, 0x1fdb);
        SETBITS(sXml10StartChars, 0x1fe0, 0x1fec);
        SETBITS(sXml10StartChars, 0x1ff2, 0x1ff4);
        SETBITS(sXml10StartChars, 0x1ff6, 0x1ffc);
        SETBITS(sXml10StartChars, 0x2126);
        SETBITS(sXml10StartChars, 0x212a, 0x212b);
        SETBITS(sXml10StartChars, 0x212e);
        SETBITS(sXml10StartChars, 0x2180, 0x2182);
        SETBITS(sXml10StartChars, 0x3041, 0x3094);
        SETBITS(sXml10StartChars, 0x30a1, 0x30fa);
        SETBITS(sXml10StartChars, 0x3105, 0x312c);
        // note: AC00 - D7A3 handled separately

        // [86] Ideographic (but note: > 0x312c handled separately)
        SETBITS(sXml10StartChars, 0x3007);
        SETBITS(sXml10StartChars, 0x3021, 0x3029);
    }

    final static int[] sXml10Chars = new int[SIZE];
    static {
        // Let's start with all valid start chars:
        System.arraycopy(sXml10StartChars, 0, sXml10Chars, 0, SIZE);

        SETBITS(sXml10Chars, 0x2D, 0x2E); // '-', '.'
        SETBITS(sXml10Chars, 0x30, 0x39); // '0' - '9'
        SETBITS(sXml10Chars, 0xB7);

        // [87] CombiningChar	   ::=
        SETBITS(sXml10Chars, 0x300, 0x345);
        SETBITS(sXml10Chars, 0x360, 0x361);
        SETBITS(sXml10Chars, 0x483, 0x486);
        SETBITS(sXml10Chars, 0x591, 0x5a1);
        SETBITS(sXml10Chars, 0x5a3, 0x5b9);
        SETBITS(sXml10Chars, 0x5bb, 0x5bd);
        SETBITS(sXml10Chars, 0x5bf);

        SETBITS(sXml10Chars, 0x5c1, 0x5c2);
        SETBITS(sXml10Chars, 0x5c4);
        SETBITS(sXml10Chars, 0x64b, 0x652);
        SETBITS(sXml10Chars, 0x670);
        SETBITS(sXml10Chars, 0x6d6, 0x6dc);
        SETBITS(sXml10Chars, 0x6dd, 0x6df);
        SETBITS(sXml10Chars, 0x6e0, 0x6e4);
        SETBITS(sXml10Chars, 0x6e7, 0x6e8);
        SETBITS(sXml10Chars, 0x6ea, 0x6ed);

        SETBITS(sXml10Chars, 0x901, 0x903);
        SETBITS(sXml10Chars, 0x93c);
        SETBITS(sXml10Chars, 0x93e, 0x94c);
        SETBITS(sXml10Chars, 0x94d);
        SETBITS(sXml10Chars, 0x951, 0x954);
        SETBITS(sXml10Chars, 0x962);
        SETBITS(sXml10Chars, 0x963);
        SETBITS(sXml10Chars, 0x981, 0x983);
        SETBITS(sXml10Chars, 0x9bc);
        SETBITS(sXml10Chars, 0x9be);
        SETBITS(sXml10Chars, 0x9bf);
        SETBITS(sXml10Chars, 0x9c0, 0x9c4);
        SETBITS(sXml10Chars, 0x9c7);
        SETBITS(sXml10Chars, 0x9c8);
        SETBITS(sXml10Chars, 0x9cb, 0x9cd);
        SETBITS(sXml10Chars, 0x9d7);
        SETBITS(sXml10Chars, 0x9e2);
        SETBITS(sXml10Chars, 0x9e3);
        SETBITS(sXml10Chars, 0xA02);
        SETBITS(sXml10Chars, 0xA3C);
        SETBITS(sXml10Chars, 0xA3E);
        SETBITS(sXml10Chars, 0xA3F);
        SETBITS(sXml10Chars, 0xA40, 0xA42);
        SETBITS(sXml10Chars, 0xA47);
        SETBITS(sXml10Chars, 0xA48);
        SETBITS(sXml10Chars, 0xA4B, 0xA4D);
        SETBITS(sXml10Chars, 0xA70);
        SETBITS(sXml10Chars, 0xA71);
        SETBITS(sXml10Chars, 0xA81, 0xA83);
        SETBITS(sXml10Chars, 0xABC);
        SETBITS(sXml10Chars, 0xABE, 0xAC5);
        SETBITS(sXml10Chars, 0xAC7, 0xAC9);
        SETBITS(sXml10Chars, 0xACB, 0xACD);
        SETBITS(sXml10Chars, 0xB01, 0xB03);
        SETBITS(sXml10Chars, 0xB3C);
        SETBITS(sXml10Chars, 0xB3E, 0xB43);
        SETBITS(sXml10Chars, 0xB47);
        SETBITS(sXml10Chars, 0xB48);
        SETBITS(sXml10Chars, 0xB4B, 0xB4D);
        SETBITS(sXml10Chars, 0xB56);
        SETBITS(sXml10Chars, 0xB57);
        SETBITS(sXml10Chars, 0xB82);
        SETBITS(sXml10Chars, 0xB83);
        SETBITS(sXml10Chars, 0xBBE, 0xBC2);
        SETBITS(sXml10Chars, 0xBC6, 0xBC8);
        SETBITS(sXml10Chars, 0xBCA, 0xBCD);
        SETBITS(sXml10Chars, 0xBD7);
        SETBITS(sXml10Chars, 0xC01, 0xC03);
        SETBITS(sXml10Chars, 0xC3E, 0xC44);
        SETBITS(sXml10Chars, 0xC46, 0xC48);
        SETBITS(sXml10Chars, 0xC4A, 0xC4D);
        SETBITS(sXml10Chars, 0xC55, 0xC56);
        SETBITS(sXml10Chars, 0xC82, 0xC83);
        SETBITS(sXml10Chars, 0xCBE, 0xCC4);
        SETBITS(sXml10Chars, 0xCC6, 0xCC8);
        SETBITS(sXml10Chars, 0xCCA, 0xCCD);
        SETBITS(sXml10Chars, 0xCD5, 0xCD6);
        SETBITS(sXml10Chars, 0xD02, 0xD03);
        SETBITS(sXml10Chars, 0xD3E, 0xD43);
        SETBITS(sXml10Chars, 0xD46, 0xD48);
        SETBITS(sXml10Chars, 0xD4A, 0xD4D);
        SETBITS(sXml10Chars, 0xD57);
        SETBITS(sXml10Chars, 0xE31);
        SETBITS(sXml10Chars, 0xE34, 0xE3A);
        SETBITS(sXml10Chars, 0xE47, 0xE4E);
        SETBITS(sXml10Chars, 0xEB1);
        SETBITS(sXml10Chars, 0xEB4, 0xEB9);
        SETBITS(sXml10Chars, 0xEBB, 0xEBC);
        SETBITS(sXml10Chars, 0xEC8, 0xECD);
        SETBITS(sXml10Chars, 0xF18, 0xF19);
        SETBITS(sXml10Chars, 0xF35);
        SETBITS(sXml10Chars, 0xF37);
        SETBITS(sXml10Chars, 0xF39);
        SETBITS(sXml10Chars, 0xF3E);
        SETBITS(sXml10Chars, 0xF3F);
        SETBITS(sXml10Chars, 0xF71, 0xF84);
        SETBITS(sXml10Chars, 0xF86, 0xF8B);
        SETBITS(sXml10Chars, 0xF90, 0xF95);
        SETBITS(sXml10Chars, 0xF97);
        SETBITS(sXml10Chars, 0xF99, 0xFAD);
        SETBITS(sXml10Chars, 0xFB1, 0xFB7);
        SETBITS(sXml10Chars, 0xFB9);
        SETBITS(sXml10Chars, 0x20D0, 0x20DC);
        SETBITS(sXml10Chars, 0x20E1);
        SETBITS(sXml10Chars, 0x302A, 0x302F);
        SETBITS(sXml10Chars, 0x3099);
        SETBITS(sXml10Chars, 0x309A);
        // [88] Digit:
        SETBITS(sXml10Chars, 0x660, 0x669);
        SETBITS(sXml10Chars, 0x6f0, 0x6f9);
        SETBITS(sXml10Chars, 0x966, 0x96f);
        SETBITS(sXml10Chars, 0x9e6, 0x9ef);
        SETBITS(sXml10Chars, 0xa66, 0xa6f);
        SETBITS(sXml10Chars, 0xae6, 0xaef);
        SETBITS(sXml10Chars, 0xb66, 0xb6f);
        SETBITS(sXml10Chars, 0xbe7, 0xbef);
        SETBITS(sXml10Chars, 0xc66, 0xc6f);
        SETBITS(sXml10Chars, 0xce6, 0xcef);
        SETBITS(sXml10Chars, 0xd66, 0xd6f);
        SETBITS(sXml10Chars, 0xe50, 0xe59);
        SETBITS(sXml10Chars, 0xed0, 0xed9);
        SETBITS(sXml10Chars, 0xf20, 0xf29);

        // [89] Extender:
        SETBITS(sXml10Chars, 0xb7);
        SETBITS(sXml10Chars, 0x2d0);
        SETBITS(sXml10Chars, 0x2d1);
        SETBITS(sXml10Chars, 0x387);
        SETBITS(sXml10Chars, 0x640);
        SETBITS(sXml10Chars, 0xE46);
        SETBITS(sXml10Chars, 0xEC6);
        SETBITS(sXml10Chars, 0x3005);
        SETBITS(sXml10Chars, 0x3031, 0x3035);
        SETBITS(sXml10Chars, 0x309d, 0x309e);
        SETBITS(sXml10Chars, 0x30fc, 0x30fe);
    }

    private XmlChars() {
    }

    public final static boolean is10NameStartChar(int c) {
        // First, let's deal with outliers
        if (c > 0x312C) { // Most valid chars are below this..
            if (c < 0xAC00) {
                return (c >= 0x4E00 && c <= 0x9FA5); // valid ideograms
            }
            if (c <= 0xD7A3) { // 0xAC00 - 0xD7A3, valid base chars
                return true;
            }
            /* Surrogate chars should not be coming in here; but if they do,
             * they would be illegal... so remaining ones are all invalid
             */
            return false;
        }
        // but then we'll just need to use the table...
        return (sXml10StartChars[c >> 5] & (1 << (c & 31))) != 0;
    }

    public final static boolean is10NameChar(int c) {
        // First, let's deal with outliers
        if (c > 0x312C) { // Most valid chars are below this..
            if (c < 0xAC00) {
                return (c >= 0x4E00 && c <= 0x9FA5); // valid ideograms
            }
            if (c <= 0xD7A3) { // 0xAC00 - 0xD7A3, valid base chars
                return true;
            }
            /* Surrogate chars should not be coming in here; but if they do,
             * they would be illegal... so remaining ones are all invalid
             */
            return false;
        }
        // but then we'll just need to use the table...
        return (sXml10Chars[c >> 5] & (1 << (c & 31))) != 0;
    }

    public final static boolean is11NameStartChar(int c) {
        // Others are checked block-by-block:
        if (c <= 0x2FEF) {
            if (c < 0x300) {
                if (c < 0x00C0) { // 8-bit ctrl chars
                    return false;
                }
                // most of the rest are fine...
                return (c != 0xD7 && c != 0xF7);
            }
            if (c >= 0x2C00) {
                // 0x2C00 - 0x2FEF are ok
                return true;
            }
            if (c < 0x370 || c > 0x218F) {
                // 0x300 - 0x36F, 0x2190 - 0x2BFF invalid
                return false;
            }
            if (c < 0x2000) {
                // 0x370 - 0x37D, 0x37F - 0x1FFF are ok
                return (c != 0x37E);
            }
            if (c >= 0x2070) {
                // 0x2070 - 0x218F are ok (c <= 0x218F already checked)
                return true;
            }
            // And finally, 0x200C - 0x200D
            return (c == 0x200C || c == 0x200D);
        }

        // 0x3000 and above (note: surrogates are not legal at this point)
        if (c >= 0x3001) {
            if (c <= 0xD7FF) {
                return true;
            }
            if (c >= 0xF900) {
                if (c <= 0xFFFD) {
                    /* Check above removes low surrogate (since one can not
                     * START an identifier), and byte-order markers..
                     */
                    return (c <= 0xFDCF || c >= 0xFDF0);
                }
                // There are things from BMP too that are ok with 1.1:
                return (c > 0xFFFF && c <= 0xEFFFF);
            }
        }

        return false;
    }

    public final static boolean is11NameChar(int c) {
        // Others are checked block-by-block:
        if (c <= 0x2FEF) {
            if (c < 0x2000) { // only 8-bit ctrl chars and 0x37E to filter out
                return (c >= 0x00C0 && c != 0x37E) || (c == 0xB7);
            }
            if (c >= 0x2C00) {
                // 0x100 - 0x1FFF, 0x2C00 - 0x2FEF are ok
                return true;
            }
            if (c < 0x200C || c > 0x218F) {
                // 0x2000 - 0x200B, 0x2190 - 0x2BFF invalid
                return false;
            }
            if (c >= 0x2070) {
                // 0x2070 - 0x218F are ok
                return true;
            }
            // And finally, 0x200C - 0x200D, 0x203F - 0x2040 are ok
            return (c == 0x200C || c == 0x200D || c == 0x203F || c == 0x2040);
        }

        // 0x3000 and above: (note: no surrogates accepted at this point)
        if (c >= 0x3001) {
            if (c <= 0xD7FF) {
                return true;
            }
            if (c >= 0xF900) {
                if (c <= 0xFFFD) {
                    /* Check above removes other invalid chars (below valid
                     * range), and byte-order markers (0xFFFE, 0xFFFF).
                     */
                    return (c <= 0xFDCF || c >= 0xFDF0);
                }
                // There are things from BMP too that are ok with 1.1:
                return (c > 0xFFFF && c <= 0xEFFFF);
            }
        }

        return false;
    }

    public static String getCharDesc(int i) {
        char c = (char) i;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code " + i + ")";
        }
        if (i > 255) {
            return "'" + c + "' (code " + i + " / 0x" + Integer.toHexString(i) + ")";
        }
        return "'" + c + "' (code " + i + ")";
    }

    /*
    /**********************************************************************
    /* Internal helper methods
    /**********************************************************************
     */

    private static void SETBITS(int[] array, int start, int end) {
        int bit1 = (start & 31);
        int bit2 = (end & 31);
        start >>= 5;
        end >>= 5;

        /* Ok; this is not perfectly optimal, but should be good enough...
         * we'll only do one-by-one at the ends.
         */
        if (start == end) {
            for (; bit1 <= bit2; ++bit1) {
                array[start] |= (1 << bit1);
            }
        } else {
            for (int bit = bit1; bit <= 31; ++bit) {
                array[start] |= (1 << bit);
            }
            while (++start < end) {
                array[start] = -1;
            }
            for (int bit = 0; bit <= bit2; ++bit) {
                array[end] |= (1 << bit);
            }
        }
    }

    private static void SETBITS(int[] array, int point) {
        int ix = (point >> 5);
        int bit = (point & 31);

        array[ix] |= (1 << bit);
    }
}
