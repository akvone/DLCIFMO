package com.akvone.dlcifmo.TestingRegistrationModule;

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

import com.akvone.dlcifmo.FragmentWithLoader;
import com.akvone.dlcifmo.OnFragmentInteractionListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimePickerFragment extends FragmentWithLoader {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DAY = "day";
    private static final String ARG_MONTH = "month";
    private static final String ARG_YEAR = "year";
    private static final int LAYOUT = R.layout.testing_registration_time_picker;

    // TODO: Rename and change types of parameters
    private int day;
    private int month;
    private int year;
    private View view;
    private RecyclerView recyclerView;
    private Context context;
    private CookieManager registrationCookieManager;
    private StringBuilder date; //Дата записи, приведённая к понятному ЦДО виду (01.09.2016)
    private BookItemAdaptor adapter;

    public TimePickerFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TimePickerFragment newInstance(int day, int month, int year) {
        TimePickerFragment fragment = new TimePickerFragment();
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
            if (month < 9) {
                date.append("0").append((month+1));
            } else {
                date.append(month+1);
            }
            date.append(year);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        hidableView = view.findViewById(R.id.hidableLayout);
        progressBar = (ProgressBar) view.findViewById(R.id.registration_facility_progress);
        Button b = (Button) view.findViewById(R.id.registrationButton);



//        SharedPreferences preferences = context.getSharedPreferences()
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoginTask().execute("191762", "dxv885");
            }
        });
        new LoadFreePlaces().execute(day, month, year);
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
                recyclerView.setAdapter(adapter = new BookItemAdaptor(places));
                showProgress(false);
            }
        }

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected List<String> doInBackground(Integer... params) {
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
                Document doc = Jsoup.connect("http://de.ifmo.ru/index.php?node="+node).get();
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
            }


            return places;
        }
    }
    private class LoginTask extends AsyncTask<String,Integer,Void> {
        @Override
        protected void onPreExecute() {
            registrationCookieManager = new CookieManager();
            registrationCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(registrationCookieManager);
            showProgress(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            int start = 0;
            int streak = 1;
            int i = 0;
            boolean empty = true;
            for (BookItemAdaptor.Item item :
                    adapter.getData()) {
                if ((empty)&&(item.checked)){
                    start = i;
                    empty = false;
                } else if ((!empty)&&(item.checked)){
                    streak++;
                    if (streak == 3) {
                        break;
                    }
                }
                i++;
            }
            if (!empty){
                new RegisterTest().execute(adapter.getTimes()[start], adapter.getTimes()[start+streak]);
            }
        }

        @Override
        protected Void doInBackground(String... params) {

            String login = params[0];
            String password = params[1];
            String surl = "https://de.ifmo.ru/--schedule/index.php";
            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(surl).openConnection();

                con.setDoOutput(true);

                ByteArrayOutputStream byteStream = new
                        ByteArrayOutputStream(400);
                PrintWriter out = new PrintWriter(byteStream, true);
                String xml = "data=" +
                        date.toString() +
                        "+12%3A00%3A00&login=" +
                        login +
                        "&passwd=" +
                        password +
                        "&role=%D1%F2%F3%E4%E5%ED%F2";
                out.write(xml);
                out.flush();

                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Length", String.valueOf(byteStream.size()));
                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");//установка свойств запроса
                con.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                byteStream.writeTo(con.getOutputStream());
                int responseCode = con.getResponseCode(); //получение кода ответа(200)

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "Windows-1251"));
                String inputLine;
                StringBuilder response = new StringBuilder();

                //Try to interrupt earlier
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    private class RegisterTest extends AsyncTask<String, Integer, Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            showProgress(false);
            Toast.makeText(context, "Запсиь успешна. Возможно...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String begin = params[0];
            String  end = params[1];

            String  surl = "https://de.ifmo.ru/--schedule/student.php";
            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(surl).openConnection();
                con.setDoOutput(true);

                ByteArrayOutputStream byteStream = new
                        ByteArrayOutputStream(400);
                PrintWriter out = new PrintWriter(byteStream, true);
                int month = Integer.parseInt(date.substring(3, 4));
                String  xml = "view=sr&func=sr&month1=" +
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

