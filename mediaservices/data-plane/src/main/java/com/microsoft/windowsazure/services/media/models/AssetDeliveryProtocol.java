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

package com.microsoft.windowsazure.services.media.models;

import java.util.EnumSet;

/**
 *
 * Specifies the protocol of an AssetDeliveryPolicy.
 *
 */
public enum AssetDeliveryProtocol {

    /** No protocols. */
    None(0),
    /** Smooth streaming protocol. */
    SmoothStreaming (1),
    /** MPEG Dynamic Adaptive Streaming over HTTP (DASH). */
    Dash(2),
    /** Apple HTTP Live Streaming protocol. */
    HLS(4),
    /** Adobe HTTP Dynamic Streaming (HDS). */
    Hds(8),
    /** Include all protocols */
    All(0xFFFF);

    /** The content key type code. */
    private int falgValue;

    /**
     * Instantiates a new content key type.
     *
     * @param contentKeyTypeCode
     *            the content key type code
     */
    private AssetDeliveryProtocol(int falgValue) {
        this.falgValue = falgValue;
    }

    /**
     * Gets the flags value.
     *
     * @return the flags value
     */
    public int getFlagValue() {
        return falgValue;
    }

    /**
     * Given an integer representing the protocols as a bit vector, convert it
     * into an <code>EnumSet&lt;AssetDeliveryProtocol&gt;</code> object
     * containing the correct protocols *
     *
     * @param bits
     *            The bit vector of protocols
     * @return The set of protocols in an <code>EnumSet</code> object.
     */
    public static EnumSet<AssetDeliveryProtocol> protocolsFromBits(int bits) {
        EnumSet<AssetDeliveryProtocol> perms = EnumSet
                .of(AssetDeliveryProtocol.None);

        for (AssetDeliveryProtocol p : AssetDeliveryProtocol.values()) {
            if ((bits & p.getFlagValue()) ==  p.getFlagValue()) {
                perms.remove(AssetDeliveryProtocol.None);
                perms.add(p);
            }
        }

        return perms;
    }

    /**
     * Convert an <code>EnumSet</code> containing protocols into the
     * corresponding integer bit vector to be passed to Media services.
     *
     * @param perms
     *            The protocols
     * @return The bit vector to go out over the wire.
     */
    public static int bitsFromProtocols(EnumSet<AssetDeliveryProtocol> protos) {
        int result = 0;

        for (AssetDeliveryProtocol p : protos) {
            result |= p.getFlagValue();
        }

        return result;
    }
}
