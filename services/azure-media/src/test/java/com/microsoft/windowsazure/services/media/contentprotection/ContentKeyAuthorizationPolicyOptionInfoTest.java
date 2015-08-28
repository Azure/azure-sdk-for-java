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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyOptionType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyRestrictionType;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyOptionInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyRestriction;

public class ContentKeyAuthorizationPolicyOptionInfoTest {

	@Test
	public void testGetSetId() {
		// Arrange
		String expectedId = "expectedId";
		ContentKeyAuthorizationPolicyOptionInfo contentKeyAuthorizationPolicyOptionInfo = new ContentKeyAuthorizationPolicyOptionInfo(
				null, new ContentKeyAuthorizationPolicyOptionType().setId(expectedId));

		// Act
		String actualId = contentKeyAuthorizationPolicyOptionInfo.getId();

		// Assert
		assertEquals(expectedId, actualId);
	}

	@Test
	public void testGetSetName() {
		// Arrange
		String expectedName = "expectedName";
		ContentKeyAuthorizationPolicyOptionInfo contentKeyAuthorizationPolicyOptionInfo = new ContentKeyAuthorizationPolicyOptionInfo(
				null, new ContentKeyAuthorizationPolicyOptionType().setName(expectedName));

		// Act
		String actualName = contentKeyAuthorizationPolicyOptionInfo.getName();

		// Assert
		assertEquals(expectedName, actualName);
	}

	@Test
	public void testGetSetKeyDeliveryConfiguration() {
		// Arrange
		String expectedKeyDeliveryConfiguration = "expectedKeyDeliveryConfiguration";
		ContentKeyAuthorizationPolicyOptionInfo contentKeyAuthorizationPolicyOptionInfo = new ContentKeyAuthorizationPolicyOptionInfo(
				null, new ContentKeyAuthorizationPolicyOptionType()
						.setKeyDeliveryConfiguration(expectedKeyDeliveryConfiguration));

		// Act
		String actualKeyDeliveryConfiguration = contentKeyAuthorizationPolicyOptionInfo.getKeyDeliveryConfiguration();

		// Assert
		assertEquals(expectedKeyDeliveryConfiguration, actualKeyDeliveryConfiguration);

	}

	@Test
	public void testGetSetKeyDeliveryType() {
		// Arrange
		int expectedKeyDeliveryType = 2;
		ContentKeyAuthorizationPolicyOptionInfo contentKeyAuthorizationPolicyOptionInfo = new ContentKeyAuthorizationPolicyOptionInfo(
				null, new ContentKeyAuthorizationPolicyOptionType().setKeyDeliveryType(expectedKeyDeliveryType));

		// Act
		int actualKeyDeliveryType = contentKeyAuthorizationPolicyOptionInfo.getKeyDeliveryType();

		// Assert
		assertEquals(expectedKeyDeliveryType, actualKeyDeliveryType);
	}

	@Test
	public void testGetSetRestrictions() {
		// Arrange
		String expectedRestrictionName = "expectedRestriction";
		String expectedRestrictionReq = "<xml></xml>";
		int expectedRestrictionType = 2;
		List<ContentKeyAuthorizationPolicyRestrictionType> settedRestrictions = new ArrayList<ContentKeyAuthorizationPolicyRestrictionType>();
		settedRestrictions.add(new ContentKeyAuthorizationPolicyRestrictionType().setName(expectedRestrictionName)
				.setKeyRestrictionType(expectedRestrictionType).setRequirements(expectedRestrictionReq));
		ContentKeyAuthorizationPolicyOptionInfo contentKeyAuthorizationPolicyOptionInfo = new ContentKeyAuthorizationPolicyOptionInfo(
				null, new ContentKeyAuthorizationPolicyOptionType().setRestrictions(settedRestrictions));

		// Act
		List<ContentKeyAuthorizationPolicyRestriction> actualRestrictions = contentKeyAuthorizationPolicyOptionInfo
				.getRestrictions();

		// Assert
		assertEquals(actualRestrictions.get(0).getName(), expectedRestrictionName);
		assertEquals(actualRestrictions.get(0).getKeyRestrictionType(), expectedRestrictionType);
		assertEquals(actualRestrictions.get(0).getRequirements(), expectedRestrictionReq);
	}
}
