// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.SkillDetails;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating CRUD operations on Skills using the synchronous SkillsClient.
 *
 * <p>Skills are a preview feature. Before running, set the following environment variable:
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 * </ul>
 */
public class SkillsSample {

    private static SkillsClient skillsClient
        = new AIProjectClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSkillsClient();

    public static void main(String[] args) {
        // Uncomment the sample you want to run
//        createSkill();
//        getSkill();
//        updateSkill();
//        listSkills();
//        deleteSkill();
    }

    public static void createSkill() {
        // BEGIN:com.azure.ai.projects.SkillsSample.createSkill

        Map<String, String> metadata = new HashMap<>();
        metadata.put("domain", "support");

        SkillDetails skill = skillsClient.createSkill(
            "product-support-skill",
            "Answers product support questions using company policy.",
            "You help answer product support questions using company policy and product guidance.",
            metadata
        );

        System.out.println("Created skill: " + skill.getName());
        System.out.println("Skill ID: " + skill.getSkillId());
        System.out.println("Blob present: " + skill.isBlobPresent());

        // END:com.azure.ai.projects.SkillsSample.createSkill
    }

    public static void getSkill() {
        // BEGIN:com.azure.ai.projects.SkillsSample.getSkill

        String skillName = "product-support-skill";
        SkillDetails skill = skillsClient.getSkill(skillName);

        System.out.println("Skill name: " + skill.getName());
        System.out.println("Skill ID: " + skill.getSkillId());
        System.out.println("Description: " + skill.getDescription());

        // END:com.azure.ai.projects.SkillsSample.getSkill
    }

    public static void updateSkill() {
        // BEGIN:com.azure.ai.projects.SkillsSample.updateSkill

        String skillName = "product-support-skill";

        Map<String, String> metadata = new HashMap<>();
        metadata.put("domain", "support");
        metadata.put("status", "updated");

        SkillDetails updated = skillsClient.updateSkill(
            skillName,
            "Updated description for the sample skill.",
            null,
            metadata
        );

        System.out.println("Updated skill: " + updated.getName());
        System.out.println("Description: " + updated.getDescription());
        System.out.println("Metadata: " + updated.getMetadata());

        // END:com.azure.ai.projects.SkillsSample.updateSkill
    }

    public static void listSkills() {
        // BEGIN:com.azure.ai.projects.SkillsSample.listSkills

        PagedIterable<SkillDetails> skills = skillsClient.listSkills();
        for (SkillDetails skill : skills) {
            System.out.println("Skill name: " + skill.getName());
            System.out.println("Skill ID: " + skill.getSkillId());
            System.out.println("Blob present: " + skill.isBlobPresent());
            System.out.println("-------------------------------------------------");
        }

        // END:com.azure.ai.projects.SkillsSample.listSkills
    }

    public static void deleteSkill() {
        // BEGIN:com.azure.ai.projects.SkillsSample.deleteSkill

        String skillName = "product-support-skill";
        skillsClient.deleteSkill(skillName);

        System.out.println("Deleted skill: " + skillName);

        // END:com.azure.ai.projects.SkillsSample.deleteSkill
    }
}
