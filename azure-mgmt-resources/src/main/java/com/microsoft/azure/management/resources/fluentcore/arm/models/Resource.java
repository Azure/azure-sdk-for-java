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
		 * @param regionName The name of the region for the resource
		 * @return the next stage of the resource definition
		 */
	    T withRegion(String regionName);
	    
	    /**
	     * @param region The location for the resource
	     * @return the next stage of the resource definition
	     */
	    T withRegion(Region region);
	}
	
	/**
	 * A resource definition allowing tags to be modified for the resource
	 */
	interface DefinitionWithTags<T> {
	    /**
	     * Specifies tags for the resource as a {@link Map}.
	     * @param tags a {@link Map} of tags
	     * @return the next stage of the resource definition
	     */
	    T withTags(Map<String, String> tags);
	    
	    /**
	     * Adds a tag to the resource.
	     * @param key the key for the tag
	     * @param value the value for the tag
	     * @return the next stage of the resource definition
	     */
	    T withTag(String key, String value);
	    
	    /**
	     * Removes a tag from the resource
	     * @param key the key of the tag to remove
	     * @return the next stage of the resource definition
	     */
	    T withoutTag(String key);
	}
	
	/**
	 * An update allowing tags to be modified for the resource
	 */
	interface UpdateWithTags<T> {
	    /**
	     * Specifies tags for the resource as a {@link Map}.
	     * @param tags a {@link Map} of tags
	     * @return the next stage of the resource update
	     */
        T withTags(Map<String, String> tags);
        
        /**
         * Adds a tag to the resource.
         * @param key the key for the tag
         * @param value the value for the tag
         * @return the next stage of the resource definition
         */
        T withTag(String key, String value);

        /**
         * Removes a tag from the resource
         * @param key the key of the tag to remove
         * @return the next stage of the resource definition
         */
        T withoutTag(String key);	    
	}
}
