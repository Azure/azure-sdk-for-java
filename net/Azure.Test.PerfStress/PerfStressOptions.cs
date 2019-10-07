using CommandLine;

namespace Azure.Test.PerfStress
{
    public class PerfStressOptions
    {
        [Option("sync")]
        public bool Sync { get; set; }

        [Option('d', "duration", Default = 10, HelpText = "Duration of test in seconds")]
        public int Duration { get; set; }

        [Option('p', "parallel", Default = 1, HelpText = "Number of operations to execute in parallel")]
        public int Parallel { get; set; }
    }
}
