package com.example.jianghlu.azureresourcerunner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.credentials.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.resources.SubscriptionClient;
import com.microsoft.azure.management.resources.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.SubscriptionsOperations;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.TenantIdDescription;
import com.microsoft.rest.ServiceResponse;

import java.lang.ref.WeakReference;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";
    private final static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
    public ProgressDialog progress;
    public TenantHandler tenantHandler;
    public SubscriptionHandler subscriptionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.progress = new ProgressDialog(this);
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
        final MainActivity self = this;
        progress.setTitle("Please wait...");
        progress.setMessage("We are loading all your tenants and subscriptions...");
        UserTokenCredentials credentials = new UserTokenCredentials(
                self, CLIENT_ID, "common", REDIRECT_URI, PromptBehavior.REFRESH_SESSION, AzureEnvironment.AZURE
        );
        SubscriptionClient client = new SubscriptionClientImpl(credentials);
        tenantHandler = new TenantHandler(new WeakReference<>(self));
        subscriptionHandler = new SubscriptionHandler(new WeakReference<>(self));
        client.getTenantsOperations().listAsync(new ListOperationCallback<TenantIdDescription>() {
            @Override
            public void failure(final Throwable t) {
                abort("Error " + t.getMessage());
            }

            @Override
            public void success(final ServiceResponse<List<TenantIdDescription>> serviceResponse) {
                self.tenantHandler.obtainMessage(0, serviceResponse.getBody()).sendToTarget();
            }
        });
        progress.show();
    }

    public void logout(View view) {
        UserTokenCredentials.clearTokenCache();
        new CookieManager().getCookieStore().removeAll();
        Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_LONG).show();
    }

    public void abort(String msg) {
        final TextView textOutput = (TextView)findViewById(R.id.text_output);
        textOutput.setText(msg);
        progress.dismiss();
    }

    public static class TenantHandler extends Handler {
        private WeakReference<MainActivity> activityRef;

        public TenantHandler(WeakReference<MainActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void handleMessage(Message input) {
            final LinkedList<TenantIdDescription> tenants = new LinkedList<>((List<TenantIdDescription>) input.obj);
            for (final TenantIdDescription tenant : tenants) {
                UserTokenCredentials credentials = new UserTokenCredentials(
                        activityRef.get(), CLIENT_ID, tenant.getTenantId(), REDIRECT_URI
                );
                SubscriptionsOperations subOps = new SubscriptionClientImpl(credentials).getSubscriptionsOperations();
                subOps.listAsync(new ListOperationCallback<Subscription>() {
                    @Override
                    public void failure(Throwable throwable) {
                        activityRef.get().abort("Error " + throwable.getMessage());
                    }

                    @Override
                    public void success(ServiceResponse<List<Subscription>> serviceResponse) {
                        activityRef.get().subscriptionHandler.obtainMessage(
                                tenants.size(),
                                new Pair<>(tenant, serviceResponse.getBody()))
                            .sendToTarget();
                    }
                });
            }
        }
    }

    public static class SubscriptionHandler extends Handler {
        private WeakReference<MainActivity> activityRef;
        private ArrayList<SubscriptionInfo> subscriptionInfos = new ArrayList<>();
        private int tenantCount = 0;

        public SubscriptionHandler(WeakReference<MainActivity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void handleMessage(Message input) {
            Pair<TenantIdDescription, List<Subscription>> pair = (Pair<TenantIdDescription, List<Subscription>>) input.obj;
            TenantIdDescription tenant = pair.first;
            List<Subscription> subscriptions = pair.second;
            for (Subscription sub : subscriptions) {
                subscriptionInfos.add(new SubscriptionInfo(tenant.getTenantId(), sub.getSubscriptionId(), sub.getDisplayName()));
            }
            if (++tenantCount == input.what) {
                Intent intent = new Intent(activityRef.get(), SubscriptionSelection.class);
                intent.putExtra("subscriptions", subscriptionInfos);
                activityRef.get().progress.dismiss();
                activityRef.get().startActivity(intent);
            }
        }
    }
}
