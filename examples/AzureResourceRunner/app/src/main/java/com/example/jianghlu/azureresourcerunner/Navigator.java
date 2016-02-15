package com.example.jianghlu.azureresourcerunner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementClientImpl;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.List;

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
                this, CLIENT_ID, subscription.getTenantId(), REDIRECT_URI
        );
        rClient = new ResourceManagementClientImpl(credentials);
        sClient = new StorageManagementClientImpl(credentials);
        cClient = new ComputeManagementClientImpl(credentials);
        rClient.setSubscriptionId(subscription.getSubscriptionId());
        sClient.setSubscriptionId(subscription.getSubscriptionId());
        cClient.setSubscriptionId(subscription.getSubscriptionId());
    }

    public void listResourceGroups(View view) {
        Intent intent = new Intent(this, ResourceOperator.class);
        startActivity(intent);
//        final ProgressDialog progress = new ProgressDialog(this);
//        progress.setTitle("Please wait...");
//        progress.setMessage("Loading resource groups...");
//        rClient.getResourceGroupsOperations().listAsync(null, null, new ListOperationCallback<ResourceGroup>() {
//            @Override
//            public void failure(Throwable t) {
//                progress.dismiss();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(getApplicationContext(), "Failed to get resource groups", Toast.LENGTH_LONG);
//                    }
//                });
//            }
//
//            @Override
//            public void success(final ServiceResponse<List<ResourceGroup>> result) {
//                final ListView listView = (ListView) findViewById(R.id.main_content);
//                final ArrayAdapter adapter = new ArrayAdapter<ResourceGroup>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, result.getBody()) {
//                    @Override
//                    public View getView(int position, View convertView, ViewGroup parent) {
//                        View view = super.getView(position, convertView, parent);
//                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
//                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
//
//                        ResourceGroup group = result.getBody().get(position);
//                        text1.setText(group.getName());
//                        text2.setText("Location: " + group.getLocation());
//                        return view;
//                    }
//                };
//                progress.dismiss();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        listView.setAdapter(adapter);
//                    }
//                });
//            }
//        });
    }

    public void listStorageAccounts(View view) {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Please wait...");
        progress.setMessage("Loading storage accounts...");
        sClient.getStorageAccountsOperations().listAsync(new ServiceCallback<List<StorageAccount>>() {
            @Override
            public void failure(Throwable t) {
                progress.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to get storage accounts", Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void success(final ServiceResponse<List<StorageAccount>> result) {
                final ListView listView = (ListView) findViewById(R.id.main_content);
                final ArrayAdapter adapter = new ArrayAdapter<StorageAccount>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, result.getBody()) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        StorageAccount account = result.getBody().get(position);
                        text1.setText(account.getName());
                        text2.setText("Type: " + account.getType() + "\t" + "Location: " + account.getPrimaryLocation() + ", " + account.getSecondaryLocation());
                        return view;
                    }
                };
                progress.dismiss();
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
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Please wait...");
        progress.setMessage("Loading storage accounts...");
        cClient.getVirtualMachinesOperations().listAllAsync(new ListOperationCallback<VirtualMachine>() {
            @Override
            public void failure(Throwable t) {
                progress.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Failed to get virtual machines", Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void success(final ServiceResponse<List<VirtualMachine>> result) {
                final ListView listView = (ListView) findViewById(R.id.main_content);
                final ArrayAdapter adapter = new ArrayAdapter<VirtualMachine>(getApplicationContext(), android.R.layout.simple_list_item_2, android.R.id.text1, result.getBody()) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        VirtualMachine vm = result.getBody().get(position);
                        text1.setText(vm.getName());
                        String desc = "";
                        if (vm.getPlan() != null) {
                            desc = desc + "Plan: " + vm.getPlan().getName() + "\t";
                        }
                        if (vm.getHardwareProfile() != null) {
                            desc = desc + "Size: " + vm.getHardwareProfile().getVmSize() + "\t";
                        }
                        desc += "Location: " + vm.getLocation();
                        text2.setText(desc);
                        return view;
                    }
                };
                progress.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
