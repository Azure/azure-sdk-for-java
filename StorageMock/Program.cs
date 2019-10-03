using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.WebUtilities;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Threading.Tasks;

namespace StorageMock
{
    class Program
    {
        private static readonly Dictionary<string, (byte[], DateTime)> _files = new Dictionary<string, (byte[], DateTime)>();

        private static readonly byte[] _randomBytes = new byte[8 * 1024 * 1024];

        static Program()
        {
            (new Random(0)).NextBytes(_randomBytes);
        }

        static void Main(string[] args)
        {
            Console.WriteLine("Connection String (unencrypted): \"BlobEndpoint=http://<hostname>:5000\"");
            Console.WriteLine("Connection String (encrypted): \"BlobEndpoint=https://<hostname>:5001\"");
            Console.WriteLine();

            new WebHostBuilder()
                .UseKestrel(options =>
                {
                    options.Listen(IPAddress.Any, 5000);
                    options.Listen(IPAddress.Any, 5001, listenOptions =>
                    {
                        listenOptions.UseHttps("testCert.pfx", "testPassword");
                    });
                })
                .UseContentRoot(Directory.GetCurrentDirectory())
                .Configure(app => app.Run(async context =>
                {
                    var request = context.Request;
                    var response = context.Response;
                    if (request.Method == HttpMethods.Put)
                    {
                        // await Put(request, response);
                        await PutNoOp(request, response);
                    }
                    else if (request.Method == HttpMethods.Head)
                    {
                        Head(request, response);
                    }
                    else if (request.Method == HttpMethods.Get)
                    {
                        if (request.Path == "/debug")
                        {
                            await Debug(request, response);
                        }
                        else
                        {
                            // await Get(request, response);
                            await GetRandom(request, response);
                        }
                    }

                }))
                .Build()
                .Run();
        }

        private static async Task Put(HttpRequest request, HttpResponse response)
        {
            byte[] bodyContent;

            using (var memoryStream = new MemoryStream())
            {
                await request.Body.CopyToAsync(memoryStream);
                bodyContent = memoryStream.ToArray();
            }

            _files[request.Path] = (bodyContent, DateTime.UtcNow);

            // HTTP/1.1 201 Created
            // Content-MD5: 1IbBQxpftsNd4a6CAVvHIQ==
            // Last-Modified: Tue, 27 Aug 2019 01:24:52 GMT
            // ETag: "0x8D72A8D5CA707D9"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 9dd11a98-f01e-00af-5976-5ce8eb000000
            // x-ms-version: 2018-11-09
            // x-ms-request-server-encrypted: true
            // Date: Tue, 27 Aug 2019 01:24:51 GMT
            // Content-Length: 0

            response.StatusCode = (int)HttpStatusCode.Created;

            var headers = response.Headers;

            // TODO: Compute MD5
            headers.Add("Content-MD5", "1IbBQxpftsNd4a6CAVvHIQ==");

            // Current time in RFC1123 format ("Mon, 15 Jun 2009 20:45:30 GMT")
            headers.Add("Last-Modified", DateTime.Now.ToString("r"));

            // TODO: Compute ETag
            headers.Add("ETag", "\"0x8D72A8D5CA707D9\"");

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            // TODO: Compute GUID
            headers.Add("x-ms-request-id", "9dd11a98-f01e-00af-5976-5ce8eb000000");

            headers.Add("x-ms-version", "2018-11-09");

            headers.Add("x-ms-request-server-encrypted", "true");
        }

        private static async Task PutNoOp(HttpRequest request, HttpResponse response)
        {
            var comp = request.Query["comp"].FirstOrDefault();

            if (comp == "block")
            {
                await PutNoOpBlock(request, response);
            }
            else
            {
                await PutNoOpBlockList(request, response);
            }
        }

