package com.akvone.dlcifmo.MainModule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.akvone.dlcifmo.LoginModule.LoginActivity;
import com.akvone.dlcifmo.R;

/**
 * Created on 05.08.2016.
 */
public class NonAuthorizedFragment extends Fragment{

    private static final String ARG_INFO = "param1";

    private String mParam1;

    public static NonAuthorizedFragment newInstance(){
        return newInstance("Войдите,\n чтобы воспользоваться данным разделом");
    }

    public static NonAuthorizedFragment newInstance(String param1) {
        NonAuthorizedFragment fragment = new NonAuthorizedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INFO, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_INFO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.blank_non_authorized, container, false);
        TextView t = (TextView) view.findViewById(R.id.title);
        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity().getApplicationContext(),LoginActivity.class));
                getActivity().finish();
            }
        });

        t.setText(mParam1);
        return view;
    }
}
