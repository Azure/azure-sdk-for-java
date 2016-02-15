package com.example.jianghlu.azureresourcerunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.microsoft.azure.management.resources.models.Subscription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionSelection extends AppCompatActivity {
    private List<SubscriptionInfo> subscriptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_selection);

        Intent intent = getIntent();
        subscriptionList =
                (List<SubscriptionInfo>) intent.getSerializableExtra("subscriptions");
        ListView subListView = (ListView)findViewById(R.id.subscription_list);
        ArrayAdapter adapter = new ArrayAdapter<SubscriptionInfo>(this, android.R.layout.simple_list_item_2, android.R.id.text1, subscriptionList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                SubscriptionInfo sub = subscriptionList.get(position);
                text1.setText(sub.getSubscriptionName());
                text2.setText("ID: " + sub.getSubscriptionId() + " Under Tenant: " + sub.getTenantId());
                return view;
            }
        };
        subListView.setAdapter(adapter);
        subListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ResourceOperator.class);
                intent.putExtra("subscription", subscriptionList.get(position));
                startActivity(intent);
            }
        });
    }

}