        private static async Task PutNoOpBlockList(HttpRequest request, HttpResponse response)
        {
            // PUT /testcontainer/downloadtest?comp=blocklist HTTP/1.1
            // Host: mikeharderperf.blob.core.windows.net
            // x-ms-version: 2019-02-02
            // x-ms-client-request-id: 6581499a-f904-456f-840e-0e271bb3a59a
            // x-ms-return-client-request-id: true
            // User-Agent: azsdk-net-Storage.Blobs/12.0.0-preview.3+399d6c245a0996265296ec9e49e9aa6960e24454 (.NET Core 3.0.0; Microsoft Windows 10.0.18362)
            // x-ms-date: Wed, 02 Oct 2019 21:05:23 GMT
            // Authorization: SharedKey mikeharderperf:<redacted>
            // Content-Type: application/xml
            // Content-Length: 21523

            // HTTP/1.1 201 Created
            // Transfer-Encoding: chunked
            // Last-Modified: Wed, 02 Oct 2019 21:05:24 GMT
            // ETag: "0x8D7477C3E3EB2E7"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 70bf0067-801e-006e-2965-794f09000000
            // x-ms-client-request-id: 6581499a-f904-456f-840e-0e271bb3a59a
            // x-ms-version: 2019-02-02
            // x-ms-content-crc64: QW/CmemQrxo=
            // x-ms-request-server-encrypted: true
            // Date: Wed, 02 Oct 2019 21:05:23 GMT

#if DEBUG
            Console.WriteLine("PutNoOpBlockList");
#endif

            await request.Body.CopyToAsync(Stream.Null);

            response.StatusCode = (int)HttpStatusCode.Created;

            var headers = response.Headers;

            headers.Add("Last-Modified", "Wed, 02 Oct 2019 21:05:24 GMT");
            headers.Add("ETag", "0x8D7477C3E3EB2E7");
            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");
            headers.Add("x-ms-request-id", "70befdba-801e-006e-1865-794f09000000");
            headers.Add("x-ms-client-request-id", "ee6e66c7-8a75-4593-b2ee-af41159f4ccd");
            headers.Add("x-ms-version", "2019-02-02");
            headers.Add("x-ms-content-crc64", "pmVjxOmkx3I=");
            headers.Add("x-ms-request-server-encrypted", "true");

            await response.Body.WriteAsync(_randomBytes, 0, 0);
        }

        private static async Task PutNoOpBlock(HttpRequest request, HttpResponse response)
        {
            // PUT https://mikeharderperf.blob.core.windows.net/testcontainer/downloadtest?comp=block&blockid=QmxvY2tfMDAwMDE%3D HTTP/1.1
            // Host: mikeharderperf.blob.core.windows.net
            // x-ms-version: 2019-02-02
            // x-ms-client-request-id: ee6e66c7-8a75-4593-b2ee-af41159f4ccd
            // x-ms-return-client-request-id: true
            // User-Agent: azsdk-net-Storage.Blobs/12.0.0-preview.3+399d6c245a0996265296ec9e49e9aa6960e24454 (.NET Core 3.0.0; Microsoft Windows 10.0.18362)
            // x-ms-date: Wed, 02 Oct 2019 21:05:17 GMT
            // Authorization: SharedKey mikeharderperf:<redacted>
            // Content-Length: 1048576

            // HTTP/1.1 201 Created
            // Transfer-Encoding: chunked
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 70befdba-801e-006e-1865-794f09000000
            // x-ms-client-request-id: ee6e66c7-8a75-4593-b2ee-af41159f4ccd
            // x-ms-version: 2019-02-02
            // x-ms-content-crc64: pmVjxOmkx3I=
            // x-ms-request-server-encrypted: true
            // Date: Wed, 02 Oct 2019 21:05:18 GMT

#if DEBUG
            var blockId = request.Query["blockid"].FirstOrDefault();
            Console.WriteLine("PutNoOpBlock: " + blockId);
#endif

            await request.Body.CopyToAsync(Stream.Null);

            response.StatusCode = (int)HttpStatusCode.Created;

            var headers = response.Headers;

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            headers.Add("x-ms-request-id", "70befdba-801e-006e-1865-794f09000000");
            headers.Add("x-ms-client-request-id", "ee6e66c7-8a75-4593-b2ee-af41159f4ccd");
            headers.Add("x-ms-version", "2019-02-02");
            headers.Add("x-ms-content-crc64", "pmVjxOmkx3I=");
            headers.Add("x-ms-request-server-encrypted", "true");

            await response.Body.WriteAsync(_randomBytes, 0, 0);
        }


