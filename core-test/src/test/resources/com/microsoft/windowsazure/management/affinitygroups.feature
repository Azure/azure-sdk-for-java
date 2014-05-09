Feature: Affinity groups should work

Background: 
    Given I create a "ManagementClient" with name "management"

Scenario: Create an AffinityGroup
    Given I create a "Microsoft.WindowsAzure.Management.Models.AffinityGroupCreateParameters" with name "parameters"
    And I create a "10" character random String with name "testAffinityGroupName1" and prefix "azure-java-test-"
    And set "parameters.Name" with "testAffinityGroupName1" of type "System.String"
    And set "parameters.Location" with value "West US" of type "System.String"
    And set "parameters.Label" with value "Great AF" of type "System.String"
    When I invoke "management.AffinityGroupsOperations.Create" with parameter "parameters" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "201"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    Then invoke "management.AffinityGroupsOperations.Delete" with parameter "testAffinityGroupName1" of type "System.String"

Scenario: Get an AffinityGroup
    Given I create a "Microsoft.WindowsAzure.Management.Models.AffinityGroupCreateParameters" with name "parameters"
    And I create a "10" character random String with name "testAffinityGroupName2" and prefix "azure-java-test-"
    And set "parameters.Name" with "testAffinityGroupName2" of type "System.String"
    And set "parameters.Location" with value "West US" of type "System.String"
    And set "parameters.Label" with value "Great AF2" of type "System.String"
    And I invoke "management.AffinityGroupsOperations.Create" with parameter "parameters"
    When I invoke "management.AffinityGroupsOperations.Get" with parameter value "AF2" of type "System.String" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    Then invoke "management.AffinityGroupsOperations.Delete" with parameter "testAffinityGroupName2" of type "System.String"

Scenario: List AffinityGroups
    Given I create a "Microsoft.WindowsAzure.Management.Models.AffinityGroupCreateParameters" with name "parameters"
    And I create a "10" character random String with name "testAffinityGroupName3" and prefix "azure-java-test-"
    And set "parameters.Name" with "testAffinityGroupName3" of type "System.String"
    And set "parameters.Location" with value "West US" of type "System.String"
    And set "parameters.Label" with value "Great AF3" of type "System.String"
    And I invoke "management.AffinityGroupsOperations.Create" with parameter "parameters"
    When I invoke "management.AffinityGroupsOperations.List" I get the result into "operationResponse"
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200"
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"
    And set "element" with value from list "operationResponse.AffinityGroups" where "Name" of type "System.String" equals "AF3"
    And property with type "System.String" and path "element.Name" should equal "AF3"
    Then invoke "management.AffinityGroupsOperations.Delete" with parameter "testAffinityGroupName3" of type "System.String"