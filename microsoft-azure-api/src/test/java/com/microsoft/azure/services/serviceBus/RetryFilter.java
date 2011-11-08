package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.http.ServiceFilter;

public class RetryFilter implements ServiceFilter {

	public Response handle(Request request, Next next) {
		Response response = null;
		Exception error = null;

		for (int retryCount = 0; retryCount != 3; ++retryCount) {
			try {
				response = next.handle(request);
			} catch (Exception ex) {
				error = ex;
			}

			boolean shouldRetry = false;
			// TODO policy
			if (!shouldRetry)
				break;
		}
		if (response == null && error != null) {
			// TODO define correct way to rethrow
			throw new RuntimeException(error);
		}
		return response;
	}

}
