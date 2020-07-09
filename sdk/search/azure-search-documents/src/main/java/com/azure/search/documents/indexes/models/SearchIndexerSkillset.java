// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * A list of skills.
 */
@Fluent
public final class SearchIndexerSkillset {
    /*
     * The name of the skillset.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * The description of the skillset.
     */
    @JsonProperty(value = "description", required = true)
    private String description;

    /*
     * A list of skills in the skillset.
     */
    @JsonProperty(value = "skills", required = true)
    private List<SearchIndexerSkill> skills;

    /*
     * Details about cognitive services to be used when running skills.
     */
    @JsonProperty(value = "cognitiveServices")
    private CognitiveServicesAccount cognitiveServicesAccount;

    /*
     * The ETag of the skillset.
     */
    @JsonProperty(value = "@odata.etag")
    private String eTag;

    /**
     * Constructor of {@link SearchIndexerSkillset}.
     *
     * @param name The name of the skillset.
     */
    public SearchIndexerSkillset(String name) {
        this.name = name;
    }

    /**
     * Constructor of {@link SearchIndexerSkillset}.
     *
     * @param name The name of the skillset.
     * @param skills A list of skills in the skillset.
     */
    @JsonCreator
    public SearchIndexerSkillset(
        @JsonProperty(value = "name") String name,
        @JsonProperty(value = "skills") List<SearchIndexerSkill> skills) {
        this.name = name;
        this.skills = skills;
    }

    /**
     * Get the name property: The name of the skillset.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the description property: The description of the skillset.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: The description of the skillset.
     *
     * @param description the description value to set.
     * @return the SearchIndexerSkillset object itself.
     */
    public SearchIndexerSkillset setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the skills property: A list of skills in the skillset.
     *
     * @return the skills value.
     */
    public List<SearchIndexerSkill> getSkills() {
        return this.skills;
    }

    /**
     * Set the skills property: A list of skills in the skillset.
     *
     * @param skills the skills value to set.
     * @return the SearchIndexerSkillset object itself.
     */
    public SearchIndexerSkillset setSkills(List<SearchIndexerSkill> skills) {
        this.skills = skills;
        return this;
    }

    /**
     * Get the cognitiveServicesAccount property: Details about cognitive
     * services to be used when running skills.
     *
     * @return the cognitiveServicesAccount value.
     */
    public CognitiveServicesAccount getCognitiveServicesAccount() {
        return this.cognitiveServicesAccount;
    }

    /**
     * Set the cognitiveServicesAccount property: Details about cognitive
     * services to be used when running skills.
     *
     * @param cognitiveServicesAccount the cognitiveServicesAccount value to
     * set.
     * @return the SearchIndexerSkillset object itself.
     */
    public SearchIndexerSkillset setCognitiveServicesAccount(CognitiveServicesAccount cognitiveServicesAccount) {
        this.cognitiveServicesAccount = cognitiveServicesAccount;
        return this;
    }

    /**
     * Get the eTag property: The ETag of the skillset.
     *
     * @return the eTag value.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag property: The ETag of the skillset.
     *
     * @param eTag the eTag value to set.
     * @return the SearchIndexerSkillset object itself.
     */
    public SearchIndexerSkillset setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }
}
