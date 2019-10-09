using Azure.Test.PerfStress;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class ContainerV11Test<TOptions> : ServiceV11Test<TOptions> where TOptions: PerfStressOptions
    {
        private const string _containerPrefix = "perfstress";
        protected static string ContainerName { get; private set; }

        static ContainerV11Test()
        {
            ContainerName = _containerPrefix + "-" + Guid.NewGuid();
        }

        protected CloudBlobContainer CloudBlobContainer { get; private set; }

        public ContainerV11Test(TOptions options) : base(options)
        {
            CloudBlobContainer = CloudBlobClient.GetContainerReference(ContainerName);
        }

        public override async Task GlobalSetup()
        {
            await base.GlobalSetup();
            await CloudBlobContainer.CreateAsync();
        }

        public override async Task GlobalCleanup()
        {
            await CloudBlobContainer.DeleteAsync();
            await base.GlobalCleanup();
        }
    }
}
