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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URLEncoder;

import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyType;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicy;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyInfo;

/**
 * Tests for the methods and factories of the Asset entity.
 */
public class ContentKeyAuthorizationPolicyEntityTest {
	// Common Arrange
	private static final String ENTITY_SET = "ContentKeyAuthorizationPolicies";
	private static final String sampleCKAPId = "nb:ckpid:UUID:e640675d-adf5-4a15-b8c5-021e3ab01a10";
	private static final String sampleCKAPName = "sampleContentKeyAuthPolicyName";
	private final String expectedUri = String.format("%s('%s')", ENTITY_SET, URLEncoder.encode(sampleCKAPId, "UTF-8"));

	public ContentKeyAuthorizationPolicyEntityTest() throws Exception {
	}

	@Test
	public void ckapCreateReturnsValidPayload() throws ServiceException {
		// Act
		ContentKeyAuthorizationPolicyType payload = (ContentKeyAuthorizationPolicyType) ContentKeyAuthorizationPolicy
				.create(sampleCKAPName).getRequestContents();

		// Assert
		assertNotNull(payload);
		assertNull(payload.getId());
		assertEquals(payload.getName(), sampleCKAPName);

	}

	@Test
	public void ckapGetReturnsExpectedUri() throws Exception {
		// Act
		EntityGetOperation<ContentKeyAuthorizationPolicyInfo> getter = ContentKeyAuthorizationPolicy.get(sampleCKAPId);

		// Assert
		assertEquals(expectedUri, getter.getUri());
	}

	@Test
	public void ckapListReturnsExpectedUri() {
		// Act
		EntityListOperation<ContentKeyAuthorizationPolicyInfo> lister = ContentKeyAuthorizationPolicy.list();

		// Assert
		assertEquals(ENTITY_SET, lister.getUri());
		assertNotNull(lister.getQueryParameters());
		assertEquals(0, lister.getQueryParameters().size());
	}

	@Test
	public void ckapListCanTakeQueryParameters() {
		EntityListOperation<ContentKeyAuthorizationPolicyInfo> lister = ContentKeyAuthorizationPolicy.list().setTop(10)
				.setSkip(2);

		assertEquals("10", lister.getQueryParameters().getFirst("$top"));
		assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
		assertEquals(2, lister.getQueryParameters().size());
	}

	@Test
	public void ckapListCanTakeQueryParametersChained() {
		EntityListOperation<ContentKeyAuthorizationPolicyInfo> lister = ContentKeyAuthorizationPolicy.list().setTop(10)
				.setSkip(2).set("filter", "something");

		assertEquals("10", lister.getQueryParameters().getFirst("$top"));
		assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
		assertEquals("something", lister.getQueryParameters().getFirst("filter"));
		assertEquals(3, lister.getQueryParameters().size());
	}

	@Test
	public void ckapDeleteReturnsExpectedUri() throws Exception {
		EntityDeleteOperation deleter = ContentKeyAuthorizationPolicy.delete(sampleCKAPId);

		assertEquals(expectedUri, deleter.getUri());
	}
}
