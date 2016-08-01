package com.akvone.dlcifmo;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created on 31.07.2016.
 */
/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Object,Integer,Integer> {
    public static final int LOGIN_SUCCESS = 1;
    public static final int PASSWORD_IS_INCORRECT = 2;

    public static final String ITMO_LOGIN_URL = "https://de.ifmo.ru/servlet/";

    private final String mLogin;
    private final String mPassword;

    Activity callerActivity;
    CookieManager cookieManager;

    UserLoginTask(String email, String password) {
        mLogin = email;
        mPassword = password;
    }


    @Override
    protected Integer doInBackground(Object... params) {
        callerActivity = (Activity) params[0];
        cookieManager = new CookieManager();
        java.net.CookieHandler.setDefault(cookieManager);
//        String login = (String) params[1];
//        String password = (String) params[2];
        String args;
        args = "Rule=LOGON&LOGIN=" +
                mLogin +
                "&PASSWD=" +
                mPassword;

        try {
            URL url = new URL(ITMO_LOGIN_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setDoOutput(true);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(400);
            PrintWriter out=new PrintWriter(byteStream,true);
            out.write(args);
            out.flush();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", String.valueOf(byteStream.size()));
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");//установка свойств запроса
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            byteStream.writeTo(connection.getOutputStream());

            int jresponsAeCode = connection.getResponseCode(); //получение кода ответа(200)
//                Log.d("DLC", responseCode + "");
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            BufferedReader inputStream = new BufferedReader(reader);
            String inputLinne;
            StringBuilder builder = new StringBuilder();
            while ((inputLinne = inputStream.readLine()) != null){
                builder.append(inputLinne);
            }
            SpannableStringBuilder sp = (SpannableStringBuilder) Html.fromHtml(builder.toString());
            String a = builder.toString();
            Log.d("Res", a);

        } catch (Exception e){
            e.printStackTrace();
        }

//        for (HttpCookie c :
//                cookieManager.getCookieStore().getCookies()) {
//            if (c.getName().equals("JSESSIONID")){
//                return LOGIN_SUCCESS;
//            }
//        }
        return PASSWORD_IS_INCORRECT;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (callerActivity instanceof LoginActivity) {
            ((LoginActivity)callerActivity).userLoginTaskAction(integer);
        }
        else {
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}