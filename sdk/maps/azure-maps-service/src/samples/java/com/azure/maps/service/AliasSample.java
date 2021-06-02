// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.service;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.maps.service.models.AliasCreateResponseV2;
import com.azure.maps.service.models.AliasListItem;

public class AliasSample {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage AliasSample.java <creatorDataItemId(default \"\")>");
		}
		Alias alias = MapsCommon.createMapsClient().getAlias();
		String creatorDataItemId = "";
		if (args.length > 0) {
			creatorDataItemId = args[0];
		}
		String aliasId = AliasSample.create(alias);
		try {
			if(!creatorDataItemId.isEmpty()) {
				AliasSample.assign(alias, aliasId, creatorDataItemId);
			}
			AliasSample.getDetails(alias, aliasId);
			AliasSample.list(alias);
		} catch(HttpResponseException err) {
			System.out.println(err);
		} finally {
			AliasSample.delete(alias, aliasId);
		}
	}

	public static String create(Alias alias) {
		AliasCreateResponseV2 response = alias.create();
		System.out.println(String.format("Created alias with: %s", response.getAliasId()));
		return response.getAliasId();
	}
	
	public static void assign(Alias alias, String aliasId, String creatorDataItemId)	{
		AliasListItem aliasListItem = alias.assign(aliasId, creatorDataItemId);

		System.out.println(String.format("Assigned resource with creator_data_item_id %s to alias with alias_id: %s",
				aliasListItem.getCreatorDataItemId(), aliasListItem.getAliasId()));
	}
	public static void list(Alias alias)	{
		PagedIterable<AliasListItem> aliasList = alias.list();
		System.out.println("View all previously created aliases:");
		aliasList.forEach(aliasListItem -> System.out.println(aliasListItem.getAliasId()));
	}
	public static void delete(Alias alias, String aliasId) {
	    alias.delete(aliasId);
	    System.out.println(String.format("Deleted alias with aliasId: %s", aliasId));
	}
	public static void getDetails(Alias alias, String aliasId) {
		AliasListItem aliasListItem = alias.getDetails(aliasId);
		System.out.println("Got details of alias:");
		System.out.println(aliasListItem.getAliasId());
	}

}
