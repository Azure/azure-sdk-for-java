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
 * UUIDClock is used by UUIDTimer to get the current time. The default
 * implementation returns the time from the system clock, but this class can
 * be overriden to return any number. This is useful when UUIDs with past or
 * future timestamps should be generated, or when UUIDs must be generated in
 * a reproducible manner.
 *
 * @since 3.3
 */
public class UUIDClock
{
    /**
     * Returns the current time in milliseconds.
     */
    public long currentTimeMillis()
    {
        return System.currentTimeMillis();
    }
}
