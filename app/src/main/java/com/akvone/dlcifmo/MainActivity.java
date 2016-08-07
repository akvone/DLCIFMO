package com.akvone.dlcifmo;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.akvone.dlcifmo.JournalModule.JournalFragment;
import com.akvone.dlcifmo.JournalModule.Subject;
import com.akvone.dlcifmo.LoginModule.LoginActivity;
import com.akvone.dlcifmo.EnrollModule.EnrollDatePickerFragment;
import com.akvone.dlcifmo.EnrollModule.EnrollTimePickerFragment;
import com.akvone.dlcifmo.LoginModule.UserLoginTask;
import com.akvone.dlcifmo.TopStudentsModule.TopStFragment;

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

    private boolean hasLoginData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);
        hasLoginData = sharedPref.getBoolean(Constants.PREF_HAS_LOGIN_DATA, false);
        //Проверяем, пропускал ли пользователь авторизацию
        if (sharedPref.getBoolean(Constants.PREF_SKIP_LOGIN_BOOLEAN,false)){
            //Если пользователь логинился в предыдущий запуск приложения
            if (sharedPref.getBoolean(Constants.PREF_HAS_LOGIN_DATA, false)){
                String login = sharedPref.getString(Constants.PREF_LOGIN, null);
                String password = sharedPref.getString(Constants.PREF_PASSWORD, null);
                new UserLoginTask(login, password).execute(this);
            }
        }
        else {
            startLoginActivity();
        }

        setContentView(R.layout.activity_main);
        initToolbar();
        initDrawer();
        initNavigationView();

        loadFragments();
        getSupportFragmentManager()
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
        if (hasLoginData) {
            journalFragment = JournalFragment.getInstance();
            changesProtocolFragment = BlankFragment.newInstance("Здесь будет протокол изменений");
            enrollFragment = EnrollDatePickerFragment.newInstance();
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
        } else if (id == R.id.feedback) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, feedbackFragment)
                    .commit();
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
        getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        //Сохранённый журнал
        getSharedPreferences(Constants.PREF_MOCK_FILE, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        //Журнал придётся пересоздавать под нового пользователя
        Subject.subjects.clear();
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
