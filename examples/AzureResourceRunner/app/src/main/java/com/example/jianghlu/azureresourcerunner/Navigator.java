package com.example.jianghlu.azureresourcerunner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.PageImpl;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountListResult;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.credentials.UserTokenCredentials;

public class Navigator extends AppCompatActivity {
    SubscriptionInfo subscription;
    ResourceManagementClient rClient;
    StorageManagementClient sClient;
    ComputeManagementClient cClient;

    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";
    private final static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigator);
        Intent intent = getIntent();
        subscription = (SubscriptionInfo) intent.getSerializableExtra("subscription");
        setTitle(subscription.getSubscriptionName());

        UserTokenCredentials credentials = new UserTokenCredentials(
                this, CLIENT_ID, "common", REDIRECT_URI, null
        );
        rClient = new ResourceManagementClientImpl(credentials);
        sClient = new StorageManagementClientImpl(credentials);
        cClient = new ComputeManagementClientImpl(credentials);
        rClient.setSubscriptionId(subscription.getSubscriptionId());
        sClient.setSubscriptionId(subscription.getSubscriptionId());
        cClient.setSubscriptionId(subscription.getSubscriptionId());
    }

    public void listResourceGroups(View view) {

        rClient.getResourceGroups().listAsync(null, null, new ServiceCallback<PageImpl<ResourceGroup>>() {
            @Override
            public void failure(Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to get resource groups", Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void success(final ServiceResponse<PageImpl<ResourceGroup>> result) {
                final ListView listView = (ListView) findViewById(R.id.main_content);
                final ArrayAdapter adapter = new ArrayAdapter<ResourceGroup>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, result.getBody().getItems()) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        ResourceGroup group = result.getBody().getItems().get(position);
                        text1.setText(group.getName());
                        text2.setText("Location: " + group.getLocation());
                        return view;
                    }
                };
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    public void listStorageAccounts(View view) {
        sClient.getStorageAccounts().listAsync(new ServiceCallback<StorageAccountListResult>() {
            @Override
            public void failure(Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to get resource groups", Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void success(final ServiceResponse<StorageAccountListResult> result) {
                final ListView listView = (ListView) findViewById(R.id.main_content);
                final ArrayAdapter adapter = new ArrayAdapter<StorageAccount>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, result.getBody().getValue()) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        StorageAccount account = result.getBody().getValue().get(position);
                        text1.setText(account.getName());
                        text2.setText("Type: " + account.getType() + "\t" + "Location: " + account.getPrimaryLocation() + ", " + account.getSecondaryLocation());
                        return view;
                    }
                };
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    public void listVirtualMachines(View view) {
    }
}
