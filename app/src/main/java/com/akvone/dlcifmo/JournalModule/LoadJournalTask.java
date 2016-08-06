package com.akvone.dlcifmo.JournalModule;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by 1 on 05.08.2016.
 */
public class LoadJournalTask extends AsyncTask<Void, Integer, JSONObject> {
    JournalFragment journalFragment;

    public LoadJournalTask(JournalFragment fragment) {
        super();
        journalFragment = fragment;
    }

    @Override
    protected void onPostExecute(JSONObject eregister) {
        try {
            JSONArray years = eregister.getJSONArray("years");
            Subject.CHOSEN_SEMESTER = years.length()*2 - (Subject.isAutumnSemester ? 1 : 0);
            Subject.CURRENT_SEMESTER = Subject.CHOSEN_SEMESTER;
            Subject.years = years.length();
            Log.d("Subject", "CHOSEN_SEMESTER is " + Subject.CHOSEN_SEMESTER);
            for (int j = 0; j < years.length(); j++)
            {

                JSONObject currentYear = years.getJSONObject(j);
                JSONArray subjects = currentYear.getJSONArray("subjects");
                for (int i = 0; i<subjects.length(); i++)
                {
                    Subject.subjects.add(new Subject(subjects.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        journalFragment.setRefreshActionButtonState(false);
        journalFragment.setLoadingJournal(false);

        journalFragment.updateCards();
    }

    @Override
    protected void onPreExecute() {
        journalFragment.setRefreshActionButtonState(true);
        journalFragment.setLoadingJournal(true);
    }

    @Override
    protected void onCancelled() {
        journalFragment.setRefreshActionButtonState(false);
        journalFragment.setLoadingJournal(false);
        Toast.makeText(journalFragment.getContext(), "Ошибка при загрузке. Попробуйте позже", Toast.LENGTH_SHORT).show();
        super.onCancelled();
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        JSONObject object = null;
        try {
            URL url = new URL("https://de.ifmo.ru/api/private/eregister");
            InputStream is = null;
            int resp = -1;

            try {
                URLConnection connection = url.openConnection();
                HttpsURLConnection cnctn = (HttpsURLConnection)  connection;
                resp = cnctn.getResponseCode();
                if (resp == 204){
                    this.cancel(true);
                }
                is = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;
            StringBuilder sb = new StringBuilder();
            Log.d("Https Response code", resp + "");
            try {
                while ((rc = reader.read(buffer)) != -1)
                    sb.append(buffer, 0, rc);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            object = null;
            try {
                object = new JSONObject(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return object;

    }
}
