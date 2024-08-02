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

public final class EntityNames {
    public final static PNameC ENTITY_AMP = PNameC.construct("amp");
    public final static PNameC ENTITY_APOS = PNameC.construct("apos");
    public final static PNameC ENTITY_GT = PNameC.construct("gt");
    public final static PNameC ENTITY_LT = PNameC.construct("lt");
    public final static PNameC ENTITY_QUOT = PNameC.construct("quot");

    public final static int ENTITY_AMP_QUAD;
    public final static int ENTITY_APOS_QUAD;
    public final static int ENTITY_GT_QUAD;
    public final static int ENTITY_LT_QUAD;
    public final static int ENTITY_QUOT_QUAD;
    static {
        ENTITY_AMP_QUAD = ('a' << 16) | ('m' << 8) | 'p';
        ENTITY_APOS_QUAD = ('a' << 24) | ('p' << 16) | ('o' << 8) | 's';
        ENTITY_GT_QUAD = ('g' << 8) | 't';
        ENTITY_LT_QUAD = ('l' << 8) | 't';
        ENTITY_QUOT_QUAD = ('q' << 24) | ('u' << 16) | ('o' << 8) | 't';
    }

    private EntityNames() {
    }
}
