
### FAQ

#### I am getting this error:

- ``Request Rate too Large`` Request too large is an error from service indicating that you temporarily went beyond the provisioned throughput. You should retry after the provided
``DocumentClientException#getRetryAfterInMilliseconds()``.

- ``CollectionPoolExhausted`` this is a SDK side error indicating that the SDK's connection pool is saturated. Consider to retry later, increase the connection pool size or use a semaphore to throttle your workload.


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Ffaq%2FREADME.png)
