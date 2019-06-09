package com.azure.data.cosmos;

public class CosmosUserResponse extends CosmosResponse<CosmosUserSettings> {
    private CosmosUser user;
    
    CosmosUserResponse(ResourceResponse<User> response, CosmosDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosUserSettings(response));
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
     * @return {@link CosmosUserSettings}
     */
    public CosmosUserSettings settings(){
        return resourceSettings();
    }
}
