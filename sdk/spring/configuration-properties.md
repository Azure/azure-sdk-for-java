

| Name                                                         | Description                                                  | Default Value |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------- |
| spring.cloud.azure.client.application-id                     | Represents current application and is used for telemetry/monitoring purposes. |               |
| spring.cloud.azure.client.amqp.transport-type                | Transport type for AMQP-based client.                        |               |
| spring.cloud.azure.client.headers                            | Comma-delimited list of headers applied to each request sent with client. |               |
| spring.cloud.azure.client.http.connect-timeout               | Amount of time the request attempts to connect to the remote host and the connection is resolved. |               |
| spring.cloud.azure.client.http.connection-idle-timeout       | Amount of time before an idle connection.                    |               |
| spring.cloud.azure.client.http.logging.allowed-header-names  | Comma-delimited list of whitelisted headers that should be logged. |               |
| spring.cloud.azure.client.http.logging.allowed-query-param-names | Comma-delimited list of whitelisted query parameters.        |               |
| spring.cloud.azure.client.http.logging.level                 | The level of detail to log on HTTP messages.                 |               |
| spring.cloud.azure.client.http.logging.prettyPrintBody       | Whether to pretty print the message bodies.                  |               |
| spring.cloud.azure.client.http.maximum-connection-pool-size  | Maximum connection pool size used by the underlying HTTP client. |               |
| spring.cloud.azure.client.http.read-timeout                  | Amount of time used when reading the server response.        |               |
| spring.cloud.azure.client.http.write-timeout                 | Amount of time each request being sent over the w            |               |
| spring.cloud.azure.credential.client-certificate-password    | Password of the certificate file.                            |               |
| spring.cloud.azure.credential.client-certificate-path        | Path of a PEM certificate file to use when performing service principal authentication with Azure. |               |
| spring.cloud.azure.credential.client-id                      | Client id to use when performing service principal authentication with Azure. |               |
| spring.cloud.azure.credential.client-secret                  | Client secret to use when performing service principal authentication with Azure. |               |
| spring.cloud.azure.credential.managed-identity-client-id     | Client id to use when using managed identity to authenticate with Azure. |               |
| spring.cloud.azure.credential.username                       | Username to use when performing username/password authentication with Azure. |               |
| spring.cloud.azure.credential.password                       | Password to use when performing username/password authentication with Azure. |               |
| spring.cloud.azure.profile.cloud                             | Name of the Azure cloud to connect to.                       |               |
| spring.cloud.azure.profile.environment.active-directory-endpoint |                                                              |               |
| spring.cloud.azure.profile.subscription                      | Subscription id to use when connecting to Azure resources.   |               |
| spring.cloud.azure.profile.tenant-id                         | Tenant id for Azure resources.                               |               |
| spring.cloud.azure.proxy.authentication-type                 | Authentication type used against the proxy.                  |               |
| spring.cloud.azure.proxy.hostname                            | The host of the proxy.                                       |               |
| spring.cloud.azure.proxy.password                            | Password used to authenticate with the proxy.                |               |
| spring.cloud.azure.proxy.port                                | The port of the proxy.                                       |               |
| spring.cloud.azure.proxy.type                                | Type of the proxy.                                           |               |
| spring.cloud.azure.proxy.username                            | Username used to authenticate with the proxy.                |               |
| spring.cloud.azure.proxy.http.non-proxy-hosts                | A list of hosts or CIDR to not use proxy HTTP/HTTPS connections through. |               |
| spring.cloud.azure.retry.backoff.delay                       | Amount of time to wait between retry attempts.               |               |
| spring.cloud.azure.retry.backoff.max-delay                   | Maximum permissible amount of time between retry attempts.   |               |
| spring.cloud.azure.retry.backoff.multiplier                  | Multiplier used to calculate the next backoff delay. If positive, then used as a multiplier for generating the next delay for backoff. |               |
| spring.cloud.azure.retry.http.retry-after-header             | HTTP header, such as Retry-After or x-ms-retry-after-ms, to lookup for the retry delay. |               |
| spring.cloud.azure.retry.http.retry-after-time-unit          | Time unit to use when applying the retry delay.              |               |
| spring.cloud.azure.retry.max-attempts                        | The maximum number of attempts.                              |               |
| spring.cloud.azure.retry.timeout                             | Amount of time to wait until a timeout.                      |               |

### 