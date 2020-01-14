using Microsoft.AspNetCore.Http;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace StorageMock
{
    public abstract class Handler
    {
        protected byte[] EmptyByteArray { get; } = new byte[0];

        protected Handler()
        {
        }

        public abstract Task ProcessRequest(HttpRequest request, HttpResponse response, Account account);

        protected (string containerName, string blobName) GetNames(HttpRequest request)
        {
            var pathParts = request.Path.Value.Split('/');
            return (pathParts[0], pathParts[1]);
        }

        protected void SetCommonHeaders(HttpRequest request, HttpResponse response)
        {
            var requestHeaders = request.Headers;            
            var headers = response.Headers;

            headers.Add("Server", "Windows-Azure-Blob/1.0 Microsoft-HTTPAPI/2.0");
            headers.Add("x-ms-request-id", Guid.NewGuid().ToString());
            headers.Add("x-ms-client-request-id", requestHeaders["x-ms-client-request-id"].FirstOrDefault());
            headers.Add("x-ms-version", "2019-02-02");
        }
    }
}
