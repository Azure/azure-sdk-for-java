/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

/**
 * An HTTP ETag. An object of this type may be set as a field on an {@link HTTPAccessConditions} object to specify that
 * a request should use ETag conditions.
 */
public final class ETag {

    private final String eTagString;

    /**
     * Used for matching with no ETag.
     */
    public static final ETag NONE = new ETag(null);

    /**
     * Used for matching with any ETag.
     */
    public static final ETag ANY = new ETag("*");

    /**
     * Creates a {@link ETag} object.
     *
     * @param eTagString
     *      The {@code String} to convert to an ETag.
     */
    public ETag(String eTagString) {
        this.eTagString = eTagString;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ETag)) {
            return false;
        }
        if (this.eTagString == null) {
            return ((ETag) obj).eTagString == null;
        }
        return this.eTagString.equals(((ETag)obj).eTagString);
    }

    @Override
    public String toString() {
        if (this.equals(ETag.NONE)) {
            return null;
        }
        return this.eTagString;
    }
}
