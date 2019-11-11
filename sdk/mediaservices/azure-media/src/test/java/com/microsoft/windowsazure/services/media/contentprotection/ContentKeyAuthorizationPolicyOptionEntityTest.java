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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyAuthorizationPolicyOptionType;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyOption;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyOptionInfo;
import com.microsoft.windowsazure.services.media.models.ContentKeyAuthorizationPolicyRestriction;

/**
 * Tests for the methods and factories of the Asset entity.
 */
public class ContentKeyAuthorizationPolicyOptionEntityTest {
	private static final String ENTITY_SET = "ContentKeyAuthorizationPolicyOptions";
	private static final String sampleCKAPOId = "nb:ckpoid:UUID:c244a5a7-a3d9-4ba0-a707-4336554f09b2";
	private static final String sampleCKAPOName = "sampleContentKeyAuthPolicyOptName";
	private static final String sampleCKAPOKDC = "sampleKeyDeliveryConfiguration";
	private final String expectedUri = String.format("%s('%s')", ENTITY_SET, URLEncoder.encode(sampleCKAPOId, "UTF-8"));

	public ContentKeyAuthorizationPolicyOptionEntityTest() throws Exception {
	}

	@Test
	public void ckapoCreateReturnsValidPayload() throws ServiceException {
		List<ContentKeyAuthorizationPolicyRestriction> restrictions = new ArrayList<ContentKeyAuthorizationPolicyRestriction>();
		ContentKeyAuthorizationPolicyOptionType payload = (ContentKeyAuthorizationPolicyOptionType) ContentKeyAuthorizationPolicyOption
				.create(sampleCKAPOName, 2, sampleCKAPOKDC, restrictions).getRequestContents();

		assertNotNull(payload);
		assertNull(payload.getId());
		assertEquals(payload.getName(), sampleCKAPOName);
		assertEquals(payload.getKeyDeliveryType(), 2);
		assertEquals(payload.getKeyDeliveryConfiguration(), sampleCKAPOKDC);
		assertNotNull(payload.getRestrictions());
	}

	@Test
	public void ckapoGetReturnsExpectedUri() throws Exception {
		EntityGetOperation<ContentKeyAuthorizationPolicyOptionInfo> getter = ContentKeyAuthorizationPolicyOption
				.get(sampleCKAPOId);

		assertEquals(expectedUri, getter.getUri());
	}

	@Test
	public void ckapoListReturnsExpectedUri() {
		EntityListOperation<ContentKeyAuthorizationPolicyOptionInfo> lister = ContentKeyAuthorizationPolicyOption
				.list();

		assertEquals(ENTITY_SET, lister.getUri());
		assertNotNull(lister.getQueryParameters());
		assertEquals(0, lister.getQueryParameters().size());
	}

	@Test
	public void ckapoListCanTakeQueryParameters() {
		EntityListOperation<ContentKeyAuthorizationPolicyOptionInfo> lister = ContentKeyAuthorizationPolicyOption.list()
				.setTop(10).setSkip(2);

		assertEquals("10", lister.getQueryParameters().getFirst("$top"));
		assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
		assertEquals(2, lister.getQueryParameters().size());
	}

	@Test
	public void ckapoListCanTakeQueryParametersChained() {
		EntityListOperation<ContentKeyAuthorizationPolicyOptionInfo> lister = ContentKeyAuthorizationPolicyOption.list()
				.setTop(10).setSkip(2).set("filter", "something");

		assertEquals("10", lister.getQueryParameters().getFirst("$top"));
		assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
		assertEquals("something", lister.getQueryParameters().getFirst("filter"));
		assertEquals(3, lister.getQueryParameters().size());
	}

	@Test
	public void ckapoDeleteReturnsExpectedUri() throws Exception {
		EntityDeleteOperation deleter = ContentKeyAuthorizationPolicyOption.delete(sampleCKAPOId);

		assertEquals(expectedUri, deleter.getUri());
	}
}
