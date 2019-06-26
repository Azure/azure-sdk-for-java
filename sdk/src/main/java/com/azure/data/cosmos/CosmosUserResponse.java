package com.azure.data.cosmos;

public class CosmosUserResponse extends CosmosResponse<CosmosUserProperties> {
    private CosmosUser user;
    
    CosmosUserResponse(ResourceResponse<User> response, CosmosDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosUserProperties(response));
            this.user = new CosmosUser(resourceSettings().id(), database);
        }
    }

    /**
     * Get cosmos user
     *
     * @return {@link CosmosUser}
     */
    public CosmosUser user() {
        return user;
    }

    /**
     * Gets the cosmos user settings
     *
     * @return {@link CosmosUserProperties}
     */
    public CosmosUserProperties settings(){
        return resourceSettings();
    }
}
