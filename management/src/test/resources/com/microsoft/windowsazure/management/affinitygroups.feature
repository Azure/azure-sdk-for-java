Feature: Affinity groups should work

Background: 
    Given I create a "ManagementClient" with name "management"

Scenario: Create an AffinityGroup
    Given I create a "Microsoft.WindowsAzure.Management.Models.AffinityGroupCreateParameters" with name "parameters"
    And set property "parameters.Name" with value "AF1"
    And set property "parameters.Location" with value "West US"
    And set property "parameters.Label" with value "Great AF"
    When I invoke "management.AffinityGroupsOperations.Create" with parameter "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "201"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    Then invoke "management.AffinityGroupsOperations.Delete" with parameter value "AF1" of type "System.String"

Scenario: Get an AffinityGroup
    Given I create a "Microsoft.WindowsAzure.Management.Models.AffinityGroupCreateParameters" with name "parameters"
    And set property "parameters.Name" with value "AF2"
    And set property "parameters.Location" with value "West US"
    And set property "parameters.Label" with value "Great AF2"
    And I invoke "management.AffinityGroupsOperations.Create" with parameter "parameters"
    When I invoke "management.AffinityGroupsOperations.Get" with parameter value "AF2" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    Then invoke "management.AffinityGroupsOperations.Delete" with parameter value "AF2" of type "System.String"
    
Scenario: List AffinityGroups
    Given I create a "Microsoft.WindowsAzure.Management.Models.AffinityGroupCreateParameters" with name "parameters"
    And set property "parameters.Name" with value "AF3"
    And set property "parameters.Location" with value "West US"
    And set property "parameters.Label" with value "Great AF3"
    And I invoke "management.AffinityGroupsOperations.Create" with parameter "parameters"
    When I invoke "management.AffinityGroupsOperations.List" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And property with type "System.String" and path "operationResponse.AffinityGroups[0].Name" should equal "AF3"
    Then invoke "management.AffinityGroupsOperations.Delete" with parameter value "AF3" of type "System.String"