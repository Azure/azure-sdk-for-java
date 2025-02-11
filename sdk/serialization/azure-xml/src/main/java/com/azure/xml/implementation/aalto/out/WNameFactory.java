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

package com.azure.xml.implementation.aalto.out;

import javax.xml.stream.XMLStreamException;

/**
 * This abstract class defines factory object (with factory methods)
 * that are needed by {@link WNameTable} instances, to be able to
 * construct {@link WName} instances for element and attribute names.
 */
public abstract class WNameFactory {
    public abstract WName constructName(String localName) throws XMLStreamException;

    public abstract WName constructName(String prefix, String localName) throws XMLStreamException;
}
