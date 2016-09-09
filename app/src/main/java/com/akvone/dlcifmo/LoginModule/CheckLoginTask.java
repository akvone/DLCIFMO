package com.akvone.dlcifmo.LoginModule;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.MainModule.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created on 31.07.2016.
 */
/**
 * Represents an asynchronous activity_login/registration task used to authenticate
 * the user.
 */
public class CheckLoginTask extends AsyncTask<Object,Integer,Integer> {
    public static final int LOGIN_SUCCESS = 1;
    public static final int PASSWORD_IS_INCORRECT = 2;

    public static final String ITMO_LOGIN_URL = "https://de.ifmo.ru/servlet/";

    private final String mLogin;
    private final String mPassword;

    Activity callerActivity;
    //Из MainActivity надо логиниться, поэтому public
    public CheckLoginTask(String login, String password) {
        mLogin = login;
        mPassword = password;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        MainActivity.offline = true;

    }

    @Override
    protected void onPreExecute() {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        if (!activeNetworkInfo.isConnected()){
//            Toast.makeText(callerActivity.getApplicationContext(),"ХУЙ",Toast.LENGTH_LONG);
//        }
//
//        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Object... params) {

        callerActivity = (Activity) params[0];
        MainActivity.cookieManager = new CookieManager();
        java.net.CookieHandler.setDefault(MainActivity.cookieManager);
//        String activity_login = (String) params[1];
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
            String htmlString = builder.toString();
            Document doc = null;//Здесь хранится будет разобранный html документ
            doc = Jsoup.parse(htmlString);
            Elements elements = doc.select("title");
            Element titleElement = elements.get(0);
            if (titleElement.text().contains("Message")) { //TODO исправить на Access if forbidden
                return PASSWORD_IS_INCORRECT;
            }
            else {
                SharedPreferences sharedPref = callerActivity
                        .getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE,
                                Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Constants.PREF_IS_FULL_MODE, true);
                editor.putString(Constants.PREF_LOGIN, mLogin);
                editor.putString(Constants.PREF_PASSWORD, mPassword);
                editor.commit();
                return LOGIN_SUCCESS;
            }

        } catch (Exception e){
            e.printStackTrace();
            cancel(true);
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
        MainActivity.offline = false;
        if (callerActivity instanceof LoginActivity) {
            ((LoginActivity)callerActivity).userLoginTaskAction(integer);
        }
        else {
//            Вызов из MainActivity
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}