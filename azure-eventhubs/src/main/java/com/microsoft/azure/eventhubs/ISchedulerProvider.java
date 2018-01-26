package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.amqp.ReactorDispatcher;

interface ISchedulerProvider {

    ReactorDispatcher getReactorScheduler();
}
