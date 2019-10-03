using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;

namespace StorageMock
{
    public class PutHandler : Handler
    {

        private PutHandler()
        {
        }

        public static PutHandler Instance { get; } = new PutHandler();

        public override async Task ProcessRequest(HttpRequest request, HttpResponse response, Account account)
        {
            //PUT https://mikeharderperf.blob.core.windows.net/perfstress/uploadtest HTTP/1.1
            //Host: mikeharderperf.blob.core.windows.net
            //x-ms-blob-type: BlockBlob
            //x-ms-version: 2019-02-02
            //x-ms-client-request-id: 76423177-d0a4-4e74-a222-c8af42997722
            //x-ms-return-client-request-id: true
            //User-Agent: azsdk-net-Storage.Blobs/12.0.0-preview.3+399d6c245a0996265296ec9e49e9aa6960e24454 (.NET Core 3.0.0; Microsoft Windows 10.0.18362)
            //x-ms-date: Thu, 03 Oct 2019 17:40:00 GMT
            //Authorization: SharedKey mikeharderperf:<redacted>
            //Content-Length: 1024

            (var containerName, var blobName) = GetNames(request);

            var memoryStream = new MemoryStream();
            await request.Body.CopyToAsync(memoryStream);

            if (account.TryPut(containerName, blobName, memoryStream, out var blob))
            {
                await SendSuccessResponse(request, response, blob);
            }
            else
            {
                await SendFailureResponse(request, response);
            }
        }

        private Task SendSuccessResponse(HttpRequest request, HttpResponse response, Blob blob)
        {
#if DEBUG
            Console.WriteLine("PutHandler.SendSuccessResponse");
#endif

            //HTTP/1.1 201 Created

            // Common
            //Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            //x-ms-request-id: 613536e2-f01e-004b-3911-7ae675000000
            //x-ms-client-request-id: 76423177-d0a4-4e74-a222-c8af42997722
            //x-ms-version: 2019-02-02

            // Specific
            //Content-MD5: 1IbBQxpftsNd4a6CAVvHIQ==
            //Last-Modified: Thu, 03 Oct 2019 17:40:01 GMT
            //ETag: "0x8D74828B7C5A4F7"
            //x-ms-content-crc64: 8Od48H1qTIQ=
            //x-ms-request-server-encrypted: true

            // Auto
            //Transfer-Encoding: chunked
            //Date: Thu, 03 Oct 2019 17:40:01 GMT


            response.StatusCode = (int)HttpStatusCode.Created;

            SetCommonHeaders(request, response);

            response.Headers.Add("Content-MD5", blob.ContentMD5);
            response.Headers.Add("Last-Modified", blob.LastModified.ToString("r"));
            response.Headers.Add("ETag", blob.ETag);
            response.Headers.Add("x-ms-content-crc64", blob.ContentCrc64);
            response.Headers.Add("x-ms-request-server-encrypted", "true");

            return response.Body.WriteAsync(EmptyByteArray, 0, 0);
        }

        private Task SendFailureResponse(HttpRequest request, HttpResponse response)
        {
            //HTTP/1.1 404 The specified container does not exist.

            // Common
            //Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            //x-ms-request-id: f1bd1795-401e-0095-5536-7af293000000
            //x-ms-client-request-id: 1e1bc25a-1d1c-4e73-a5d7-328158ba09c0
            //x-ms-version: 2019-02-02

            // Specific
            //x-ms-error-code: ContainerNotFound

            // Auto
            //Transfer-Encoding: chunked
            //Date: Thu, 03 Oct 2019 22:01:01 GMT

            response.StatusCode = (int)HttpStatusCode.NotFound;

            SetCommonHeaders(request, response);

            response.Headers.Add("x-ms-error-code", "ContainerNotFound");

            return response.Body.WriteAsync(EmptyByteArray, 0, 0);
        }
    }
}
