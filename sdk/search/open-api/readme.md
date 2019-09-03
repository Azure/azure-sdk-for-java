## Generating Azure Search (Service and Data) REST java implementations

### Prerequisites
1. Clone the [AutoRest.java](https://github.com/Azure/autorest.java/tree/v3) project ('v3' branch - which supports [project Reactor](https://projectreactor.io/))
2. Install [AutoRest v2](https://github.com/Azure/autorest/blob/master/README.md#installing-autorest) project

### Generation

run the following bash script:

```bash
$ sh getSpecsAndGenerate.sh <autorest.java directory path>
```

It will create/update two directories azure-search-service and azure-search-data with the newly created REST implementations