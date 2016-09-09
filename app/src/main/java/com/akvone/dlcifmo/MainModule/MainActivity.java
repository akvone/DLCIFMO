package com.akvone.dlcifmo.MainModule;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.akvone.dlcifmo.AboutActivity;
import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.EnrollModule.EnrollMainFragment;
import com.akvone.dlcifmo.JournalModule.Journal;
import com.akvone.dlcifmo.JournalModule.JournalFragment;
import com.akvone.dlcifmo.JournalModule.LoadSavedJournal;
import com.akvone.dlcifmo.LoginModule.LoginActivity;
import com.akvone.dlcifmo.EnrollModule.EnrollDatePickerFragment;
import com.akvone.dlcifmo.EnrollModule.EnrollTimePickerFragment;
import com.akvone.dlcifmo.EnrollModule.OnFragmentInteractionListener;
import com.akvone.dlcifmo.R;
import com.akvone.dlcifmo.SettingsModule.SettingsActivity;
import com.akvone.dlcifmo.TopStudentsModule.TopStFragment;

import java.io.FileOutputStream;
import java.net.CookieManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    public static CookieManager cookieManager = new CookieManager();
    public static boolean offline = false;

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;

    Fragment journalFragment;
    Fragment changesProtocolFragment;
    TopStFragment topStFragment;
    Fragment enrollFragment;
    Fragment feedbackFragment;

    private boolean isFullMode;
    private boolean skipLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE,Context.MODE_PRIVATE);
        isFullMode = sharedPref.getBoolean(Constants.PREF_IS_FULL_MODE, false);
        skipLogin = sharedPref.getBoolean(Constants.PREF_SKIP_LOGIN, false);
        //Проверяем, нужно ли нам пропустить LoginActivity
        if (skipLogin){ //Да, пропускаем и выполняем основную деятельность
            setContentView(R.layout.activity_main);
            initToolbar();
            initDrawer();
            initNavigationView();
            loadFragments();

            //Проверяем, в каком режиме работает приложение
            if (isFullMode){
                new MainLoginTask(this).
                        execute(MainLoginTask.UPDATE_NAME_AND_MORE,
                                MainLoginTask.UPDATE_RATING_AND_MORE,
                                MainLoginTask.UPDATE_JOURNAL);
                changeFragment(journalFragment);
            }
            else {
                changeFragment(topStFragment);
            }
        }
        else {
            startLoginActivity();
            finish();
        }
    }

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (isFullMode){
            updateDrawer();
        }
        else {
        }
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
        if (isFullMode) {
            journalFragment = JournalFragment.getInstance();
            changesProtocolFragment = BlankFragment.newInstance("Здесь будет протокол изменений");
            enrollFragment = EnrollMainFragment.getInstance();
        }
        else{
            journalFragment = NonAuthorizedFragment.newInstance();
            changesProtocolFragment = NonAuthorizedFragment.newInstance();
            enrollFragment = NonAuthorizedFragment.newInstance();
        }
        topStFragment = new TopStFragment();
        feedbackFragment = BlankFragment.newInstance("Здесь будет обратная связь");
    }

    public void updateDrawer(){
        View header = navigationView.getHeaderView(0);
        if (isFullMode){
            TextView positionInRatingView = (TextView) header.findViewById(R.id.positionInRating);
            TextView userNameView = (TextView) header.findViewById(R.id.userName);
            TextView groupNameView = (TextView) header.findViewById(R.id.groupName);
            SharedPreferences preferences = getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
            String positionInRating = preferences.getString(Constants.PREF_POSITION_RATING_INFORMATION,"error");
            String userName = preferences.getString(Constants.PREF_FAMILY_NAME,"error")
                    + " " + preferences.getString(Constants.PREF_GIVEN_NAME,"error");
            String groupName = "Группа " + preferences.getString(Constants.PREF_GROUP_NAME,"error");
            positionInRatingView.setText(positionInRating);
            userNameView.setText(userName);
            groupNameView.setText(groupName);
        }
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
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, journalFragment)
                    .commit();
        } else if (id == R.id.change_protocol) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, changesProtocolFragment)
                    .commit();
        } else if (id == R.id.top_student) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, topStFragment)
                    .commit();
        } else if (id == R.id.testing_registration) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, enrollFragment)
                    .commit();
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        } else if (id == R.id.about) {
            startActivity(new Intent(getApplicationContext(),AboutActivity.class));
        } else if (id == R.id.logout) {
            //Если пользователь решил выйти, очистить его данные
            startLoginActivity();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void startLoginActivity(){
        //На всякий почистим данные пользователя
        getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Journal.delete();
        try {
            FileOutputStream v = this.openFileOutput("journal.json", MODE_PRIVATE);
            v.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
    }

    //Реализация интерфеса общения с фрагментами
    @Override
    public void sendDate(int day, int month, int year) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_container, EnrollTimePickerFragment.newInstance(day, month, year))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void popFragmentStack() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void changeFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
