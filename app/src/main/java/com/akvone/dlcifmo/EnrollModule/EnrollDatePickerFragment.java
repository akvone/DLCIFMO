package com.akvone.dlcifmo.EnrollModule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.akvone.dlcifmo.OnFragmentInteractionListener;
import com.akvone.dlcifmo.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EnrollDatePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnrollDatePickerFragment extends Fragment {
    private static final int LAYOUT = R.layout.enroll_date_picker;

    private View view;

    private OnFragmentInteractionListener mListener;

    public EnrollDatePickerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EnrollDatePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnrollDatePickerFragment newInstance() {
        EnrollDatePickerFragment fragment = new EnrollDatePickerFragment();
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
        view = inflater.inflate(LAYOUT, container, false);
        final DatePicker picker = (DatePicker) view.findViewById(R.id.registrationDatePicker);
        Button send = (Button) view.findViewById(R.id.datePicked);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.sendDate(picker.getDayOfMonth(), picker.getMonth(), picker.getYear());
                }
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
