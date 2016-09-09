package com.akvone.dlcifmo.EnrollModule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.MainModule.FragmentWithLoader;
import com.akvone.dlcifmo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EnrollTimePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EnrollTimePickerFragment extends FragmentWithLoader {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DAY = "day";
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private static final int LAYOUT = R.layout.enroll_time_picker;
    public static final String TAG = "Enroll Time Picker Frag";

    // TODO: Rename and change types of parameters
    private int day;
    private int month;
    private int year;
    private View view;
    private RecyclerView recyclerView;
    private Context context;
    protected static CookieManager enrollCookieManager;
    private StringBuilder date; //Дата записи, приведённая к понятному ЦДО виду (01.09.2016)
    private BookItemAdaptor adapter;
    private LinkedHashMap<Integer, Integer> streaks;

    public EnrollTimePickerFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static EnrollTimePickerFragment newInstance(int day, int month, int year) {
        EnrollTimePickerFragment fragment = new EnrollTimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DAY, day);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_YEAR, year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            day = getArguments().getInt(ARG_DAY);
            month = getArguments().getInt(ARG_MONTH);
            year = getArguments().getInt(ARG_YEAR);

            date = new StringBuilder();
            if (day < 10) {
                date.append("0").append(day);
            } else date.append(day);
            date.append(".");
            if (month < 9) {
                date.append("0").append((month+1));
            } else {
                date.append(month+1);
            }
            date.append(".");
            date.append(year);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        hidableView = view.findViewById(R.id.hidable_layout);
        progressBar = (ProgressBar) view.findViewById(R.id.registration_facility_progress);
        Button b = (Button) view.findViewById(R.id.registrationButton);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int start = -1;
                int streak = 0;
                int i = 0;
                streaks = new LinkedHashMap<>();
                for (BookItemAdaptor.Item item:
                        adapter.getData()){
                    if (item.checked){
                        if (start == -1){
                            start = i;
                        } else {
                            streak ++;
                        }
                    } else {
                        if (start != -1){
                            streaks.put(start, streak);
                            start = -1;
                            streak = 0;
                        }
                    }
                    i++;
                }
                if (start != -1){
                    streaks.put(start, streak);
                }
                if (!streaks.isEmpty()){
                    for (int k :
                            streaks.keySet()) {
                        start = k;
                        streak = streaks.get(k) + 1;
                        new Enroll().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,adapter.getTimes()[start], adapter.getTimes()[start+streak]);
                    }
                } else {
                    Toast.makeText(context, "Chose something!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        new LoadFreePlaces().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, day, month, year);
        return view;
    }



    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    private class LoadFreePlaces extends AsyncTask<Integer,Integer,List<String>> {
        @Override
        protected void onPostExecute(List<String> places) {
            if (places == null){
                Toast.makeText(context, "Записи на этот день нет", Toast.LENGTH_LONG).show();
                OnFragmentInteractionListener activity = (OnFragmentInteractionListener) context;
                activity.popFragmentStack();
            } else {

                recyclerView = (RecyclerView) view.findViewById(R.id.registrationRecyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));

                recyclerView.setAdapter(adapter = new BookItemAdaptor(places, date.toString()));
                try {
                    showProgress(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                showProgress(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected List<String> doInBackground(Integer... params) {
            Log.d(TAG, "doInBackground: LoadFreePlaces");
            int day = params[0];
            int month = params[1];
            int year = params[2];
            List<String> places = new ArrayList<>();
            Calendar calendar = new GregorianCalendar(year, month, day);
            int week = calendar.get(Calendar.WEEK_OF_YEAR);
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
            //dayOfWeek задаётся константами
            // SUNDAY = 1;
            // MONDAY = 2;
            // ...
            // SATURDAY = 7;
            //Elements days же начинается с понедельника, для которого 0
            calendar.set(year, 8, 1);
            int week1stSeptember = calendar.get(Calendar.WEEK_OF_YEAR);
            //1st of september is 35 week;
            //первая учебная неделя, http://de.ifmo.ru/index.php?node=23
            int node = week - week1stSeptember + 23;

            try {
                Document doc = Jsoup.connect("http://de.ifmo.ru/index.php?node="+node).timeout(10000).get();
                Elements mainTable = doc.select("td.ptext table tbody tr");
                //Первые 3 td не нужны, 4-ый - понедельник
                Elements days = mainTable.select("td[rowspan]");
                Element theDay = days.get(dayOfWeek);
                if (!theDay.attr("rowspan").equals("1")) {

                    Element nextDay = days.get(dayOfWeek + 1);
                    Element x = mainTable.first();
                    int i = 1;
                    while (x != theDay) {
                        x = mainTable.get(i++).child(0);
                    }
                    places.add(x.parent().child(2).text());
                    x = mainTable.get(i++).child(0);
                    do {
                        places.add(x.parent().child(1).text());
                        x = mainTable.get(i++).child(0);
                    }while (x != nextDay);

                    Log.d("Doc", mainTable.toString());
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }


            return places;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(context, "Error happened", Toast.LENGTH_SHORT).show();
            ((OnFragmentInteractionListener) context).popFragmentStack();
        }
    }
    private class Enroll extends AsyncTask<String, Integer, Integer>{
        String mBegin;
        String mEnd;
        final int TO_RESTART = 2;
        final int DATA_LOAD_FAIL = 4;
        final int NO_INTERNET = 8;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                showProgress(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            try {
                showProgress(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(context, "Запсиь успешна. Возможно...", Toast.LENGTH_SHORT).show();
            ((OnFragmentInteractionListener) context).popFragmentStack();
            EnrollMainFragment.getInstance().updateData();
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(TAG, "doInBackground: enroll");
            String begin = params[0];
            String  end = params[1];
            mBegin = begin;
            mEnd = end;
            if (!(EnrollMainFragment.logged)){
                cancel(true);
                return TO_RESTART;
            }
            String  surl = "https://de.ifmo.ru/--schedule/student.php";
            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(surl).openConnection();
                con.setDoOutput(true);

                ByteArrayOutputStream byteStream = new
                        ByteArrayOutputStream(400);
                PrintWriter out = new PrintWriter(byteStream, true);
                int month = Integer.parseInt(date.substring(3, 5));
                String xml = "view=sr&func=sr&month1=" +
                        month +
                        "&year1=" +
                        date.substring(6) +
                        "&data2=" +
                        date +
                        "&begin1=" +
                        URLEncoder.encode(begin, "Windows-1251") +
                        "&end1=" +
                        URLEncoder.encode(end, "Windows-1251");
                out.write(xml);
                out.flush();
//                view=sr&func=sr&month1=9&year1=2016&data2=02.09.2016&begin1=11%3A20&end1=12%3A40
//                view=sr&func=sr&month1=0&year1=2016&data2=02.09.2016&begin1=11%3A20&end1=12%3A40
//                view=sr&func=sr&month1=0&year1=2016&data2=02.09.2016&begin1=11%3A20&end1=12%3A00
//                view=sr&func=sr&month1=9&year1=2016&data2=02.09.2016&begin1=10%3A00&end1=10%3A40
                //func=dr&reid=680667&month1=9&year1=2016

                //add reuqest header
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                byteStream.writeTo(con.getOutputStream());
                int responseCode = con.getResponseCode(); //получение кода ответа(200)

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "Windows-1251"));
                String inputLine;
                StringBuilder response = new StringBuilder();


                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                Document doc = Jsoup.parse(response.toString());
                Log.d("Doc", doc.select("td").toString());
            } catch (UnknownHostException e){
                e.printStackTrace();
                cancel(true);
                return NO_INTERNET;
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                return DATA_LOAD_FAIL;
            }
            return null;
        }

        @Override
        protected void onCancelled(Integer result) {
            super.onCancelled();
            if (result == TO_RESTART) {
                Log.d(TAG, "onCancelled: enroll will fail cuz no login");
                EnrollMainFragment.getInstance().login();
                new Enroll().execute(mBegin, mEnd);
            }
//            if (result == DATA_LOAD_FAIL){
//                Toast.makeText(getContext(), "Fail", Toast.LENGTH_SHORT).show();
//                ((OnFragmentInteractionListener) getContext()).popFragmentStack();
//
//            }
            switch (result){
                case NO_INTERNET:
                    String string = getContext().getString(R.string.error_swipe) + "login";
                    string = "Я интернета не чувствую!";
                    Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                    try {
                        showProgress(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DATA_LOAD_FAIL:
                    new Enroll().execute(mBegin, mEnd);
                    break;
            }
        }
    }
}

