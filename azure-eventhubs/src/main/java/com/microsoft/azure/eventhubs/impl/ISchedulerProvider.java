package com.microsoft.azure.eventhubs.impl;

interface ISchedulerProvider {

    ReactorDispatcher getReactorScheduler();
}
