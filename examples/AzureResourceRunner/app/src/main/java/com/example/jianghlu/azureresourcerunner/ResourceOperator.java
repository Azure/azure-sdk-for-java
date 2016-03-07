package com.example.jianghlu.azureresourcerunner;

import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementClientImpl;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementClientImpl;

public class ResourceOperator extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ResourceFragment.OnFragmentInteractionListener {
    SubscriptionInfo subscription;
    ResourceManagementClient rClient;
    StorageManagementClient sClient;
    ComputeManagementClient cClient;

    private final static String CLIENT_ID = "1950a258-227b-4e31-a9cf-717495945fc2";
    private final static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_operator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);


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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.resource_operator, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_resource) {
            ResourceFragment fragment = new ResourceFragment();
            fragment.setRetainInstance(true);
            fragment.setResourceManagementClient(rClient);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.resource_operator_main_content, fragment)
                    .commit();

            // Highlight the selected item, update the title, and close the drawer
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(0);
            setTitle(this.getTitle() + " - Resources");
            drawer.closeDrawer(navigationView);
        } else if (id == R.id.nav_storage) {
//            StorageFragment fragment = new StorageFragment();
//            fragment.setRetainInstance(true);
//            fragment.setStorageManagementClient(sClient);
//            FragmentManager fragmentManager = getFragmentManager();
//            fragmentManager.beginTransaction()
//                    .replace(R.id.resource_operator_main_content, fragment)
//                    .commit();
//
//            // Highlight the selected item, update the title, and close the drawer
//            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//            navigationView.setCheckedItem(0);
//            setTitle(this.getTitle() + " - Storage");
//            drawer.closeDrawer(navigationView);
        } else if (id == R.id.nav_compute) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
