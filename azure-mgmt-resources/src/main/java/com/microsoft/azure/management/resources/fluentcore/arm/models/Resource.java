package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

import java.util.Map;

public interface Resource extends Indexable {
	String id();
	String type();
	String name();
	String region();
	Map<String, String> tags();

	/**
	 * A resource definition allowing a location be selected for the resource
	 */	
	interface DefinitionWithRegion<T> {
		/**
		 * @param regionName The name of the location for the resource
		 * @return The next stage of the resource definition
		 */
	    T withRegion(String regionName);
	    
	    /**
	     * @param region The location for the resource
	     * @return The next stage of the resource definition
	     */
	    T withRegion(Region region);
	}
	
	
	/**
	 * A resource definition allowing tags to be specified
	 */
	interface DefinitionWithTags<T> extends Taggable<T> {
	}
}
