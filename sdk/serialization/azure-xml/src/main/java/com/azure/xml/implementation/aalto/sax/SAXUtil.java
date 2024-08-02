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

import java.util.*;
import org.xml.sax.SAXNotRecognizedException;

/**
 * Container for utility methods needed by the parser factory, or
 * parser wrapper components.
 */
public final class SAXUtil {
    final static HashMap<String, SAXFeature> sStdFeatures;
    static {
        SAXFeature[] feats = SAXFeature.values();
        sStdFeatures = new HashMap<String, SAXFeature>(feats.length);
        for (SAXFeature feat : feats) {
            sStdFeatures.put(feat.getSuffix(), feat);
        }
    }

    final static HashMap<String, SAXProperty> sStdProperties;
    static {
        SAXProperty[] props = SAXProperty.values();
        sStdProperties = new HashMap<String, SAXProperty>(props.length);
        for (SAXProperty prop : props) {
            sStdProperties.put(prop.getSuffix(), prop);
        }
    }

    private SAXUtil() {
    }

    public static SAXFeature findStdFeature(String featURI) {
        if (featURI.startsWith(SAXFeature.STD_FEATURE_PREFIX)) {
            String suffix = featURI.substring(SAXFeature.STD_FEATURE_PREFIX.length());
            return sStdFeatures.get(suffix);
        }
        return null;
    }

    public static SAXProperty findStdProperty(String featURI) {
        if (featURI.startsWith(SAXProperty.STD_PROPERTY_PREFIX)) {
            String suffix = featURI.substring(SAXProperty.STD_PROPERTY_PREFIX.length());
            return sStdProperties.get(suffix);
        }
        return null;
    }

    /**
     * Convenience method that will return value (True/False) for the
     * given standard feature, iff it has fixed (immutable) value for
     * the current implementation. If value is not immutable, returns
     * null
     */
    public static Boolean getFixedStdFeatureValue(SAXFeature stdFeat) {
        switch (stdFeat) {
            case EXTERNAL_GENERAL_ENTITIES: // not yet implemented
                return Boolean.FALSE;

            case EXTERNAL_PARAMETER_ENTITIES: // not yet implemented
                return Boolean.FALSE;

            case IS_STANDALONE: // read-only, but only during parsing
                break;

            case LEXICAL_HANDLER_PARAMETER_ENTITIES:
                // won't be able to handle PEs, so this is n/a
                return Boolean.TRUE;

            case NAMESPACES: // can't disable
                return Boolean.TRUE;

            case NAMESPACE_PREFIXES: // can't enable:
                return Boolean.FALSE;

            case RESOLVE_DTD_URIS: // n/a for now
                return Boolean.FALSE;

            case STRING_INTERNING:
                // Can not disable
                return Boolean.TRUE;

            case UNICODE_NORMALIZATION_CHECKING:
                // not implemented as of yet, maybe never
                return Boolean.FALSE;

            case USE_ATTRIBUTES2: // read-only
            case USE_LOCATOR2: // read-only
            case USE_ENTITY_RESOLVER2: // read-only
                return Boolean.TRUE;

            case VALIDATION:
                // validation not yet implemented
                return Boolean.FALSE;

            case XMLNS_URIS:
                // without disabling ns-processing, irrelevant:
                return Boolean.TRUE;

            case XML_1_1:
                // not yet implemented
                return Boolean.FALSE;
        }

        return null;
    }

    public static void reportUnknownFeature(String name) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException("Feature '" + name + "' not recognized");
    }

    public static void reportUnknownProperty(String name) throws SAXNotRecognizedException {
        throw new SAXNotRecognizedException("Property '" + name + "' not recognized");
    }
}
