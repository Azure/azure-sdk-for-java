/**
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

import java.util.Locale;

import com.microsoft.azure.storage.core.Utility;

/**
 * The tier of the page blob.
 * Please take a look at https://docs.microsoft.com/en-us/azure/storage/storage-premium-storage#scalability-and-performance-targets
 * for detailed information on the corresponding IOPS and throughput per PremiumPageBlobTier.
 */
public enum PremiumPageBlobTier {
    /**
     * The tier is not recognized by this version of the library.
     */
    UNKNOWN,

    /**
     * P4 Tier
     */
    P4,

    /**
     * P6 Tier
     */
    P6,

    /**
     * P10 Tier
     */
    P10,

    /**
     * P20 Tier
     */
    P20,

    /**
     * P30 Tier
     */
    P30,

    /**
     * P40 Tier
     */
    P40,

    /**
     * P50 Tier
     */
    P50,

    /**
     * P60 Tier
     */
    P60;

    /**
     * Parses a premium page blob tier from the given string.
     * 
     * @param premiumBlobTierString
     *        A <code>String</code> which contains the premium page blob tier to parse.
     * 
     * @return A <code>PremiumPageBlobTier</code> value that represents the premium page blob tier.
     */
    protected static PremiumPageBlobTier parse(String premiumBlobTierString) {

        if (Utility.isNullOrEmpty(premiumBlobTierString)) {
            return UNKNOWN;
        }

        premiumBlobTierString = premiumBlobTierString.toLowerCase(Locale.US);
        if ("p4".equals(premiumBlobTierString)) {
            return P4;
        }
        else if ("p6".equals(premiumBlobTierString)) {
            return P6;
        }
        else if ("p10".equals(premiumBlobTierString)) {
            return P10;
        }
        else if ("p20".equals(premiumBlobTierString)) {
            return P20;
        }
        else if ("p30".equals(premiumBlobTierString)) {
            return P30;
        }
        else if ("p40".equals(premiumBlobTierString)) {
            return P40;
        }
        else if ("p50".equals(premiumBlobTierString)) {
            return P50;
        }
        else if ("p60".equals(premiumBlobTierString)) {
            return P60;
        }
        else {
            return UNKNOWN;
        }
    }
}