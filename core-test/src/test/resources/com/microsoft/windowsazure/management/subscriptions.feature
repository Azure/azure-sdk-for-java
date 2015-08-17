Feature: Subscriptions should work 

Background: 
    Given I create a "ManagementClient" with name "management" 

Scenario: List subscription operations with start and end time
    Given I create a "Microsoft.WindowsAzure.Management.Models.SubscriptionListOperationsParameters" with name "parameters"
    And set "parameters.StartTime" with value "DateTime.Now" of type "System.DateTime"
    And set "parameters.EndTime" with value "DateTime.Now" of type "System.DateTime"
    When I invoke "management.SubscriptionsOperations.ListOperations" with parameter "parameters" I get the result into "operationResponse" 
    Then property with type "System.Int32" and path "operationResponse.StatusCode" should equal "200" 
    And property with type "System.String" and path "operationResponse.RequestId" should not equal "null"