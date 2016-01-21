package com.example.jianghlu.azureresourcerunner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.azure.management.resources.SubscriptionClient;
import com.microsoft.azure.management.resources.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.SubscriptionsOperations;
import com.microsoft.azure.management.resources.models.PageImpl;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.TenantIdDescription;
import com.microsoft.azure.Page;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;

import java.net.CookieManager;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";
    private final static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    private UserTokenCredentials credentials;
    private ArrayList<SubscriptionInfo> subscriptionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        final Activity self = this;
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Please wait...");
        progress.setMessage("We are loading all your tenants and subscriptions...");
        credentials = new UserTokenCredentials(
                self, CLIENT_ID, "common", REDIRECT_URI, PromptBehavior.REFRESH_SESSION, AzureEnvironment.AZURE
        );
        final TextView textOutput = (TextView)findViewById(R.id.text_output);
        SubscriptionClient client = new SubscriptionClientImpl(credentials);
        client.getTenants().listAsync(new ServiceCallback<PageImpl<TenantIdDescription>>() {
            @Override
            public void failure(final Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textOutput.setText("Error " + t.getMessage());
                    }
                });
            }

            @Override
            public void success(final ServiceResponse<PageImpl<TenantIdDescription>> result) {
                try {
                    subscriptionList = new ArrayList<>();
                    for (TenantIdDescription tenant : result.getBody().getItems()) {
                        credentials = new UserTokenCredentials(
                                self, CLIENT_ID, tenant.getTenantId(), REDIRECT_URI
                        );
                        SubscriptionsOperations subOps = new SubscriptionClientImpl(credentials).getSubscriptions();
                        Page<Subscription> response = subOps.list().getBody();
                        for (Subscription sub : response.getItems()) {
                            subscriptionList.add(new SubscriptionInfo(tenant.getTenantId(), sub.getSubscriptionId(), sub.getDisplayName()));
                        }
                    }
                    Intent intent = new Intent(getApplicationContext(), SubscriptionSelection.class);
                    intent.putExtra("subscriptions", subscriptionList);
                    progress.dismiss();
                    startActivity(intent);
                } catch (Throwable t) {
                    failure(t);
                }
            }
        });
        progress.show();
    }

    public void logout(View view) {
        if (this.credentials != null) {
            credentials.setToken(null);
        }
        UserTokenCredentials.clearTokenCache();
        new CookieManager().getCookieStore().removeAll();
        Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_LONG).show();
    }
}
