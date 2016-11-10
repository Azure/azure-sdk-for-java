package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.Map;

/**
 * An interface representing a resource that has tags.
 */
@Fluent
public interface HasTags {
    /**
     * @return the tags associated with the resource
     */
    Map<String, String> tags();

    /**
     * A resource definition allowing tags to be modified for the resource.
     *
     * @param <T> the type of the next stage resource definition
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
    }

    /**
     * An update allowing tags to be modified for the resource.
     *
     * @param <T> the type of the next stage resource update
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
         * @return the next stage of the resource update
         */
        T withTag(String key, String value);

        /**
         * Removes a tag from the resource.
         * @param key the key of the tag to remove
         * @return the next stage of the resource update
         */
        T withoutTag(String key);
    }
}
