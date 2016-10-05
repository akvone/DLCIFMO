package com.akvone.dlcifmo.ChangesProtocolModule;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akvone.dlcifmo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created on 10.09.2016.
 */
public class ChangesProtocolFragment extends Fragment {

    public static ChangesProtocolFragment newInstance(){
        ChangesProtocolFragment fragment = new ChangesProtocolFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        GetChangesProtocolTask getChangesProtocolTask
                = new GetChangesProtocolTask();
        getChangesProtocolTask.execute(1);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.changes_protocol);
    }

    private class GetChangesProtocolTask extends AsyncTask<Integer,Void,Integer>{

        final private String TAG = "Changes Protocol Task";

        @Override
        protected Integer doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground: ");

            JSONObject object = null;
            try {
                URL url = new URL("https://de.ifmo.ru/api/private/eregisterlog?days=90");

                URLConnection connection = url.openConnection();
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) connection;
                int resp = httpsURLConnection.getResponseCode();
                Log.d("Https Response code", resp + "");
                if (resp == 204){
                    this.cancel(true);
                }

                InputStream inputStream = httpsURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String inputLine;
                StringBuilder stringBuilder = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                bufferedReader.close();

                object = null;
                object = new JSONObject(stringBuilder.toString());
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
            return 1;
        }
    }
}
