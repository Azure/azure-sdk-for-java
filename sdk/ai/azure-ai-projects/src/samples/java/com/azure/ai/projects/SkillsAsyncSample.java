// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.Skill;
import com.azure.ai.projects.models.SkillInlineContent;
import com.azure.ai.projects.models.SkillVersion;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Sample demonstrating CRUD operations on Skills using the asynchronous SkillsAsyncClient.
 *
 * <p>Skills are a preview feature. Before running, set the following environment variable:
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 * </ul>
 */
public class SkillsAsyncSample {

    private static SkillsAsyncClient skillsAsyncClient
        = new AIProjectClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildSkillsAsyncClient();

    public static void main(String[] args) {
        // Using block() to wait for the async operations to complete in the sample
//        createSkillVersion().block();
//        getSkill().block();
//        updateSkill().block();
//        listSkills().blockLast();
//        deleteSkill().block();
    }

    public static Mono<SkillVersion> createSkillVersion() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.createSkillVersion

        SkillInlineContent inlineContent = new SkillInlineContent(
            "Answers product support questions using company policy.",
            "You help answer product support questions using company policy and product guidance."
        );

        return skillsAsyncClient.createSkillVersion("product-support-skill", inlineContent, true)
            .doOnNext(skillVersion -> {
                System.out.println("Created skill version: " + skillVersion.getName());
                System.out.println("Version: " + skillVersion.getVersion());
            });

        // END:com.azure.ai.projects.SkillsAsyncSample.createSkillVersion
    }

    public static Mono<Skill> getSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.getSkill

        String skillName = "product-support-skill";

        return skillsAsyncClient.getSkill(skillName)
            .doOnNext(skill -> {
                System.out.println("Skill name: " + skill.getName());
                System.out.println("Description: " + skill.getDescription());
                System.out.println("Default version: " + skill.getDefaultVersion());
            });

        // END:com.azure.ai.projects.SkillsAsyncSample.getSkill
    }

    public static Mono<Skill> updateSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.updateSkill

        String skillName = "product-support-skill";

        return skillsAsyncClient.updateSkill(skillName, "2")
            .doOnNext(updated -> {
                System.out.println("Updated skill: " + updated.getName());
                System.out.println("Default version: " + updated.getDefaultVersion());
            });

        // END:com.azure.ai.projects.SkillsAsyncSample.updateSkill
    }

    public static Flux<Skill> listSkills() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.listSkills

        return skillsAsyncClient.listSkills()
            .doOnNext(skill -> {
                System.out.println("Skill name: " + skill.getName());
                System.out.println("Description: " + skill.getDescription());
                System.out.println("-------------------------------------------------");
            });

        // END:com.azure.ai.projects.SkillsAsyncSample.listSkills
    }

    public static Mono<Void> deleteSkill() {
        // BEGIN:com.azure.ai.projects.SkillsAsyncSample.deleteSkill

        String skillName = "product-support-skill";

        return skillsAsyncClient.deleteSkill(skillName)
            .doOnSuccess(unused -> System.out.println("Deleted skill: " + skillName));

        // END:com.azure.ai.projects.SkillsAsyncSample.deleteSkill
    }
}
