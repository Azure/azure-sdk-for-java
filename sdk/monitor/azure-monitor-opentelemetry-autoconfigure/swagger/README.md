### Getting Started

[AutoRest](https://github.com/Azure/autorest) is required to generate the models. 

#### Install autorest

```ps
npm install -g autorest
```

#### Generate the models for live metrics
First, go to [this link](https://github.com/Azure/azure-rest-api-specs/tree/main/specification/applicationinsights/data-plane/LiveMetrics) to determine the version of the api you want to use.
If a new api version gets added to above link, then please update the "input-file" property 
of livemetrics_autorest.md in this repo to a new link that points to the correct swagger definition.

In the cmd prompt, run the following commands:
```
cd <repo-root>\sdk\monitor\azure-monitor-opentelemetry-autoconfigure\swagger
autorest livemetrics_autorest.md
```
This should generate the live metrics apis/classes in the swagger folder inside the quickpulse directory.

#### Generate other models relevant to the module

```ps
cd <swagger-folder>
autorest autorest_code.md
```

In order to use the latest version of autorest, update the `use` directive in the `autorest_code.md` file.

```yml
use: '@autorest/java@4.1.29'
```

After the code has been updated, copy the generated models from

`sdk/monitor/azure-monitor-opentelemetry-autoconfigure/src/main/java/com/azure/monitor/opentelemetry/autoconfigure/models/` 

to the appropriate package under 

`sdk/monitor/azure-monitor-opentelemetry-autoconfigure/src/main/java/com/azure/monitor/opentelemetry/autoconfigure/implementation/models/`

And then delete 

`sdk/monitor/azure-monitor-opentelemetry-autoconfigure/src/main/java/com/azure/monitor/opentelemetry/autoconfigure/models/` folder to clean it up.

