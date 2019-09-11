using Azure.Core.Pipeline;
using Azure.Storage.Blobs;

using CommandLine;
using System;
using System.IO;
using System.Net.Http;
using System.Runtime;
using System.Text;
using System.Threading.Tasks;

namespace StoragePerfNet
{
    class Program
    {
        private const string _containerName = "testcontainer";
        private const string _blobName = "testblob";
        
        private const int _bytesPerMessage = 1024 * 10;
        private static readonly byte[] _payload;
        private static readonly Stream _payloadStream;

        static Program()
        {
            _payload = new byte[_bytesPerMessage];

            // Initialize payload with stable random data since all-zeros may be compressed or optimized
            (new Random(0)).NextBytes(_payload);

            _payloadStream = new MemoryStream(_payload, writable:false);
        }

        public class Options
        {
            [Option('c', "count", Default = 1)]
            public int Count { get; set; }

            [Option('d', "debug")]
            public bool Debug { get; set; }
        }

        static async Task Main(string[] args)
        {
            if (!GCSettings.IsServerGC)
            {
                throw new InvalidOperationException("Requires server GC");
            }

            var connectionString = Environment.GetEnvironmentVariable("STORAGE_CONNECTION_STRING");

            await Parser.Default.ParseArguments<Options>(args).MapResult(
                async o => await Run(connectionString, o),
                errors => Task.CompletedTask);
        }

        static async Task Run(string connectionString, Options options)
        {
#if DEBUG
            if (!options.Debug)
            {
                throw new InvalidOperationException("Requires release configuration");
            }
#endif

            var httpClientHandler = new HttpClientHandler();
            httpClientHandler.ServerCertificateCustomValidationCallback = (message, cert, chain, errors) => true;
            var httpClient = new HttpClient(httpClientHandler);

            var blobClientOptions = new BlobClientOptions();
            blobClientOptions.Transport = new HttpClientTransport(httpClient);

            var client = new BlobClient(connectionString, _containerName, _blobName, blobClientOptions);
            await client.UploadAsync(_payloadStream);

            var downloadResponse = await client.DownloadAsync();
            
            var downloadContent = downloadResponse.Value.Content;
            var downloadPayload = new byte[downloadContent.Length];
            downloadContent.Read(downloadPayload, 0, downloadPayload.Length);
            
            if (!((ReadOnlySpan<byte>)_payload).SequenceEqual(downloadPayload))
            {
                var sb = new StringBuilder();
                sb.Append($"Downloaded {downloadPayload.Length} bytes, not equal to uploaded {_payload.Length} bytes.");

                if (downloadPayload.Length == _payload.Length)
                {
                    for (var i = 0; i < _payload.Length; i++)
                    {
                        if (_payload[i] != downloadPayload[i])
                        {
                            sb.Append($"First difference at position {i}.  Uploaded {_payload[i]}, downloaded {downloadPayload[i]}");
                            break;
                        }
                    }
                }

                throw new InvalidOperationException(sb.ToString());
            }
        }
    }
}
