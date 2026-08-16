// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.SkillDetails;
import com.azure.ai.projects.models.SkillInlineContent;
import com.azure.ai.projects.models.SkillVersion;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrating CRUD operations on Skills using the synchronous BetaSkillsClient.
 *
 * <p>Skills are a preview feature. Before running, set the following environment variable:
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 * </ul>
 */
public class SkillsSample {

    private static BetaSkillsClient skillsClient
        = new AIProjectClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .beta().buildBetaSkillsClient();

    public static void main(String[] args) {
        // Uncomment the sample you want to run
//        createSkillVersion();
//        getSkill();
//        updateSkill();
//        listSkills();
//        deleteSkill();
    }

    public static void createSkillVersion() {
        // BEGIN:com.azure.ai.projects.SkillsSample.createSkillVersion

        SkillInlineContent inlineContent = new SkillInlineContent(
            "Answers product support questions using company policy.",
            "You help answer product support questions using company policy and product guidance."
        );

        SkillVersion skillVersion = skillsClient.createSkillVersion("product-support-skill", inlineContent, true);

        System.out.println("Created skill version: " + skillVersion.getName());
        System.out.println("Version: " + skillVersion.getVersion());

        // END:com.azure.ai.projects.SkillsSample.createSkillVersion
    }

    public static void getSkill() {
        // BEGIN:com.azure.ai.projects.SkillsSample.getSkill

        String skillName = "product-support-skill";
        SkillDetails skill = skillsClient.getSkill(skillName);

        System.out.println("Skill name: " + skill.getName());
        System.out.println("Description: " + skill.getDescription());
        System.out.println("Default version: " + skill.getDefaultVersion());

        // END:com.azure.ai.projects.SkillsSample.getSkill
    }

    public static void updateSkill() {
        // BEGIN:com.azure.ai.projects.SkillsSample.updateSkill

        String skillName = "product-support-skill";

        SkillDetails updated = skillsClient.updateSkill(skillName, "2");

        System.out.println("Updated skill: " + updated.getName());
        System.out.println("Default version: " + updated.getDefaultVersion());

        // END:com.azure.ai.projects.SkillsSample.updateSkill
    }

    public static void listSkills() {
        // BEGIN:com.azure.ai.projects.SkillsSample.listSkills

        PagedIterable<SkillDetails> skills = skillsClient.listSkills();
        for (SkillDetails skill : skills) {
            System.out.println("Skill name: " + skill.getName());
            System.out.println("Description: " + skill.getDescription());
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
