package com.microsoft.azure.eventhubs.impl;

interface SchedulerProvider {

    ReactorDispatcher getReactorScheduler();
}
