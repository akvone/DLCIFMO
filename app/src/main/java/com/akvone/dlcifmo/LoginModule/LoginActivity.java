package com.akvone.dlcifmo.LoginModule;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.MainModule.MainActivity;
import com.akvone.dlcifmo.R;

/**
 * A activity_login screen that offers activity_login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    /**
     * Keep track of the activity_login task to ensure we can cancel it if requested.
     */
    private CheckLoginTask mAuthTask = null;

    // UI references.
    private View progressView;
    private View scrollView;
    private View movingLayout;
    private LinearLayout hidableLayout;

    private EditText loginTextView;
    private EditText passwordTextView;
    private Button logInButton;

    private boolean itmoLogoIsHidden = false;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Если мы оказались здесь, то пока
        //не введем правильные логин-пароль/не пропустим шаг с авторизацией
        //нас будет всегда бросать в это активити
        SharedPreferences sharedPref = getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constants.PREF_SKIP_LOGIN, false);
        editor.commit();
        setContentView(R.layout.activity_login);
        //Настраиваем наш layout
        scrollView = findViewById(R.id.scroll_view);
        progressView = findViewById(R.id.login_progress_view);

        initActionBar();
        initLogo();
        initTextViews();
        initSwitchToMainButtons();
    }

    private void initActionBar(){
        ActionBar actionBar =  getSupportActionBar();
        if (actionBar!=null) {
            //TODO: убрать хардкод
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1e51a4")));
        }
    }

    private void initLogo(){
        TextView aboutUniversity = (TextView) findViewById(R.id.aboutUniversity);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/calibribold.ttf");
        aboutUniversity.setTypeface(font);
    }

    private void initTextViews(){
        movingLayout =  findViewById(R.id.moving_layout);
        hidableLayout = (LinearLayout) findViewById(R.id.hidable_layout);

        loginTextView = (EditText) findViewById (R.id.login);
        passwordTextView = (EditText) findViewById(R.id.password);
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (itmoLogoIsHidden) {
                    return;
                }
                if (hasFocus) {
                    performLayoutAfterTextViewClicked(true);
                }
            }
        };
        loginTextView.setOnFocusChangeListener(onFocusChangeListener);
        passwordTextView.setOnFocusChangeListener(onFocusChangeListener);

        passwordTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    private void initSwitchToMainButtons(){
        logInButton = (Button) findViewById(R.id.log_in_button);
        logInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!itmoLogoIsHidden){
                    loginTextView.requestFocus();
                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                else{
                    attemptLogin();
                }
            }
        });

        TextView skipAuthorization = (TextView) findViewById(R.id.skipAuthorization);
        skipAuthorization.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG); //Подчеркиваем текст
        skipAuthorization.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });
    }

    private void performLayoutAfterTextViewClicked(boolean hide){
        itmoLogoIsHidden = hide;
        if (hide) {
            hidableLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the activity_login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual activity_login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        // Reset errors.
        loginTextView.setError(null);
        passwordTextView.setError(null);
        // Store values at the time of the activity_login attempt.
        String login = loginTextView.getText().toString();
        String password = passwordTextView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordTextView.setError(getString(R.string.error_field_required));
            focusView = passwordTextView;
            cancel = true;
        }
        // Check for a valid activity_login address.
        if (TextUtils.isEmpty(login)) {
            loginTextView.setError(getString(R.string.error_field_required));
            focusView = loginTextView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt activity_login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user activity_login attempt.
            showProgress(true);
            mAuthTask = new CheckLoginTask(login, password);
            mAuthTask.execute(this);
        }
    }

    /**
     * Shows the progress UI and hides the activity_login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            scrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    public void userLoginTaskAction(int answer){
        mAuthTask = null;
        showProgress(false);
        switch (answer){
            case CheckLoginTask.LOGIN_SUCCESS:
                startMainActivity();
                break;
            case CheckLoginTask.PASSWORD_IS_INCORRECT:
                passwordTextView.setError(getString(R.string.error_incorrect_data));
                passwordTextView.requestFocus();
                break;
        }
    }

    public void startMainActivity(){
        SharedPreferences sharedPref = getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constants.PREF_SKIP_LOGIN, true);
        editor.commit();
        startActivity(new Intent(new Intent(getApplicationContext(),MainActivity.class)));
        finish();
    }
}

