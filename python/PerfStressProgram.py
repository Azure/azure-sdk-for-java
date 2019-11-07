import asyncio
import logging

from core.PerfStressRunner import PerfStressRunner

if __name__ == '__main__':  
    main_loop = PerfStressRunner()
    asyncio.run(main_loop.RunAsync())
