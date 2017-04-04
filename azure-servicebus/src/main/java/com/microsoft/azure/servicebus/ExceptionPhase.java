package com.microsoft.azure.servicebus;

public enum ExceptionPhase {
	RECEIVE,
	RENEWMESSAGELOCK,
	COMPLETE,
	ABANDON,
	USERCALLBACK,
	SESSIONCLOSE,
	ACCEPTSESSION,
	RENEWSESSIONLOCK
}
