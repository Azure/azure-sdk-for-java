// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.service;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.AliasListItem;
import com.azure.maps.service.models.AliasesCreateResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AliasSample {
	public static void main(String[] args) throws JsonProcessingException {
		if (args.length != 1) {
			System.out.println("Usage AliasSample.java <creatorDataItemId>");
			return;
		}
		HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
				new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
		MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();
		String creatorDataItemId = args[0];

		System.out.println("Create alias:");
		AliasesCreateResponse aliasCreateResponse = client.getAlias().create();
		MapsCommon.print(aliasCreateResponse);
		String aliasId = aliasCreateResponse.getAliasId();
		try {
			System.out.println("Assign resource:");
			AliasListItem assignResponse = client.getAlias().assign(aliasId, creatorDataItemId);
			MapsCommon.print(assignResponse);

			System.out.println("Get details:");
			AliasListItem getDetailsResponse = client.getAlias().getDetails(aliasId);
			MapsCommon.print(getDetailsResponse);

			System.out.println("List aliases:");
			PagedIterable<AliasListItem> list = client.getAlias().list();
			for (AliasListItem item : list) {
				MapsCommon.print(item);
			}
		} catch (HttpResponseException err) {
			System.out.println(err);
		} finally {
			client.getAlias().delete(aliasId);
			System.out.println(String.format("Deleted alias with aliasId: %s", aliasId));
		}
	}
}
