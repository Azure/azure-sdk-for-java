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

package com.microsoft.windowsazure.services.media.contentprotection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyRestriction;

/**
 * Tests for the methods and factories of the Asset entity.
 */
public class ContentKeyAuthorizationPolicyRestrictionTest {
	
    @Test
    public void roundTripTest() {
        // provides full code coverage
        assertEquals(ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.Open, ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.valueOf("Open"));
        assertEquals(ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.TokenRestricted, ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.valueOf("TokenRestricted"));
        assertEquals(ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.IPRestricted, ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.valueOf("IPRestricted"));        
    }
    
    @Test
    public void getValueOfContentKeyRestrictionTypeTest() {
        // provides full code coverage
        assertEquals(ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.Open.getValue(), 0);
        assertEquals(ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.TokenRestricted.getValue(), 1);
        assertEquals(ContentKeyAuthorizationPolicyRestriction.ContentKeyRestrictionType.IPRestricted.getValue(), 2);        
    }
}
