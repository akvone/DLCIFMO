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
    JournalFragment journalFragment;

    public LoadJournalTask(JournalFragment fragment) {
        super();
        journalFragment = fragment;
    }

    @Override
    protected void onPostExecute(JSONObject eregister) {
        if (eregister != null){
            journalFragment.saveJournal(eregister);
        }
        Subject.parseJSONJournal(eregister);
        journalFragment.setSwipeRefreshState(false);
        journalFragment.setLoadingJournal(false);

        journalFragment.setupListRecyclerView();
    }

    @Override
    protected void onPreExecute() {
        journalFragment.setSwipeRefreshState(true);
        journalFragment.setLoadingJournal(true);
    }

    @Override
    protected void onCancelled() {
        journalFragment.setSwipeRefreshState(false);
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
            Log.d("Journal parse", "reader failure");
            cancel(true);
        }
        return object;

    }
}
