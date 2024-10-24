### Getting Started

[AutoRest](https://github.com/Azure/autorest) is required to generate the models. 

#### Install autorest

```ps
npm install -g autorest
```

#### Generate the models

```ps
cd <swagger-folder>
autorest autorest_code.md
```

In order to use the latest version of autorest, update the `use` directive in the `autorest_code.md` file.

```yml
use: '@autorest/java@4.1.29'
```

After the code has been updated, copy the generated models from

`sdk/monitor/azure-monitor-opentelemetry-exporter/src/main/java/com/azure/monitor/opentelemetry/exporter/models/` 

to the appropriate package under 

`sdk/monitor/azure-monitor-opentelemetry-exporter/src/main/java/com/azure/monitor/opentelemetry/exporter/implementation/models/`

And then delete 

`sdk/monitor/azure-monitor-opentelemetry-exporter/src/main/java/com/azure/monitor/opentelemetry/exporter/models/` folder to clean it up.

