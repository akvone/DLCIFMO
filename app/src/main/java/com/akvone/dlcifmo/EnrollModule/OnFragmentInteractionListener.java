package com.akvone.dlcifmo.EnrollModule;

import android.support.v4.app.Fragment;

public interface OnFragmentInteractionListener {
    void sendDate(int day, int month, int year);
    void popFragmentStack();
    void changeFragment(Fragment fragment);
}
