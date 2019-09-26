using Azure.Storage.Common;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class ParallelTransferTest<TOptions> : RandomDataTest<TOptions> where TOptions : ParallelTransferOptionsOptions
    {
        public ParallelTransferTest(TOptions options) : base(options)
        {
            ParallelTransferOptions = new ParallelTransferOptions()
            {
                MaximumThreadCount = options.MaximumThreadCount,
                MaximumTransferLength = options.MaximumTransferLength
            };
        }

        protected ParallelTransferOptions ParallelTransferOptions { get; private set; }
    }
}
