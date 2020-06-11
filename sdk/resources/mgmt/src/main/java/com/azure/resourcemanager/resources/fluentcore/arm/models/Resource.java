// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

import java.util.Map;

/**
 * Base interfaces for fluent resources.
 */
public interface Resource extends
        Indexable,
        HasId,
        HasName {

    Resource DUMMY = new Resource() {
        @Override
        public String type() {
            return null;
        }

        @Override
        public String regionName() {
            return null;
        }

        @Override
        public Region region() {
            return null;
        }

        @Override
        public Map<String, String> tags() {
            return null;
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public String name() {
            return null;
        }

        @Override
        public String key() {
            return null;
        }
    };

    /**
     * @return the type of the resource
     */
    String type();

    /**
     * @return the name of the region the resource is in
     */
    String regionName();

    /**
     * @return the region the resource is in
     */
    Region region();

    /**
     * @return the tags for the resource
     */
    Map<String, String> tags();

    /**
     * A resource definition allowing a location be selected for the resource.
     *
     * @param <T> the type of the next stage resource definition
     */
    interface DefinitionWithRegion<T> {
        /**
         * Specifies the region for the resource by name.
         *
         * @param regionName The name of the region for the resource
         * @return the next stage of the definition
         */
        T withRegion(String regionName);

        /**
         * Specifies the region for the resource.
         *
         * @param region The location for the resource
         * @return the next stage of the definition
         */
        T withRegion(Region region);
    }

    /**
     * A resource definition allowing tags to be modified for the resource.
     *
     * @param <T> the type of the next stage resource definition
     */
    interface DefinitionWithTags<T> {
        /**
         * Specifies tags for the resource as a {@link Map}.
         *
         * @param tags a {@link Map} of tags
         * @return the next stage of the definition
         */
        T withTags(Map<String, String> tags);

        /**
         * Adds a tag to the resource.
         *
         * @param key the key for the tag
         * @param value the value for the tag
         * @return the next stage of the definition
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
         *
         * @param tags a {@link Map} of tags
         * @return the next stage of the resource update
         */
        T withTags(Map<String, String> tags);

        /**
         * Adds a tag to the resource.
         *
         * @param key the key for the tag
         * @param value the value for the tag
         * @return the next stage of the resource update
         */
        T withTag(String key, String value);

        /**
         * Removes a tag from the resource.
         *
         * @param key the key of the tag to remove
         * @return the next stage of the resource update
         */
        T withoutTag(String key);
    }
}