        private static async Task Get(HttpRequest request, HttpResponse response)
        {
            // HTTP/1.1 206 Partial Content
            // Content-Length: 1024
            // Content-Type: application/octet-stream
            // Content-Range: bytes 0-1023/1024
            // Last-Modified: Tue, 27 Aug 2019 01:24:52 GMT
            // Accept-Ranges: bytes
            // ETag: "0x8D72A8D5CA707D9"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 9dd11a9d-f01e-00af-5d76-5ce8eb000000
            // x-ms-version: 2018-11-09
            // x-ms-tag-count: 0
            // x-ms-creation-time: Tue, 27 Aug 2019 01:23:14 GMT
            // x-ms-blob-content-md5: 1IbBQxpftsNd4a6CAVvHIQ==
            // x-ms-lease-status: unlocked
            // x-ms-lease-state: available
            // x-ms-blob-type: BlockBlob
            // x-ms-server-encrypted: true
            // Date: Tue, 27 Aug 2019 01:24:52 GMT

            var (content, lastUpdated) = _files[request.Path];

            response.StatusCode = (int)HttpStatusCode.PartialContent;
            response.ContentLength = content.Length;
            response.ContentType = "application/octet-stream";

            var headers = response.Headers;
            
            headers.Add("Content-Range", $"bytes 0-{content.Length - 1}/{content.Length}");

            // Last modified time in RFC1123 format ("Mon, 15 Jun 2009 20:45:30 GMT")
            headers.Add("Last-Modified", lastUpdated.ToString("r"));

            headers.Add("Accept-Ranges", "bytes");

            // TODO: Compute ETag
            headers.Add("ETag", "\"0x8D72A8D5CA707D9\"");

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            // TODO: Compute GUID
            headers.Add("x-ms-request-id", "9dd11a98-f01e-00af-5976-5ce8eb000000");

            headers.Add("x-ms-version", "2018-11-09");

            headers.Add("x-ms-tag-count", "0");

            // TODO: Track creation time separate from last updated time
            // Creation time in RFC1123 format ("Mon, 15 Jun 2009 20:45:30 GMT")
            headers.Add("x-ms-creation-time", lastUpdated.ToString("r"));

            // TODO: Compute MD5
            headers.Add("x-ms-blob-content-md5", "1IbBQxpftsNd4a6CAVvHIQ==");

            headers.Add("x-ms-lease-status", "unlocked");
            headers.Add("x-ms-lease-state", "available");
            headers.Add("x-ms-blob-type", "BlockBlob");
            headers.Add("x-ms-server-encrypted", "true");

            await response.Body.WriteAsync(content, 0, content.Length);
        }

        private static void Head(HttpRequest request, HttpResponse response)
        {
            // HEAD https://mikeharderperf.blob.core.windows.net/testcontainer/downloadtest HTTP/1.1
            // Host: mikeharderperf.blob.core.windows.net
            // x-ms-version: 2019-02-02
            // x-ms-client-request-id: 43b3869a-cebf-4b8a-a27d-98c38f4f2fae
            // x-ms-return-client-request-id: true
            // User-Agent: azsdk-net-Storage.Blobs/12.0.0-preview.3+399d6c245a0996265296ec9e49e9aa6960e24454 (.NET Core 3.0.0; Microsoft Windows 10.0.18362)
            // x-ms-date: Wed, 02 Oct 2019 21:05:23 GMT
            // Authorization: SharedKey mikeharderperf:<redacted>

            // HTTP/1.1 200 OK
            // Content-Length: 524288000
            // Content-Type: application/octet-stream
            // Last-Modified: Wed, 02 Oct 2019 21:05:24 GMT
            // Accept-Ranges: bytes
            // ETag: "0x8D7477C3E3EB2E7"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 70bf0070-801e-006e-3265-794f09000000
            // x-ms-client-request-id: 43b3869a-cebf-4b8a-a27d-98c38f4f2fae
            // x-ms-version: 2019-02-02
            // x-ms-creation-time: Wed, 02 Oct 2019 21:05:24 GMT
            // x-ms-lease-status: unlocked
            // x-ms-lease-state: available
            // x-ms-blob-type: BlockBlob
            // x-ms-server-encrypted: true
            // Date: Wed, 02 Oct 2019 21:05:23 GMT

#if DEBUG
            Console.WriteLine("Head");
#endif

            response.StatusCode = (int)HttpStatusCode.OK;
            response.ContentLength = 524288000;
            response.ContentType = "application/octet-stream";

            var headers = response.Headers;

            headers.Add("Last-Modified", "Wed, 02 Oct 2019 21:05:24 GMT");

            headers.Add("Accept-Ranges", "bytes");

            headers.Add("ETag", "0x8D7477C3E3EB2E7");

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            headers.Add("x-ms-request-id", "40dfc7e8-a01e-00ef-5365-79efd3000000");
            headers.Add("x-ms-client-request-id", "a8fe19f2-9276-42bd-9fe8-c0b851cd298e");

            headers.Add("x-ms-version", "2019-02-02");

            headers.Add("x-ms-creation-time", "Wed, 02 Oct 2019 21:05:24 GMT");

            headers.Add("x-ms-lease-status", "unlocked");
            headers.Add("x-ms-lease-state", "available");
            headers.Add("x-ms-blob-type", "BlockBlob");
            headers.Add("x-ms-server-encrypted", "true");
        }

