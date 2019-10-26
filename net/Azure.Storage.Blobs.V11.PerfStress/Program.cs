using Azure.Test.PerfStress;
using System;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.V11.PerfStress
{
    class Program
    {
        static async Task Main(string[] args)
        {
            await PerfStressProgram.Main(typeof(Program).Assembly, args);
        }
    }
}
