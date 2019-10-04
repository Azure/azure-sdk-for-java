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
using System.Net.Http;
using System.Net.Mime;
using System.Threading.Tasks;

namespace HttpMock
{
    class Program
    {
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
                .Configure(app => app.Run(context =>
                {
                    var request = context.Request;
                    var response = context.Response;

                    return Proxy.ProxyRequest(request, response);
                }))
                .Build()
                .Run();
        }
    }
}
