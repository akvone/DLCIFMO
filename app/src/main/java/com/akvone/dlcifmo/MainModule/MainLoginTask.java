package com.akvone.dlcifmo.MainModule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

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
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

import static com.akvone.dlcifmo.Constants.*;

/**
 * Created on 22.08.2016.
 */
public class MainLoginTask extends AsyncTask<Integer,Void,ArrayList<Integer>> {
    private static final int LOGIN_SUCCESS = 0;
    private static final int LOGIN_DATA_IS_INCORRECT = -1;

    public static final int UPDATE_NAME_AND_MORE = 1;
    public static final int UPDATE_RATING_AND_MORE = 2;

    public static final String ITMO_LOGIN_URL = "https://de.ifmo.ru/servlet/";

    private MainActivity mainActivity;
    private String login;
    private String password;

    public MainLoginTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        MainActivity.offline = true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        SharedPreferences preferences = mainActivity.
                getSharedPreferences(PREF_CURRENT_USER_DATA_FILE,Context.MODE_PRIVATE);
        login = preferences.getString(PREF_LOGIN, null);
        password = preferences.getString(PREF_PASSWORD, null);
    }

    @Override
    protected ArrayList<Integer> doInBackground(Integer... params) {
        ArrayList<Integer> toDoList = new ArrayList<>(Arrays.asList(params));

        MainActivity.cookieManager = new CookieManager();
        java.net.CookieHandler.setDefault(MainActivity.cookieManager);

//        if (login==null || password==null){
//            return NO_DATA_FOUND_REQUIRED_FOR_LOGIN;
//        }

        String args;
        args = "Rule=LOGON&LOGIN=" + login +
                "&PASSWD=" + password;

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
            String inputLine;
            StringBuilder builder = new StringBuilder();
            while ((inputLine = inputStream.readLine()) != null){
                builder.append(inputLine);
            }
            String htmlString = builder.toString();
            Document doc = null;//Здесь хранится будет разобранный html документ
            doc = Jsoup.parse(htmlString);
            Elements elements = doc.select("title");
            Element titleElement = elements.get(0);
            if (titleElement.text().contains("Message")) { //TODO исправить на Access if forbidden
                toDoList.add(LOGIN_DATA_IS_INCORRECT);
                return toDoList;
            }
            else {
                toDoList.add(LOGIN_SUCCESS);
                return toDoList;
            }

        } catch (Exception e){
            e.printStackTrace();
            cancel(true);
        }
        toDoList.add(LOGIN_DATA_IS_INCORRECT);
        return toDoList;
    }

    @Override
    protected void onPostExecute(ArrayList<Integer> toDoList) {
        super.onPostExecute(toDoList);
        if (toDoList.contains(LOGIN_DATA_IS_INCORRECT)){
            SharedPreferences preferences = mainActivity.
                    getSharedPreferences(PREF_CURRENT_USER_DATA_FILE,Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear().commit();
        }
        else if (toDoList.contains(LOGIN_SUCCESS)){
            if (toDoList.contains(UPDATE_NAME_AND_MORE)) {
                new GetNameAndMoreTask(mainActivity).execute();
            }
            if (toDoList.contains(UPDATE_RATING_AND_MORE)) {
                new GetRatingAndMoreTask(mainActivity).execute();
            }
        }
    }
}