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

import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyType;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyInfo;

public class ContentKeyAuthorizationPolicyInfoTest {

	@Test
	public void testGetSetId() {
		// Arrange
		String expectedId = "expectedId";
		ContentKeyAuthorizationPolicyInfo contentKeyAuthorizationPolicyInfo = new ContentKeyAuthorizationPolicyInfo(
				null, new ContentKeyAuthorizationPolicyType().setId(expectedId));

		// Act
		String actualId = contentKeyAuthorizationPolicyInfo.getId();

		// Assert
		assertEquals(expectedId, actualId);
	}

	@Test
	public void testGetSetName() {
		// Arrange
		String expectedName = "expectedName";
		ContentKeyAuthorizationPolicyInfo contentKeyAuthorizationPolicyInfo = new ContentKeyAuthorizationPolicyInfo(
				null, new ContentKeyAuthorizationPolicyType().setName(expectedName));

		// Act
		String actualName = contentKeyAuthorizationPolicyInfo.getName();

		// Assert
		assertEquals(expectedName, actualName);
	}
}
