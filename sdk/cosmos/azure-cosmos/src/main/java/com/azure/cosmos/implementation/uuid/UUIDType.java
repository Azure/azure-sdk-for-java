/* JUG Java Uuid Generator
 *
 * Copyright (c) 2002- Tatu Saloranta, tatu.saloranta@iki.fi
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

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.uuid;

/**
 * Enumeration of different flavors of UUIDs: 5 specified by specs
 * (<a href="http://tools.ietf.org/html/rfc4122">RFC-4122</a>)
 * and one
 * virtual entry ("UNKNOWN") to represent invalid one that consists of
 * all zero bites
 */
public enum UUIDType {
    TIME_BASED(1),
    DCE(2),
    NAME_BASED_MD5(3),
    RANDOM_BASED(4),
    NAME_BASED_SHA1(5),
    UNKNOWN(0)
    ;

    private final int _raw;
	
    private UUIDType(int raw) {
       _raw = raw;
    }

    /**
     * Returns "raw" type constants, embedded within UUID bytes.
     */
    public int raw() { return _raw; }
}
