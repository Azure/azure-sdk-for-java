Feature: ServerFarm should work

Background: 
    Given I create a "WebSiteManagementClient" with name "management"

Scenario: Create Server Farm
    Given I create a "Microsoft.WindowsAzure.Management.WebSites.Models.ServerFarmCreateParameters" with name "parameters"
    And set "parameters.Name" with value "AF1"
    And set "parameters.Location" with value "West US"
    And set "parameters.Label" with value "Great AF"
    And I create a "System.String" with name "param1"
    And set "param1" with value "newserverfarm"
    And I invoke "management.ServerFarmsOperations.Create" with parameters "param1" and "parameters"
