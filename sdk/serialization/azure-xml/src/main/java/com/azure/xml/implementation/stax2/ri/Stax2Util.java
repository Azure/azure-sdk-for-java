// Original file from https://github.com/FasterXML/stax2-api under BSD 2-Clause "Simplified" License
/* Stax2 API extension for Streaming Api for Xml processing (StAX).
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

package com.azure.xml.implementation.stax2.ri;

import javax.xml.stream.XMLStreamConstants;

public final class Stax2Util implements XMLStreamConstants {
    private Stax2Util() {
    } // no instantiation

    /**
     * Method that converts given standard Stax event type into
     * textual representation.
     */
    public static String eventTypeDesc(int type) {
        switch (type) {
            case START_ELEMENT:
                return "START_ELEMENT";

            case END_ELEMENT:
                return "END_ELEMENT";

            case START_DOCUMENT:
                return "START_DOCUMENT";

            case END_DOCUMENT:
                return "END_DOCUMENT";

            case CHARACTERS:
                return "CHARACTERS";

            case CDATA:
                return "CDATA";

            case SPACE:
                return "SPACE";

            case COMMENT:
                return "COMMENT";

            case PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";

            case DTD:
                return "DTD";

            case ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
        }
        return "[" + type + "]";
    }

}
