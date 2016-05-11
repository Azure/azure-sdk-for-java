package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

public interface GroupableResource extends
		Resource {
	
	public String group();

	interface DefinitionWithGroupContext<T> {
	}

	/**
	 * A resource definition allowing a resource group to be selected
	 */
	interface DefinitionWithGroup<T>  extends DefinitionWithGroupContext<T> {
		/**
		 * Associates the resources with an existing resource group.
		 * @param groupName The name of an existing resource group to put this resource in.
		 * @return The next stage of the resource definition
		 */
		T withExistingGroup(String groupName);

		/**
		 * Associates the resources with an existing resource group.
		 * @param group An existing resource group to put the resource in
		 * @return The next stage of the resource definition
		 */
		T withExistingGroup(ResourceGroup group);

		/**
		 * Associates the resources with an existing resource group.
		 * @param group An existing resource group object as returned by the Azure SDK for Java to put the resource in
		 * @return The next stage of the resource definition
		 */
		T withExistingGroup(ResourceGroupInner group);

		/**
		 * Creates a new resource group to put the resource in. The group will be created in the same location as the resource.
		 * @param name The name of the new group
		 * @return The next stage of the resource definition
		 */
		T withNewGroup(String name);
		
		/**
		 * Creates a new resource group to put the resource in. The group will be created in the same location as the resource.
		 * The group's name is automatically derived from the resource's name.
		 * @return The next stage of the resource definition
		 */
		T withNewGroup();
		
		/**
		 * Creates a new resource group to put the resource in based on the provisionable definition specified.
		 * @param groupDefinition A provisionable definition for a new resource group
		 * @return The next stage of the resource definition
		 */
		T withNewGroup(ResourceGroup.DefinitionProvisionable groupDefinition) throws Exception;
	}
}
