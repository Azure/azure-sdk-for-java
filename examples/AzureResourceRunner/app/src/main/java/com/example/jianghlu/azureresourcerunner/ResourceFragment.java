package com.example.jianghlu.azureresourcerunner;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.ListOperationCallback;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResourceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResourceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ResourceManagementClient resourceManagementClient;
    private View layout;

    public ResourceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResourceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResourceFragment newInstance(String param1, String param2) {
        ResourceFragment fragment = new ResourceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        listResourceGroups();
    }

    public void listResourceGroups() {
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Please wait...");
        progress.setMessage("Loading resource groups...");
        resourceManagementClient.getResourceGroupsOperations().listAsync(null, null, new ListOperationCallback<ResourceGroup>() {
            @Override
            public void failure(Throwable t) {
                progress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Failed to get resource groups", Toast.LENGTH_LONG);
                    }
                });
            }

            @Override
            public void success(final ServiceResponse<List<ResourceGroup>> result) {
                final ListView listView = (ListView) getActivity().findViewById(R.id.main_content);
                final ArrayAdapter adapter = new ArrayAdapter<ResourceGroup>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, result.getBody()) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        ResourceGroup group = result.getBody().get(position);
                        text1.setText(group.getName());
                        text2.setText("Location: " + group.getLocation());
                        return view;
                    }
                };
                progress.dismiss();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    public void createResourceGroup() {
        EditText name = (EditText) getActivity().findViewById(R.id.resource_group_name);
        EditText location = (EditText) getActivity().findViewById(R.id.resource_group_location);
        ResourceGroup resourceGroup = new ResourceGroup();
        resourceGroup.setLocation(location.getText().toString());
        resourceManagementClient.getResourceGroupsOperations().createOrUpdateAsync(name.getText().toString(), resourceGroup, new ServiceCallback<ResourceGroup>() {
            @Override
            public void failure(Throwable t) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Failed to create rg", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void success(ServiceResponse<ResourceGroup> result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Created", Toast.LENGTH_LONG).show();
                        listResourceGroups();
                    }
                });
            }
        });
    }

    public void deleteResourceGroup() {
        EditText name = (EditText) getActivity().findViewById(R.id.resource_group_name);
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Deleting resource group " + name.getText().toString() + ", please wait...");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.show();
            }
        });
        resourceManagementClient.getResourceGroupsOperations().deleteAsync(name.getText().toString(), new ServiceCallback<Void>() {
            @Override
            public void failure(Throwable t) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Failed to delete rg", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void success(ServiceResponse<Void> result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_LONG).show();
                        listResourceGroups();
                    }
                });
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_resource, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.resource_create_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createResourceGroup();
            }
        });
        view.findViewById(R.id.resource_delete_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteResourceGroup();
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setResourceManagementClient(ResourceManagementClient resourceManagementClient) {
        this.resourceManagementClient = resourceManagementClient;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
