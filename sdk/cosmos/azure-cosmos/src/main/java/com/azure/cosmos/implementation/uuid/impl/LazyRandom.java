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

package com.azure.cosmos.implementation.uuid.impl;

import java.security.SecureRandom;

/**
 * Trivial helper class that uses class loading as synchronization
 * mechanism for lazy instantiation of the shared secure random
 * instance.
 */
public final class LazyRandom
{
    private final static SecureRandom shared = new SecureRandom();

    public static SecureRandom sharedSecureRandom() {
        return shared;
    }
}
