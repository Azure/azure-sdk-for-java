package com.microsoft.azure.auth.wrap;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.management.timer.Timer;

import com.microsoft.azure.ServiceException;
import com.microsoft.azure.auth.wrap.contract.WrapContract;
import com.microsoft.azure.auth.wrap.contract.WrapResponse;
import com.microsoft.azure.utils.DateFactory;

public class WrapClient {

    WrapContract contract;
    private DateFactory dateFactory;
    private String uri;
    private String name;
    private String password;
    private String scope;

    private ActiveToken activeToken;

    @Inject
    public WrapClient(
            WrapContract contract,
            DateFactory dateFactory,
            @Named("wrap.uri") String uri,
            @Named("wrap.scope") String scope,
            @Named("wrap.name") String name,
            @Named("wrap.password") String password) {
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

        WrapResponse wrapResponse = getContract().post(uri, name, password, scope);
        Date expiresUtc = new Date(now.getTime() + wrapResponse.getExpiresIn() * Timer.ONE_SECOND / 2);

        ActiveToken acquired = new ActiveToken();
        acquired.setWrapResponse(wrapResponse);
        acquired.setExpiresUtc(expiresUtc);
        this.activeToken = acquired;

        return wrapResponse.getAccessToken();
    }

    class ActiveToken {
        Date expiresUtc;
        WrapResponse wrapResponse;

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
        public WrapResponse getWrapResponse() {
            return wrapResponse;
        }

        /**
         * @param wrapResponse
         *            the wrapResponse to set
         */
        public void setWrapResponse(WrapResponse wrapResponse) {
            this.wrapResponse = wrapResponse;
        }
    }

}
