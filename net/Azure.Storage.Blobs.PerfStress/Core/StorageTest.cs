using Azure.Core.Pipeline;
using Azure.Test.PerfStress;
using System;
using System.Net.Http;

namespace Azure.Storage.Blobs.PerfStress
{
    public abstract class StorageTest<TOptions> : PerfStressTest<TOptions> where TOptions: PerfStressOptions
    {
        private const string _containerName = "perfstress";

        protected BlobClient BlobClient { get; private set; }

        public StorageTest(string id, TOptions options) : base(id, options)
        {
            var blobName = this.GetType().Name.ToLowerInvariant() + id;
            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            if (string.IsNullOrEmpty(connectionString))
            {
                throw new InvalidOperationException("Undefined environment variable STORAGE_CONNECTION_STRING");
            }

            var httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true;
            var httpClient = new HttpClient(httpClientHandler);

            var blobClientOptions = new BlobClientOptions();
            blobClientOptions.Transport = new HttpClientTransport(httpClient);

            var serviceClient = new BlobServiceClient(connectionString, blobClientOptions);
            try
            {
                serviceClient.CreateBlobContainer(_containerName);
            }
            catch (StorageRequestFailedException)
            {
            }

            BlobClient = new BlobClient(connectionString, _containerName, blobName, blobClientOptions);
        }
    }
}
