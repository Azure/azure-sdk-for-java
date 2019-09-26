using Azure.Test.PerfStress;

namespace Azure.Storage.Blobs.PerfStress
{
    class Program
    {
        static void Main(string[] args)
        {
            PerfStressProgram.Main(typeof(Program).Assembly, args);
        }
    }
}
