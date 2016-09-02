package com.akvone.dlcifmo.MainModule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.akvone.dlcifmo.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created on 21.08.2016.
 */
public class GetNameAndMoreTask extends AsyncTask<Void,Void,Void> {


    public static final String TAG = "Get Name and More Task";
    private MainActivity mainActivity;

    public GetNameAndMoreTask(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: begin");
        Document doc = null; //Здесь хранится будет разобранный html документ


        //Запрос делаем для получения Ф,И,О, а также номера группы
        String queryString = "https://de.ifmo.ru/servlet/distributedCDE?Rule=editPersonProfile";
        try {
            doc = Jsoup.connect(queryString).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Elements elements = doc.select("tbody #d_s_middle #d_s_m_content form[name=editForm] div table.d_table tbody tr");
            String familyName = elements.get(0).select("td").text().trim();
            String givenName = elements.get(1).select("td").text().trim();
            String middleName = elements.get(2).select("td").text().trim();
            String groupName = elements.get(11).select("td").text().trim();

            SharedPreferences sharedPref = mainActivity.
                    getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Constants.PREF_FAMILY_NAME, familyName);
            editor.putString(Constants.PREF_GIVEN_NAME, givenName);
            editor.putString(Constants.PREF_MIDDLE_NAME, middleName);
            editor.putString(Constants.PREF_GROUP_NAME, groupName);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "doInBackground: finish");
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mainActivity.updateDrawer();
    }
}
