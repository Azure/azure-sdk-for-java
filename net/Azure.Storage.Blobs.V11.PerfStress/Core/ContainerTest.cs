using Azure.Test.PerfStress;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class ContainerTest<TOptions> : ServiceTest<TOptions> where TOptions: PerfStressOptions
    {
        private const string _containerPrefix = "perfstress";
        protected static string ContainerName { get; private set; }

        static ContainerTest()
        {
            ContainerName = _containerPrefix + "-" + Guid.NewGuid();
        }

        protected CloudBlobContainer CloudBlobContainer { get; private set; }

        public ContainerTest(TOptions options) : base(options)
        {
            CloudBlobContainer = CloudBlobClient.GetContainerReference(ContainerName);
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();
            await CloudBlobContainer.CreateAsync();
        }

        public override async Task GlobalCleanupAsync()
        {
            await CloudBlobContainer.DeleteAsync();
            await base.GlobalCleanupAsync();
        }
    }
}
