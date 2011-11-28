package com.microsoft.windowsazure.services.serviceBus.implementation;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.management.timer.Timer;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.utils.DateFactory;

public class WrapTokenManager {

    WrapContract contract;
    private final DateFactory dateFactory;
    private final String uri;
    private final String name;
    private final String password;
    private final String scope;

    private ActiveToken activeToken;

    @Inject
    public WrapTokenManager(WrapContract contract, DateFactory dateFactory, @Named("wrap.uri") String uri, @Named("wrap.scope") String scope,
            @Named("wrap.name") String name, @Named("wrap.password") String password) {
        this.contract = contract;
        this.dateFactory = dateFactory;
        this.uri = uri;
        this.scope = scope;
        this.name = name;
        this.password = password;
    }

    /**
     * @return the contract
     */
    public WrapContract getContract() {
        return contract;
    }

    /**
     * @param contract
     *            the contract to set
     */
    public void setContract(WrapContract contract) {
        this.contract = contract;
    }

    public String getAccessToken() throws ServiceException {
        Date now = dateFactory.getDate();
        ActiveToken active = this.activeToken;

        if (active != null && now.before(active.getExpiresUtc())) {
            return active.getWrapResponse().getAccessToken();
        }

        WrapAccessTokenResult wrapResponse = getContract().wrapAccessToken(uri, name, password, scope);
        Date expiresUtc = new Date(now.getTime() + wrapResponse.getExpiresIn() * Timer.ONE_SECOND / 2);

        ActiveToken acquired = new ActiveToken();
        acquired.setWrapResponse(wrapResponse);
        acquired.setExpiresUtc(expiresUtc);
        this.activeToken = acquired;

        return wrapResponse.getAccessToken();
    }

    class ActiveToken {
        Date expiresUtc;
        WrapAccessTokenResult wrapResponse;

        /**
         * @return the expiresUtc
         */
        public Date getExpiresUtc() {
            return expiresUtc;
        }

        /**
         * @param expiresUtc
         *            the expiresUtc to set
         */
        public void setExpiresUtc(Date expiresUtc) {
            this.expiresUtc = expiresUtc;
        }

        /**
         * @return the wrapResponse
         */
        public WrapAccessTokenResult getWrapResponse() {
            return wrapResponse;
        }

        /**
         * @param wrapResponse
         *            the wrapResponse to set
         */
        public void setWrapResponse(WrapAccessTokenResult wrapResponse) {
            this.wrapResponse = wrapResponse;
        }
    }

}
