import asyncio
import logging

from azure.ai.agentserver.responses import (
    CreateResponse,
    ResponseContext,
    ResponsesAgentServerHost,
    TextResponse,
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
app = ResponsesAgentServerHost()


@app.create_handler
async def handler(
    request: CreateResponse,
    context: ResponseContext,
    cancellation_signal: asyncio.Event,
):
    input_text = await context.get_input_text()
    logger.info("Received input: %s", input_text)
    return TextResponse(context, request, text=f"Echo: {input_text}")


def main() -> None:
    app.run()


if __name__ == "__main__":
    main()
