package com.akvone.dlcifmo.JournalModule;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by 1 on 05.08.2016.
 */
public class LoadJournalTask extends AsyncTask<Void, Integer, JSONObject> {
    String TAG = "Load Journal Task";
    JournalFragment journalFragment;
    @Override
    protected void onPostExecute(JSONObject eregister) {
        Log.d(TAG, "onPostExecute: ");
        if (eregister != null){
            try {
                if (Journal.getInstance() == null){
                    Journal.newInstance(eregister);
                } else Journal.getInstance().update(eregister);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        Journal.newInstance(eregister);
        journalFragment.setSwipeRefreshState(false);
        journalFragment.setLoadingJournal(false);

        journalFragment.setupListRecyclerView();
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute: ");
        journalFragment = JournalFragment.getInstance();
        journalFragment.setSwipeRefreshState(true);
        journalFragment.setLoadingJournal(true);
    }

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: ");
        journalFragment.setSwipeRefreshState(false);
        journalFragment.setLoadingJournal(false);
        Toast.makeText(journalFragment.getContext(), "Ошибка при загрузке. Попробуйте позже", Toast.LENGTH_SHORT).show();
        super.onCancelled();
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: ");
        JSONObject object = null;
        try {
            URL url = new URL("https://de.ifmo.ru/api/private/eregister");
            InputStream is = null;
            int resp = -1;
            URLConnection connection = url.openConnection();
            HttpsURLConnection cnctn = (HttpsURLConnection)  connection;
            resp = cnctn.getResponseCode();
            if (resp == 204){
                this.cancel(true);
            }
            is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;
            StringBuilder sb = new StringBuilder();
            Log.d("Https Response code", resp + "");
            while ((rc = reader.read(buffer)) != -1)
                sb.append(buffer, 0, rc);
            reader.close();
            object = null;
            object = new JSONObject(sb.toString());
        } catch (JSONException e) {
//            e.printStackTrace();
            Log.d("Parse journal", "JSON creating failure");
            cancel(true);
        } catch (IOException e){
//            e.printStackTrace();
            Log.d("Parse journal", "reader failure");
            cancel(true);
        } catch (Exception e) {
            Log.d("Loading journal", "unknown error");
            cancel(true);
        }
        return object;

    }
}
