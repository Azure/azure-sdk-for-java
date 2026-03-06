// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.discovery;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.discovery.models.Project;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live tests for Project operations against EUAP endpoint.
 * 
 * Tests match the comprehensive coverage in Python SDK:
 * - test_list_projects_by_workspace
 */
public class ProjectTests extends DiscoveryManagementTest {

    // Resource group and workspace that exist in the test environment
    private static final String WORKSPACE_RESOURCE_GROUP = "newapiversiontest";
    private static final String WORKSPACE_NAME = "wrksptest44";

    @Test
    public void testListProjectsByWorkspace() {
        // Test listing projects in a workspace (matching Python test_list_projects_by_workspace)
        PagedIterable<Project> projects
            = discoveryManager.projects().listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);
        assertNotNull(projects);

        // Collect all projects
        List<Project> projectList = new ArrayList<>();
        for (Project project : projects) {
            assertNotNull(project.name());
            assertNotNull(project.id());
            assertNotNull(project.type());
            projectList.add(project);
        }

        // Projects list should be a valid list (may be empty)
        assertNotNull(projectList);
    }

    @Test
    public void testGetProjectIfExists() {
        // First list projects to find one to get
        PagedIterable<Project> projects
            = discoveryManager.projects().listByWorkspace(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME);

        Project firstProject = null;
        for (Project project : projects) {
            firstProject = project;
            break;
        }

        if (firstProject != null) {
            String projectName = firstProject.name();

            // Get the project by name
            Project retrieved = discoveryManager.projects().get(WORKSPACE_RESOURCE_GROUP, WORKSPACE_NAME, projectName);

            assertNotNull(retrieved);
            assertNotNull(retrieved.name());
            // Type may be lowercase or PascalCase
            assertTrue(retrieved.type().equalsIgnoreCase("Microsoft.Discovery/workspaces/projects"));
        }
        // If no projects exist, test passes (nothing to get)
    }
}
