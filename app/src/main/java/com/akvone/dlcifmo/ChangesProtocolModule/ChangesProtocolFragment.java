package com.akvone.dlcifmo.ChangesProtocolModule;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created on 10.09.2016.
 */
public class ChangesProtocolFragment extends Fragment {

    AsyncTask getChangesProtocolTask;

    View rootView;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ChangesProtocolCardsAdapter cardsAdapter;

    public static ChangesProtocolFragment newInstance(){
        ChangesProtocolFragment fragment = new ChangesProtocolFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.change_protocol,container,false);

        initSwipeLayout();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.changes_protocol_cards);

        cardsAdapter = new ChangesProtocolCardsAdapter(getContext(),new ArrayList<SubjectChanges>());
        recyclerView.setAdapter(cardsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    public void initSwipeLayout(){
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getChangesProtocolTask == null
                        || getChangesProtocolTask.getStatus()== AsyncTask.Status.FINISHED){
                    getChangesProtocolTask = new GetChangesProtocolTask();
                    getChangesProtocolTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }

    @Override
    public void onStart() {
        getChangesProtocolTask = new GetChangesProtocolTask();
        getChangesProtocolTask.execute();
        if (getChangesProtocolTask == null){

        } else if (getChangesProtocolTask.getStatus()==AsyncTask.Status.RUNNING){
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        super.onStart();
    }

    private class SubjectChanges{
        public String subject;
        public String name;
        public String minMark;
        public String maxMark;
        public String threshold;
        public String value;
        public String date;
        public String sign;

        public SubjectChanges(String subject, String name,
                              String minMark, String maxMark,
                              String threshold, String value,
                              String date, String sign) {
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

    private class GetChangesProtocolTask extends AsyncTask<Object,Void,ArrayList<SubjectChanges>>{

        final private String TAG = "ChangesProtocolTask";

        @Override
        protected ArrayList<SubjectChanges> doInBackground(Object... params) {
            Log.d(TAG, "doInBackground: start");

            ArrayList<SubjectChanges> subjectsChanges = null;
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

                JSONArray receivedArray = new JSONArray("[\n" +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Текущее тестирование\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 6,\n" +
                        "\"threshold\": 3\n" +
                        "},\n" +
                        "\"value\": 6,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "},\n" +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Текущее тестирование\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 18,\n" +
                        "\"threshold\": 12\n" +
                        "},\n" +
                        "\"value\": 12,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "},\n" +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Текущее тестирование\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 6,\n" +
                        "\"threshold\": 3\n" +
                        "},\n" +
                        "\"value\": 6.1,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "},\n" +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Текущее тестирование\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 6,\n" +
                        "\"threshold\": 3\n" +
                        "},\n" +
                        "\"value\": 6,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "},\n" +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Текущее тестирование\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 6,\n" +
                        "\"threshold\": 3\n" +
                        "},\n" +
                        "\"value\": 6,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "},\n" +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Текущее тестирование\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 6,\n" +
                        "\"threshold\": 3\n" +
                        "},\n" +
                        "\"value\": 6,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "},\n"  +
                        "{\n" +
                        "\"subject\":\"Основы программирования\",\n" +
                        "\"var\": {\n" +
                        "\"name\": \"Лабораторные работы\",\n" +
                        "\"min\": 0,\n" +
                        "\"max\": 6,\n" +
                        "\"threshold\": 3\n" +
                        "},\n" +
                        "\"value\": 6,\n" +
                        "\"date\": \"12.12.2015\",\n" +
                        "\"sign\": \"Иванов И.И.\"\n" +
                        "}\n" +
                        "]");

                subjectsChanges = new ArrayList<>();
                for (int i = 0;i<receivedArray.length();i++){
                    JSONObject jsonObject = (JSONObject) receivedArray.get(i);
                    JSONObject varObject = jsonObject.getJSONObject("var");
                    SubjectChanges subjectChanges =
                            new SubjectChanges(jsonObject.getString("subject"),
                                    varObject.getString("name"),
                                    toCorrectDoubleString(varObject.getString("min")),
                                    toCorrectDoubleString(varObject.getString("max")),
                                    toCorrectDoubleString(varObject.getString("threshold")),
                                    toCorrectDoubleString(jsonObject.getString("value")),
                                    jsonObject.getString("date"),
                                    jsonObject.getString("sign"));
                    subjectsChanges.add(subjectChanges);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Parse journal", "JSON creating failure");
                cancel(true);
            } catch (IOException e){
                e.printStackTrace();
                Log.d("Parse journal", "reader failure");
                cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Loading journal", "unknown error");
                cancel(true);
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "doInBackground: finish");
            return subjectsChanges;
        }

        public String toCorrectDoubleString(String stringDouble){
            if (stringDouble.contains(",")) {
                if (stringDouble.charAt(0) == ',') {
                    stringDouble = "0" + stringDouble;
                }
                stringDouble = stringDouble.replace(',','.');
            }
            return stringDouble;
        }

        @Override
        protected void onPostExecute(ArrayList<SubjectChanges> subjectChanges) {
            super.onPostExecute(subjectChanges);
            swipeRefreshLayout.setRefreshing(false);
            cardsAdapter.changeContent(subjectChanges);
        }
    }

    private class ChangesProtocolCardsAdapter extends RecyclerView.Adapter<ChangesProtocolCardsAdapter.CardViewHolder>{

        private ArrayList<SubjectChanges> subjectChanges;
        // Store the context for easy access
        private Context context;

        // Pass in the contact array into the constructor
        public ChangesProtocolCardsAdapter(Context context, ArrayList<SubjectChanges> subjectChanges) {
            this.subjectChanges = subjectChanges;
            this.context = context;
        }

        @Override
        public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View inflatingView;
            inflatingView = inflater.inflate(R.layout.change_protocol_item, parent, false);

            // Return a new holder instance
            CardViewHolder cardViewHolder = new CardViewHolder(inflatingView);
            return cardViewHolder;
        }

        @Override
        public void onBindViewHolder(CardViewHolder holder, int position) {
            SubjectChanges changeData = subjectChanges.get(position);
            holder.subject.setText(changeData.subject);
            holder.name.setText(changeData.name);
            holder.sign.setText(changeData.sign);
            holder.threshold.setText(getResources().getString(R.string.threshold, 6));
            holder.mark.setText(getResources()
                    .getString(R.string.mark, changeData.value, changeData.maxMark));
        }

        @Override
        public int getItemCount() {
            return subjectChanges.size();
        }

        public void changeContent(ArrayList<SubjectChanges> subjectChanges){
            this.subjectChanges = subjectChanges;
            notifyDataSetChanged();
        }

        public class CardViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView subject;
            public TextView name;
            public TextView sign;
            public TextView mark;
            public TextView threshold;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public CardViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);
                subject = (TextView) itemView.findViewById(R.id.subject_name);
                name = (TextView) itemView.findViewById(R.id.subject_title);
                sign = (TextView) itemView.findViewById(R.id.lecturer);
                mark = (TextView) itemView.findViewById(R.id.mark);
                threshold = (TextView) itemView.findViewById(R.id.threshold);
            }
        }
    }


}
