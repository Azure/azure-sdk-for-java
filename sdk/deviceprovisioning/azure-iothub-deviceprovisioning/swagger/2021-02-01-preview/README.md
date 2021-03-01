This folder contains two swagger files. "service.json" contains the current service team's swagger, and 
"service_modified.json" contains a slightly modified version of the service team's swagger to enable
code gen. Currently the service swagger is missing the type of the produced responses in several of the 
DELETE APIs

For instance:

    "operationId": "DeleteDeviceTransfer",
    "consumes": [],
    "produces": [<should have "application/json" here but doesn't>]
    
These DELETE APIs typically result in no response payload since they give a 204, but for error cases,
the service does return an application/json payload.

Once the service team has fixed this bug in their swagger, this folder should delete the modified swagger.
