package com.akvone.dlcifmo.MainModule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.AboutActivity;
import com.akvone.dlcifmo.ChangesProtocolModule.ChangesProtocolFragment;
import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.EnrollModule.EnrollMainFragment;
import com.akvone.dlcifmo.EnrollModule.EnrollTimePickerFragment;
import com.akvone.dlcifmo.EnrollModule.OnFragmentInteractionListener;
import com.akvone.dlcifmo.JournalModule.Journal;
import com.akvone.dlcifmo.JournalModule.JournalFragment;
import com.akvone.dlcifmo.JournalModule.LoadSavedJournal;
import com.akvone.dlcifmo.LoginModule.LoginActivity;
import com.akvone.dlcifmo.R;
import com.akvone.dlcifmo.SettingsModule.PreferencesActivity;
import com.akvone.dlcifmo.SettingsModule.SettingsActivity;
import com.akvone.dlcifmo.TopStudentsModule.TopStFragment;
import com.bakatrouble.ifmo_timetable.TimetableActivity;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileNotFoundException;
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

    String rootTitle;
    final String TAG = "Main activity";

    private boolean isFullMode;
    private boolean skipLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
        isFullMode = sharedPref.getBoolean(Constants.PREF_IS_FULL_MODE, false);
        skipLogin = sharedPref.getBoolean(Constants.PREF_SKIP_LOGIN, false);
        //Проверяем, нужно ли нам пропустить LoginActivity
        if (skipLogin) { //Да, пропускаем и выполняем основную деятельность
            setContentView(R.layout.main);
            initToolbar();
            initDrawer();
            initNavigationView();
            loadFragments();

            //Проверяем, в каком режиме работает приложение
            if (isFullMode) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (preferences.getBoolean("show_timetable_first", false)){
                    startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
                }
                new MainLoginTask(this).
                        execute(MainLoginTask.UPDATE_NAME_AND_MORE,
                                MainLoginTask.UPDATE_RATING_AND_MORE,
                                MainLoginTask.UPDATE_JOURNAL,
                                MainLoginTask.LOGIN_PHP);
//                changeFragment(journalFragment);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_activity_container, journalFragment)
                        .commit();
                rootTitle = getString(R.string.journalTitle);
            } else {

                startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
//                changeFragment(topStFragment);
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.main_activity_container, journalFragment)
//                        .commit();

                rootTitle = getString(R.string.top_student);
            }
        } else {
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
        if (isFullMode) {
            updateDrawer();
        } else {
        }
        toggle.syncState();

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initNavigationView() {
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.full_rating),
                        Toast.LENGTH_SHORT)
                        .show();
                RatingDialog ratingDialog = new RatingDialog(MainActivity.this);
                ratingDialog.initDialog();
            }
        });
    }


    public void loadFragments() {
//        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//
//                Fragment f = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getFragments().size() - 1);
//                Log.d(TAG, "onBackStackChanged: setting title for last of " +
//                        getSupportFragmentManager().getFragments().size() + " fragments");
//                //Очевидно, getFragments совсем не отражает суть стека, что наталкивает на мыли о суициде
//                if (f instanceof JournalFragment) {
//                    getSupportActionBar().setTitle(getString(R.string.journal));
//                }
//                if (f instanceof ChangesProtocolFragment) {
//                    getSupportActionBar().setTitle(getString(R.string.changes_protocol));
//                }
//                if (f instanceof EnrollMainFragment) {
//                    getSupportActionBar().setTitle(getString(R.string.enroll));
//                }
//                if (f instanceof TopStFragment) {
//                    getSupportActionBar().setTitle(getString(R.string.top_student));
//                }
//
//            }
//        });
        if (isFullMode) {
            journalFragment = JournalFragment.getInstance();
            changesProtocolFragment = ChangesProtocolFragment.newInstance();
            enrollFragment = EnrollMainFragment.getInstance();
        } else {
            journalFragment = NonAuthorizedFragment.newInstance();
            changesProtocolFragment = NonAuthorizedFragment.newInstance();
            enrollFragment = NonAuthorizedFragment.newInstance();
        }

        topStFragment = new TopStFragment();
        feedbackFragment = BlankFragment.newInstance("Здесь будет обратная связь");
    }

    public void updateDrawer() {
        View header = navigationView.getHeaderView(0);
        if (isFullMode) {
            TextView positionInRatingView = (TextView) header.findViewById(R.id.positionInRating);
            TextView userNameView = (TextView) header.findViewById(R.id.userName);
            TextView groupNameView = (TextView) header.findViewById(R.id.groupName);
            SharedPreferences preferences = getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
            String positionInRating = preferences.getString(Constants.PREF_POSITION_RATING_INFORMATION, "[-]");
            String userName = preferences.getString(Constants.PREF_FAMILY_NAME, "[-]")
                    + " " + preferences.getString(Constants.PREF_GIVEN_NAME, "[-]");
            String groupName = preferences.getString(Constants.PREF_GROUP_NAME, "[-]");
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
        if (id == R.id.gradebook) {
            changeFragment(journalFragment);
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.main_activity_container, journalFragment)
//                    .commit();
//            getSupportActionBar().setTitle(item.getTitle());
        } else if (id == R.id.changes_protocol) {
            changeFragment(changesProtocolFragment);
//            getSupportActionBar().setTitle(item.getTitle());
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.main_activity_container, changesProtocolFragment)z
//                    .commit();
        } else if (id == R.id.top_student) {
            changeFragment(topStFragment);
//            getSupportActionBar().setTitle(item.getTitle());
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.main_activity_container, topStFragment)
//                    .commit();
        } else if (id == R.id.testing_registration) {
            changeFragment(enrollFragment);
//            getSupportActionBar().setTitle(item.getTitle());
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.main_activity_container, enrollFragment)
//                    .commit();
        } else if (id == R.id.settings) {
            startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
//            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        } else if (id == R.id.about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        } else if (id == R.id.timetable) {
            startActivity(new Intent(getApplicationContext(), TimetableActivity.class));
        } else if (id == R.id.logout) {
            startLoginActivity();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void startLoginActivity() {
        //На всякий почистим данные пользователя
        getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
        Journal.delete();
        JournalFragment.delete();
        EnrollMainFragment.delete();
        try {
            FileOutputStream v = this.openFileOutput("journal.json", MODE_PRIVATE);
            v.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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
        FragmentManager manager = getSupportFragmentManager();
        Log.d("Fragments", "changeFragment: before " + manager.getBackStackEntryCount());
        if (fragment == journalFragment) {
            Log.d(TAG, "changeFragment: clear before setting root");
            while (manager.getBackStackEntryCount() > 0) {
                manager.popBackStackImmediate();
            }
            return;
        }
        FragmentTransaction transaction = manager.beginTransaction();
        switch (manager.getBackStackEntryCount()) {
            case 1:
                //Заменяем фрагмент над рутом
                Log.d(TAG, "changeFragment: 1 fragment in stack. popping it out");
                manager.popBackStackImmediate();
            case 0:
                //Кладём на рут fragment
                Log.d(TAG, "changeFragment: replacing top fragment with new one");
                transaction.replace(R.id.main_activity_container, fragment)
                        .addToBackStack(null);
                break;
            default:
                // сбросить фрагменты до 1 и заменить
                Log.d(TAG, "changeFragment: more than 1 fragment in stack - clearing");
                while (manager.getBackStackEntryCount() > 0) {
                    manager.popBackStackImmediate();
                }
                transaction.replace(R.id.main_activity_container, fragment)
                        .addToBackStack(null);
                break;
        }
        transaction.commit();
    }

//    /**
//     * ATTENTION: This was auto-generated to implement the App Indexing API.
//     * See https://g.co/AppIndexing/AndroidStudio for more information.
//     * тот момент, когда студия пишет код за тебя, и ты не знаешь, можно ли его вообще удалять.
//     */
//    public Action getIndexApiAction() {
//        Thing object = new Thing.Builder()
//                .setName("Main Page") // TODO: Define a title for the content shown.
//                // TODO: Make sure this auto-generated URL is correct.
//                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
//                .build();
//        return new Action.Builder(Action.TYPE_VIEW)
//                .setObject(object)
//                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
//                .build();
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        AppIndex.AppIndexApi.start(client, getIndexApiAction());
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        client.disconnect();
//    }
}
