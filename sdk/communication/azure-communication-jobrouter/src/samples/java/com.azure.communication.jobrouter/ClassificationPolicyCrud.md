# Azure.Communication.JobRouter Samples - Classification Policy CRUD operations (sync)

## Create a client

Create a `RouterClient`.

```Java Snippet:Azure_Communication_JobRouter_Samples_CreateClient
JobRouterClient routerClient = new JobRouterClientBuilder()
                                    .connectionString("<Your connection string>")
                                    .build();

JobRouterAdministrationClient routerAdminClient = new JobRouterAdministrationClientBuilder()
                                                        .connectionString("<Your connection string>")
                                                        .build();
```


