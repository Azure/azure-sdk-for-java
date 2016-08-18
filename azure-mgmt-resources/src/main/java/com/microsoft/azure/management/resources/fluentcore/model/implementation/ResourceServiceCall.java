package com.microsoft.azure.management.resources.fluentcore.model.implementation;


import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

/**
 * Type representing a service-call that returns Resource {@link Resource}.
 *
 * @param <FluentModelT>
 * @param <InnerModelT>
 * @param <FluentModelImplT>
 */
public class ResourceServiceCall<FluentModelT extends Resource,
        InnerModelT,
        FluentModelImplT extends CreatableUpdatableImpl<FluentModelT, InnerModelT, FluentModelImplT> & Resource> extends ServiceCall<FluentModelT> {
    private final FluentModelImplT fluentModelImpl;
    private SuccessHandler<InnerModelT> successHandler;
    private FailureHandler failureHandler;

    /**
     * Creates ResourceServiceCall.
     *
     * @param model the fluent model
     */
    public ResourceServiceCall(FluentModelImplT model) {
        super(null);
        this.fluentModelImpl = model;
    }

    /**
     * Wraps a callback.
     *
     * @param callback the callback to wrap
     * @return new callback that wraps the given callback
     */
    public ServiceCallback<InnerModelT> wrapCallBack(final ServiceCallback<FluentModelT> callback) {
        return wrapCallBack(callback, true);
    }

    /**
     * Wraps a callback.
     *
     * @param callback the callback to wrap
     * @param reportSuccess true to invoke callback.success on success
     * @return new callback that wraps the given callback
     */
    @SuppressWarnings("unchecked")
    public ServiceCallback<InnerModelT> wrapCallBack(final ServiceCallback<FluentModelT> callback, final boolean reportSuccess) {
        final ResourceServiceCall<FluentModelT, InnerModelT, FluentModelImplT> self = this;
        return new ServiceCallback<InnerModelT>() {
            @Override
            public void failure(Throwable t) {
                if (self.failureHandler != null) {
                    self.failureHandler.failure(t);
                }
                callback.failure(t);
                self.failure(t); // Signal Future
            }

            @Override
            public void success(ServiceResponse<InnerModelT> response) {
                self.fluentModelImpl.setInner(response.getBody());
                if (self.successHandler != null) {
                    self.successHandler.success(response);
                }
                if (reportSuccess) {
                    callback.success(new ServiceResponse<>((FluentModelT) self.fluentModelImpl, response.getResponse()));
                    self.success(new ServiceResponse<>((FluentModelT) self.fluentModelImpl, response.getResponse())); // Signal Future
                }
            }
        };
    }

    /**
     * Sets the success handler.
     *
     * @param handler the success handler.
     * @return ResourceServiceCall
     */
    public ResourceServiceCall<FluentModelT, InnerModelT, FluentModelImplT> withSuccessHandler(SuccessHandler<InnerModelT> handler) {
        this.successHandler = handler;
        return this;
    }

    /**
     * Sets the failure handler.
     *
     * @param handler the failure handler.
     * @return ResourceServiceCall
     */
    public ResourceServiceCall<FluentModelT, InnerModelT, FluentModelImplT> withFailureHandler(FailureHandler handler) {
        this.failureHandler = handler;
        return this;
    }

    /**
     * Handler to handle success.
     *
     * @param <T> the type of result
     */
    public interface SuccessHandler<T> {
        /**
         * handle success.
         *
         * @param response the response with result.
         */
        void success(ServiceResponse<T> response);
    }

    /**
     * Handler to handle failure.
     */
    public interface FailureHandler {
        /**
         * handle failure.
         *
         * @param t the exception.
         */
        void failure(Throwable t);
    }
}