        private static async Task GetRandom(HttpRequest request, HttpResponse response)
        {
            // GET https://mikeharderperf.blob.core.windows.net/testcontainer/downloadtest HTTP/1.1
            // Host: mikeharderperf.blob.core.windows.net
            // x-ms-version: 2019-02-02
            // x-ms-range: bytes=46137344-50331647
            // If-Match: "0x8D7477C3E3EB2E7"
            // x-ms-client-request-id: a8fe19f2-9276-42bd-9fe8-c0b851cd298e
            // x-ms-return-client-request-id: true
            // User-Agent: azsdk-net-Storage.Blobs/12.0.0-preview.3+399d6c245a0996265296ec9e49e9aa6960e24454 (.NET Core 3.0.0; Microsoft Windows 10.0.18362)
            // x-ms-date: Wed, 02 Oct 2019 21:05:23 GMT
            // Authorization: SharedKey mikeharderperf:<redacted>

            // HTTP/1.1 206 Partial Content
            // Content-Length: 4194304
            // Content-Type: application/octet-stream
            // Content-Range: bytes 46137344-50331647/524288000
            // Last-Modified: Wed, 02 Oct 2019 21:05:24 GMT
            // Accept-Ranges: bytes
            // ETag: "0x8D7477C3E3EB2E7"
            // Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            // x-ms-request-id: 40dfc7e8-a01e-00ef-5365-79efd3000000
            // x-ms-client-request-id: a8fe19f2-9276-42bd-9fe8-c0b851cd298e
            // x-ms-version: 2019-02-02
            // x-ms-creation-time: Wed, 02 Oct 2019 21:05:24 GMT
            // x-ms-lease-status: unlocked
            // x-ms-lease-state: available
            // x-ms-blob-type: BlockBlob
            // x-ms-server-encrypted: true
            // Date: Wed, 02 Oct 2019 21:05:24 GMT

#if DEBUG
            Console.WriteLine("GetRandom");
#endif

            var range = request.Headers["x-ms-range"].First();
            var rangeParts = range.Split('=', '-');
            var start = long.Parse(rangeParts[1]);
            var end = long.Parse(rangeParts[2]);
            var contentLength = end - start + 1;

            response.StatusCode = (int)HttpStatusCode.PartialContent;
            response.ContentLength = contentLength;
            response.ContentType = "application/octet-stream";

            var headers = response.Headers;

            headers.Add("Content-Range", $"bytes {start}-{end}/{524288000}");

            headers.Add("Last-Modified", "Wed, 02 Oct 2019 21:05:24 GMT");

            headers.Add("Accept-Ranges", "bytes");

            headers.Add("ETag", "0x8D7477C3E3EB2E7");

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");

            headers.Add("x-ms-request-id", "40dfc7e8-a01e-00ef-5365-79efd3000000");
            headers.Add("x-ms-client-request-id", "a8fe19f2-9276-42bd-9fe8-c0b851cd298e");

            headers.Add("x-ms-version", "2019-02-02");

            headers.Add("x-ms-creation-time", "Wed, 02 Oct 2019 21:05:24 GMT");

            headers.Add("x-ms-lease-status", "unlocked");
            headers.Add("x-ms-lease-state", "available");
            headers.Add("x-ms-blob-type", "BlockBlob");
            headers.Add("x-ms-server-encrypted", "true");

            await response.Body.WriteAsync(_randomBytes, 0, (int)contentLength);
        }


        private static Task Debug(HttpRequest request, HttpResponse response)
        {
            response.StatusCode = (int)HttpStatusCode.OK;

            foreach (var kvp in _files)
            {
                Console.WriteLine($"{kvp.Key} {kvp.Value.Item2}");
                var payload = kvp.Value.Item1;
                for (var i=0; i < payload.Length; i++)
                {
                    Console.Write(payload[i] + " ");
                }
                Console.WriteLine();
                Console.WriteLine();
            }

            return Task.CompletedTask;
        }
    }
}
