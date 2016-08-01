package com.akvone.dlcifmo;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    DrawerLayout drawer;
    NavigationView navigationView;
    BlankFragment blankFragment1;
    BlankFragment blankFragment2;
    BlankFragment blankFragment3;
    BlankFragment blankFragment4;
    BlankFragment blankFragment5;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initDrawer();
        initNavigationView();

        loadFragments();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.main_activity_container, blankFragment1)
                .commit();
    }

    private void initDrawer(){
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
        blankFragment1 = BlankFragment.newInstance("Здесь будет журнал с баллами по предметам");
        blankFragment2 = BlankFragment.newInstance("Здесь будет протокол изменений");
        blankFragment3 = BlankFragment.newInstance("Здесь будет рейтинг топовых студентов");
        blankFragment4 = BlankFragment.newInstance("Здесь будет запись на тестирование");
        blankFragment5 = BlankFragment.newInstance("Здесь будет обратная связь");
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        getSupportActionBar().setTitle(item.getTitle());
        if (id == R.id.gradebook) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, blankFragment1)
                    .commit();
        } else if (id == R.id.change_protocol) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, blankFragment2)
                    .commit();
        } else if (id == R.id.top_student) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, blankFragment3)
                    .commit();
        } else if (id == R.id.testing_registration) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, blankFragment4)
                    .commit();
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
        } else if (id == R.id.feedback) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, blankFragment5)
                    .commit();
        } else if (id == R.id.logout) {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}
