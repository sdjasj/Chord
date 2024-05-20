import asyncio
from aiolimiter import AsyncLimiter
import time
cnt = 0
async def async_function(limiter):
    global cnt
    async with limiter:
    # Your async function logic here
        # await asyncio.sleep(10)  # Placeholder for async logic
        cnt += 1
        print("{} Async function completed {}".format(time.time() - ref, cnt))
ref = time.time()
async def main():
    limiter = AsyncLimiter(5, 1)
    tasks = [async_function(limiter) for _ in range(130)]  # Create multiple tasks
    # for task in tasks:
    #     async with limiter:
    #         res = await task()
    #         print(res)
    
    await asyncio.gather(*tasks)
              # Concurrently execute tasks and gather results
      # Print results

# Run the event loop
asyncio.run(main())