package com.akvone.dlcifmo.ChangesProtocolModule;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
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
import java.text.NumberFormat;
import java.util.ArrayList;

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
        getChangesProtocolTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class SubjectChanges{
        public String subject;
        public String name;
        public double minMark;
        public double maxMark;
        public double threshold;
        public double value;
        public String date;
        public String sign;

        public SubjectChanges(String subject, String name, double minMark, double maxMark, double threshold, double value, String date, String sign) {
            this.subject = subject;
            this.name = name;
            this.minMark = minMark;
            this.maxMark = maxMark;
            this.threshold = threshold;
            this.value = value;
            this.date = date;
            this.sign = sign;
        }
    }
    private class GetChangesProtocolTask extends AsyncTask<Integer,Void,Integer>{

        final private String TAG = "Changes Protocol Task";

        @Override
        protected Integer doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground: ");

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

                JSONArray receivedArray = new JSONArray(stringBuilder.toString());
                ArrayList<SubjectChanges> subjectsChanges = new ArrayList<>();
                for (int i = 0;i<receivedArray.length();i++){
                    JSONObject jsonObject = (JSONObject) receivedArray.get(i);
                    JSONObject varObject = jsonObject.getJSONObject("var");
                    SubjectChanges subjectChanges =
                            new SubjectChanges(jsonObject.getString("subject"),
                                    varObject.getString("name"),
                                    parseJSONDouble(varObject.getString("min")),
                                    parseJSONDouble(varObject.getString("max")),
                                    parseJSONDouble(varObject.getString("threshold")),
                                    parseJSONDouble(jsonObject.getString("value")),
                                    jsonObject.getString("date"),
                                    jsonObject.getString("sign"));
                    subjectsChanges.add(subjectChanges);
                }
                int j = 0;

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

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
        }

        public double parseJSONDouble(String stringDouble){
            if (stringDouble.contains(",")) {
                if (stringDouble.charAt(0) == ',') {
                    stringDouble = "0" + stringDouble;
                }
                stringDouble = stringDouble.replace(',','.');
            }
            double a = Double.parseDouble(stringDouble);

            return a;
        }
    }
}
