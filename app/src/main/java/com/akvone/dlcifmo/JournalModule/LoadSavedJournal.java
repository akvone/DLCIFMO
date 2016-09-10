package com.akvone.dlcifmo.JournalModule;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by 1 on 08.08.2016.
 */
public class LoadSavedJournal extends AsyncTask<Void, Integer, JSONObject> {

    public static final String TAG = "Load saved journal Task";
    Context context;
    public LoadSavedJournal(Context c) {
        super();
        context = c;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        Log.d(TAG, "doInBackground: ");
        JSONObject object = null;
        try {
            FileInputStream in = context.openFileInput("journal.json");
            InputStreamReader reader = new InputStreamReader(in);
            char[] buffer = new char[256];
            int rc;
            StringBuilder sb = new StringBuilder();
            try {
                while ((rc = reader.read(buffer)) != -1)
                    sb.append(buffer, 0, rc);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            object = new JSONObject(sb.toString());
        } catch (JSONException e) {
//            e.printStackTrace();
            Log.d("Load saved journal", "JSON creating failure");
            cancel(true);
        } catch (IOException e){
//            e.printStackTrace();
            Log.d("Load saved journal", "reader failure");
            cancel(true);
        }
        Log.d(TAG, "doInBackground: finished");
        return object;
    }

    @Override
    protected void onPostExecute(JSONObject object) {
        JournalFragment.journal = object;
        Log.d(TAG, "end");
//        Subject.parseSavedJournal(JournalFragment.journal);
        if (object == null) return;
        if (Journal.getInstance() == null){
            Journal.newInstance(object);
        } else {
            //Is this even possible?
            try {
                Journal.getInstance().update(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JournalFragment.getInstance().setupListRecyclerView();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "begin");
    }
}
