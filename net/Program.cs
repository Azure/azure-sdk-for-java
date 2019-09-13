using Azure.Core.Pipeline;
using Azure.Storage.Blobs;

using CommandLine;
using System;
using System.Diagnostics;
using System.IO;
using System.Net.Http;
using System.Runtime;
using System.Text;
using System.Threading;
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

        private static int _downloads = 0;

        static Program()
        {
            _payload = new byte[_bytesPerMessage];

            // Initialize payload with stable random data since all-zeros may be compressed or optimized
            (new Random(0)).NextBytes(_payload);

            _payloadStream = new MemoryStream(_payload, writable:false);
        }

        public class Options
        {
            [Option("debug")]
            public bool Debug { get; set; }

            [Option('d', "duration", Default = 10)]
            public int Duration { get; set; }

            [Option('p', "parallel", Default = 1, HelpText = "Number of tasks to execute in parallel")]
            public int Parallel { get; set; }

            [Option('u', "upload")]
            public bool Upload { get; set; }
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

            if (options.Upload)
            {
                await UploadAndVerifyDownload(client);
            }

            Console.WriteLine($"Downloading blob '{_containerName}/{_blobName}' with {options.Parallel} parallel task(s) for {options.Duration} second(s)...");
            Console.WriteLine();

            var duration = TimeSpan.FromSeconds(options.Duration);
            var cts = new CancellationTokenSource(duration);
            var token = cts.Token;

            var tasks = new Task[options.Parallel];
            var sw = Stopwatch.StartNew();
            for (var i=0; i < options.Parallel; i++)
            {
                tasks[i] = DownloadLoop(client, token);
            }
            _ = PrintStatus(token);
            await Task.WhenAll(tasks);
            sw.Stop();

            var elapsedSeconds = sw.Elapsed.TotalSeconds;
            var downloadsPerSecond = _downloads / elapsedSeconds;
            var megabytesPerSecond = (downloadsPerSecond * _bytesPerMessage) / (1024 * 1024);

            Console.WriteLine();
            Console.WriteLine($"Downloaded {_downloads} blobs of size {_bytesPerMessage} in {elapsedSeconds:N2}s " +
                        $"({downloadsPerSecond:N2} blobs/s, {megabytesPerSecond:N2} MB/s)");
        }

        static async Task DownloadLoop(BlobClient client, CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Download(client, token);
                    Interlocked.Increment(ref _downloads);
                }
                catch (OperationCanceledException)
                {
                }
            }
        }

        static async Task Download(BlobClient client, CancellationToken token)
        {
            var response = await client.DownloadAsync(token);
            await response.Value.Content.CopyToAsync(Stream.Null);
        }

        static async Task PrintStatus(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(TimeSpan.FromSeconds(1), token);
                    Console.WriteLine(_downloads);
                }
                catch (OperationCanceledException)
                {
                }
            }
        }

        static async Task UploadAndVerifyDownload(BlobClient client)
        {
            await client.UploadAsync(_payloadStream);

            var downloadResponse = await client.DownloadAsync();

            byte[] downloadPayload;
            using (var memoryStream = new MemoryStream((int)downloadResponse.Value.ContentLength))
            {
                await downloadResponse.Value.Content.CopyToAsync(memoryStream);
                downloadPayload = memoryStream.ToArray();
            }

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
