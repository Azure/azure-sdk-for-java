using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage.Blob;
using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadFromFileTest : RandomBlobTest<SizeOptions>
    {
        private static string _tempFile;

        public UploadFromFileTest(SizeOptions options) : base(options)
        { 
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            _tempFile = Path.GetTempFileName();

            using var randomStream = RandomStream.Create(Options.Size);
            using var fileStream = File.OpenWrite(_tempFile);
            await randomStream.CopyToAsync(fileStream);
            fileStream.Close();
        }

        public override async Task GlobalCleanupAsync()
        {
            File.Delete(_tempFile);
            await base.GlobalCleanupAsync();
        }

        public override void Run(CancellationToken cancellationToken)
        {
            CloudBlockBlob.UploadFromFile(_tempFile);
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            return CloudBlockBlob.UploadFromFileAsync(_tempFile);
        }
    }
}
