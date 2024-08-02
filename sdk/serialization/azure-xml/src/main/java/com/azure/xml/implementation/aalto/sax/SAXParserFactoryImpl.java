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

package com.azure.xml.implementation.aalto.sax;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.azure.xml.implementation.aalto.AaltoInputProperties;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.azure.xml.implementation.aalto.stax.InputFactoryImpl;

/**
 * This is implementation of the main JAXP SAX factory, and as such
 * acts as the entry point from JAXP.
 *<p>
 * Note: most of the SAX features are not configurable as of yet.
 * However, effort is made to recognize all existing standard features
 * and properties, to allow using code to figure out existing
 * capabilities automatically.
 */
public class SAXParserFactoryImpl extends SAXParserFactory {
    final InputFactoryImpl mStaxFactory;

    public SAXParserFactoryImpl() {
        // defaults should be fine...

        mStaxFactory = new InputFactoryImpl();
    }

    public static SAXParserFactory newInstance() {
        return new SAXParserFactoryImpl();
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        // Standard feature?
        SAXFeature stdFeat = SAXUtil.findStdFeature(name);
        if (stdFeat != null) {
            // fixed?
            Boolean b = SAXUtil.getFixedStdFeatureValue(stdFeat);
            if (b != null) {
                return b.booleanValue();
            }
            // ok, may change:
            switch (stdFeat) {
                case IS_STANDALONE: // read-only, but only during parsing
                    return true;

                default:
            }
        } else {
            if (name.equals(AaltoInputProperties.P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES)) {
                return Boolean.TRUE
                    .equals(mStaxFactory.getProperty(AaltoInputProperties.P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES));
            }
        }

        // nope, not recognized:
        SAXUtil.reportUnknownFeature(name);
        return false; // never gets here
    }

    @Override
    public SAXParser newSAXParser() {
        return new SAXParserImpl(mStaxFactory);
    }

    @Override
    public void setFeature(String name, boolean enabled) throws SAXNotRecognizedException, SAXNotSupportedException {
        // Standard feature?
        SAXFeature stdFeat = SAXUtil.findStdFeature(name);
        if (stdFeat != null) {
            boolean ok;

            switch (stdFeat) {
                case EXTERNAL_GENERAL_ENTITIES:
                    ok = !enabled;
                    break;

                case EXTERNAL_PARAMETER_ENTITIES:
                    ok = !enabled;
                    break;

                case IS_STANDALONE: // read-only...
                    ok = true;
                    break;

                case LEXICAL_HANDLER_PARAMETER_ENTITIES:
                    ok = true; // won't be able to handle PEs, so this is n/a
                    break;

                case NAMESPACES: // can't disable
                    ok = enabled;
                    break;

                case NAMESPACE_PREFIXES: // can't enable:
                    ok = !enabled;
                    break;

                case RESOLVE_DTD_URIS: // n/a for now
                    ok = true;
                    break;

                case STRING_INTERNING:
                    // Can not disable; however, doesn't harm if they try to
                    // do it, so let's not care
                    ok = true;
                    break;

                case UNICODE_NORMALIZATION_CHECKING:
                    // not implemented as of yet, maybe never
                    ok = false;
                    break;

                case USE_ATTRIBUTES2: // read-only
                case USE_LOCATOR2: // read-only
                case USE_ENTITY_RESOLVER2: // read-only
                    // read-only, who cares:
                    ok = true;
                    break;

                case VALIDATION:
                    // validation not yet implemented
                    ok = !enabled;
                    break;

                case XMLNS_URIS:
                    // without disabling ns-processing, irrelevant:
                    ok = true;
                    break;

                case XML_1_1:
                    // not yet implemented
                    ok = !enabled;
                    break;

                default: // should never happen...
                    ok = false;
            }
            if (!ok) {
                throw new SAXNotSupportedException(
                    "Setting std feature " + stdFeat + " to " + enabled + " not supported");
            }
            return;
        } else {
            // [aalto-xml#65]: allow retaining GEs in attribute values
            if (name.equals(AaltoInputProperties.P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES)) {
                mStaxFactory.setProperty(AaltoInputProperties.P_RETAIN_ATTRIBUTE_GENERAL_ENTITIES, enabled);
                return;
            }
        }

        // nope, not recognized:
        SAXUtil.reportUnknownFeature(name);
    }

    @Override
    public void setNamespaceAware(boolean awareness) {
        if (!awareness) {
            throw new IllegalArgumentException("Non-namespace-aware mode not implemented");
        }
        super.setNamespaceAware(awareness);
    }

    @Override
    public void setValidating(boolean value) {
        if (value) {
            throw new IllegalArgumentException("Validating mode not implemented");
        }
        super.setValidating(value);
    }
}
