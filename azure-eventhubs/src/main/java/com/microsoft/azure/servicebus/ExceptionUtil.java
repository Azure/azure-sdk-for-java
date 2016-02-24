/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package com.microsoft.azure.servicebus;

import java.util.concurrent.*;
import org.apache.qpid.proton.amqp.transport.*;
import com.microsoft.azure.servicebus.amqp.*;

final class ExceptionUtil
{

	static Exception toException(ErrorCondition errorCondition)
	{
		if (errorCondition == null)
		{
			throw new IllegalArgumentException("'null' errorCondition cannot be translated to ServiceBusException");
		}
		
		if (errorCondition.getCondition() == ClientConstants.TIMEOUT_ERROR)
		{
			return new TimeoutException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.SERVER_BUSY_ERROR)
		{
			return new ServerBusyException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotFound)
		{
			return new IllegalEntityException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ENTITY_DISABLED_ERROR)
		{
			return new IllegalEntityException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.Stolen)
		{
			return new ReceiverDisconnectedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.UnauthorizedAccess)
		{
			return new AuthorizationFailedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.PayloadSizeExceeded)
		{
			return new PayloadSizeExceededException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.InternalError)
		{
			return ServiceBusException.create(false, new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == ClientConstants.ARGUMENT_ERROR)
		{
			return new IllegalArgumentException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ARGUMENT_OUT_OF_RANGE_ERROR)
		{
			return new IllegalArgumentException(errorCondition.getDescription(), new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotImplemented)
		{
			// TODO: ideally this should have been ToBeImplementedException
			return new UnsupportedOperationException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotAllowed)
		{
			return new UnsupportedOperationException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.PARTITION_NOT_OWNED_ERROR)
		{
			return ServiceBusException.create(false, errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.STORE_LOCK_LOST_ERROR)
		{
			return ServiceBusException.create(false, errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.AmqpLinkDetachForced)
		{
			return ServiceBusException.create(false, new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.ResourceLimitExceeded)
		{
			return ServiceBusException.create(false, new AmqpException(errorCondition));
		}
		
		return ServiceBusException.create(ClientConstants.DEFAULT_IS_TRANSIENT, errorCondition.getDescription());
	}
}
