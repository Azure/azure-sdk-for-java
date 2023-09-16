package com.azuresamples;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azure.data.appconfiguration.implementation.ClientConstants;
import com.azuresamples.appconfiguration.HelloWorld;
import com.azuresamples.appconfiguration.SecretReferenceConfigurationSettingSample;
//import com.azure.data.appconfiguration.CreateSnapshot;
import com.azuresamples.appconfiguration.WatchFeature;
import com.azuresamples.appconfiguration.ConditionalRequestAsync;


import com.azuresamples.storage.BasicExample;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    private final String[] keyvaultCredentials = {"Put keyvault credential string here here"};
    private final String[] appconfigCredentials = {"Put appconfig credential string here"};
    private final String[] storageCredentials = {"Put storage endpoint here", "Put storage secret key here"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // appconfig sample block
        HelloWorld.main(appconfigCredentials);
        WatchFeature.main(appconfigCredentials);
        //CreateSnapshot.main(appConfigCredentials);
        SecretReferenceConfigurationSettingSample.main(appconfigCredentials);

        try {
            ConditionalRequestAsync.main(appconfigCredentials);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // storage-blob sample block
        try {
            BasicExample.main(storageCredentials);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
