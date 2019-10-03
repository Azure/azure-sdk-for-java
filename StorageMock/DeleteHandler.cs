using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;

namespace StorageMock
{
    public class DeleteHandler : Handler
    {
        private DeleteHandler()
        {
        }

        public static DeleteHandler Instance { get; } = new DeleteHandler();

        public override Task ProcessRequest(HttpRequest request, HttpResponse response, Account account)
        {
            //DELETE /testcontainer/uploadtest HTTP/1.1
            //Host: mikeharderperf.blob.core.windows.net
            //x-ms-version: 2019-02-02
            //x-ms-client-request-id: e683f8cb-5d75-474e-9544-53af108e996e
            //x-ms-return-client-request-id: true
            //User-Agent: azsdk-net-Storage.Blobs/12.0.0-preview.3+399d6c245a0996265296ec9e49e9aa6960e24454 (.NET Core 3.0.0; Microsoft Windows 10.0.18362)
            //x-ms-date: Thu, 03 Oct 2019 16:42:54 GMT
            //Authorization: SharedKey mikeharderperf:<redacted>

            (var containerName, var blobName) = GetNames(request);
            if (account.TryDelete(containerName, blobName))
            {
                return SendSuccessResponse(request, response);
            }
            else
            {
                return SendFailureResponse(request, response);
            }
        }

        private Task SendSuccessResponse(HttpRequest request, HttpResponse response)
        {
#if DEBUG
            Console.WriteLine("DeleteHandler.SendSuccessResponse");
#endif

            //HTTP/1.1 202 Accepted

            // Common
            //Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            //x-ms-request-id: 359321d9-801e-007e-1f09-7a8a61000000
            //x-ms-client-request-id: e683f8cb-5d75-474e-9544-53af108e996e
            //x-ms-version: 2019-02-02

            // Specific
            //x-ms-delete-type-permanent: true

            // Auto
            //Transfer-Encoding: chunked
            //Date: Thu, 03 Oct 2019 16:42:54 GMT

            response.StatusCode = (int)HttpStatusCode.Accepted;
            
            SetCommonHeaders(request, response);
            
            response.Headers.Add("x-ms-delete-type-permanent", "true");

            return response.Body.WriteAsync(EmptyByteArray, 0, 0);
        }

        private Task SendFailureResponse(HttpRequest request, HttpResponse response)
        {
#if DEBUG
            Console.WriteLine("DeleteHandler.SendFailureResponse");
#endif
            //HTTP/1.1 404 The specified blob does not exist.

            // Common
            //Server: Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0
            //x-ms-request-id: c81ac98b-b01e-0081-1009-7abafc000000
            //x-ms-client-request-id: 348f4512-9654-47aa-99ab-f9a4dc27d9a6
            //x-ms-version: 2019-02-02


            // Specific
            //Content-Length: 215
            //Content-Type: application/xml
            //x-ms-error-code: BlobNotFound

            // Auto
            //Date: Thu, 03 Oct 2019 16:42:53 GMT

            response.StatusCode = (int)HttpStatusCode.NotFound;
            
            SetCommonHeaders(request, response);
            
            // TODO: Response from live service is 215 bytes.  Maybe BOM?
            response.ContentLength = 212;
            response.ContentType = "application/xml";
            response.Headers.Add("x-ms-error-code", "BlobNotFound");

            return response.WriteAsync(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?><Error><Code>BlobNotFound</Code><Message>The specified blob does not exist.\n" +
                "RequestId:c81ac98b-b01e-0081-1009-7abafc000000\n" +
                "Time:2019-10-03T16:42:54.2421117Z</Message></Error>");
        }
    }
}
