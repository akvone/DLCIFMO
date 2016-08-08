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

/**
 * Created by 1 on 08.08.2016.
 */
public class LoadSavedJournal extends AsyncTask<Void, Integer, JSONObject> {

    Context context;
    public LoadSavedJournal(Context c) {
        super();
        context = c;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
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
            Log.d("Parse journal", "JSON creating failure");
            cancel(true);
        } catch (IOException e){
//            e.printStackTrace();
            Log.d("Journal parse", "reader failure");
            cancel(true);
        }
        return object;
    }

    @Override
    protected void onPostExecute(JSONObject object) {
        Log.d("Load saved journal", "end");
        Subject.parseJSONJournal(object);
        JournalFragment.getInstance().setupListRecyclerView();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("Load saved journal", "begin");
    }
}
