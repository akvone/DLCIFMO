package com.akvone.dlcifmo;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.akvone.dlcifmo.LoginModule.LoginActivity;
import com.akvone.dlcifmo.TestingRegistrationModule.DatePickerFragment;
import com.akvone.dlcifmo.TestingRegistrationModule.TimePickerFragment;
import com.akvone.dlcifmo.TopStudentsModule.TopStFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;

    Fragment journalFragment;
    Fragment changesProtocolFragment;
    TopStFragment topStFragment;
    Fragment enrollFragment;
    Fragment feedbackFragment;
    DatePickerFragment datePickerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Проверяем, пропускал ли пользователь авторизацию
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(getString(R.string.preference_skip_login_key),false)){

        }
        else {
            startLoginActivity();
        }

        setContentView(R.layout.activity_main);
        initToolbar();
        initDrawer();
        initNavigationView();

        loadFragments();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_container, journalFragment)
                .commit();
    }

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }
    private void initToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void initNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void loadFragments(){
        if (false) {
            journalFragment = BlankFragment.newInstance("Здесь будет журнал с баллами по предметам");
            changesProtocolFragment = BlankFragment.newInstance("Здесь будет протокол изменений");
            enrollFragment = BlankFragment.newInstance("Здесь будет запись на тестирование");
        }
        else{
            journalFragment = NonAuthorizedFragment.newInstance();
            changesProtocolFragment = NonAuthorizedFragment.newInstance();
            enrollFragment = NonAuthorizedFragment.newInstance();
        }
        topStFragment = new TopStFragment();
        feedbackFragment = BlankFragment.newInstance("Здесь будет обратная связь");
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        getSupportActionBar().setTitle(item.getTitle());
        if (id == R.id.gradebook) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, journalFragment)
                    .commit();
        } else if (id == R.id.change_protocol) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, changesProtocolFragment)
                    .commit();
        } else if (id == R.id.top_student) {
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.main_activity_container))
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, topStFragment)
                    .commit();
        } else if (id == R.id.testing_registration) {
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.main_activity_container))
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, datePickerFragment)
                    .replace(R.id.main_activity_container, enrollFragment)
                    .commit();
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        } else if (id == R.id.feedback) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, feedbackFragment)
                    .commit();
        } else if (id == R.id.logout) {
            startLoginActivity();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void startLoginActivity(){
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

    @Override
    public void sendDate(int day, int month, int year) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_container, TimePickerFragment.newInstance(day, month, year))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void popFragmentStack() {
        getSupportFragmentManager().popBackStack();
    }
}
