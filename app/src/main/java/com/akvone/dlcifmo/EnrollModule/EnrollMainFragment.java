package com.akvone.dlcifmo.EnrollModule;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.akvone.dlcifmo.OnFragmentInteractionListener;
import com.akvone.dlcifmo.R;

import java.util.Calendar;

/**
 * Created by 1 on 20.08.2016.
 */
public class EnrollMainFragment extends Fragment{
    private static final int LAYOUT = R.layout.enroll_date_picker;
    private String TAG = "Enroll fragment";

    private View view;

    private OnFragmentInteractionListener mListener;

    public EnrollMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EnrollDatePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnrollMainFragment newInstance() {
        EnrollMainFragment fragment = new EnrollMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(LAYOUT, container, false);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.datePickerFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollDatePickerFragment dialogFragment = new EnrollDatePickerFragment();
                dialogFragment.setListener(mListener);
                dialogFragment.show(getActivity().getSupportFragmentManager(), null);
            }
        });
        return view;
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

}
