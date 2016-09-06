package com.akvone.dlcifmo.EnrollModule;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.JournalModule.MySwipeRefreshLayout;
import com.akvone.dlcifmo.MainModule.FragmentWithLoader;
import com.akvone.dlcifmo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.Inflater;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by 1 on 20.08.2016.
 */
public class EnrollMainFragment extends Fragment{
    private static final int LAYOUT = R.layout.enroll_main;
    private String TAG = "Enroll fragment";

    private View view;
    private String login;
    private String pass;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipe;
    private FloatingActionButton mFab;
    private ScrollView mScrollView;
    protected static List<RecordItem> records = new ArrayList<>();


    private OnFragmentInteractionListener mListener;

    public EnrollMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EnrollDatePickerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EnrollMainFragment newInstance() {
        EnrollMainFragment fragment = new EnrollMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        view = inflater.inflate(LAYOUT, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.enrollRecords);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(null);
        mFab = (FloatingActionButton) view.findViewById(R.id.datePickerFAB);
        mFab.setVisibility(View.INVISIBLE);
        mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.enroll_refresh_main);
//        mSwipe.setScrollingView(recyclerView);
        mSwipe.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent),
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mFab.getVisibility() == View.INVISIBLE){
                    new LoginTask().execute(login, pass);
                } else {
                    new LoadRecords().execute();
                }
//                updateRecords();
            }
        });
//        mSwipe.bringToFront();
//        mSwipe.setRefreshing(true);
        mScrollView = (ScrollView) view.findViewById(R.id.enroll_scroll);
//        mScrollView.setVisibility(View.GONE);



        new LoginTask().execute(login, pass);
//        new LoadRecords().execute();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnrollDatePickerFragment dialogFragment = new EnrollDatePickerFragment();
                dialogFragment.setListener(mListener);
                dialogFragment.show(getActivity().getSupportFragmentManager(), null);
            }
        });
        //<tr class="table_bottomlogoline"> neighbours
        return view;
    }

    void updateRecords(){
        mSwipe.setRefreshing(false);
        mScrollView.setVisibility(View.GONE);
        RecordItemAdapter adapter = new RecordItemAdapter(records, this);
        recyclerView.setAdapter(adapter);

    }
    void withdraw(final String id, final String date,final int pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new WithdrawEnroll(pos).execute(id, date);
            }
        });
        builder.setNegativeButton("Нет", null);
        builder.setTitle("Вы действительно хотите отзвать заявку?");
        builder.create().show();
    }

    public void setSwipeRefreshState(final boolean refreshing) {

        if (mSwipe != null) {
            mSwipe.post(new Runnable() {
                @Override
                public void run() {
                    mSwipe.setRefreshing(refreshing);
                }
            });
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
        login = preferences.getString(Constants.PREF_LOGIN, "");
         pass = preferences.getString(Constants.PREF_PASSWORD, "");

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class LoginTask extends AsyncTask<String,Integer,Void> {
        @Override
        protected void onPreExecute() {
            setSwipeRefreshState(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //две по три подряд нельзя
            //нельзя более 3 академ. часов подряд
            mFab.setVisibility(View.VISIBLE);
            new LoadRecords().execute();
        }

        @Override
        protected Void doInBackground(String... params) {
            StringBuilder date;
            Calendar today = Calendar.getInstance();
            int day = today.get(Calendar.DAY_OF_MONTH);
            int month = today.get(Calendar.MONTH);
            int year = today.get(Calendar.YEAR);

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
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setSwipeRefreshState(false);
            String string = getContext().getString(R.string.error_swipe) + "login";
            Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
        }
    }
    private class LoadRecords extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {

                Document doc = Jsoup.connect("https://de.ifmo.ru/--schedule/student.php").timeout(10000).get();
                String logged_user = doc.select("input[name=\"logged_user\"]").attr("value");
                Element head = doc.select("tr.table_bottomlogoline").first();
                Element root = head.parent();
                for (int i = 1; i < root.children().size(); i++) {
                    records.add(new RecordItem(root.child(i)));
                }
                Log.d(TAG, "doInBackground: ");

            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            records = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setSwipeRefreshState(false);
            updateRecords();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setSwipeRefreshState(false);
            Toast.makeText(getContext(), R.string.error_swipe, Toast.LENGTH_SHORT).show();
        }
    }
    protected class WithdrawEnroll extends AsyncTask<String, Integer, Void>{

        int pos;

        public WithdrawEnroll(int pos) {
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];
            String date = params[1];
            int month = Integer.parseInt(date.substring(3, 5));
            String year = date.substring(6);

//            >?????????????
            String surl = "https://de.ifmo.ru/--schedule/student.php";
            //func=dr&reid=680667&month1=9&year1=2016
//            func=dr&reid=680671&month1=9&year1=2016
//            https://de.ifmo.ru/--schedule/student.php?func=dr&reid=680673&month1=9&year1=2016
//            https://de.ifmo.ru/--schedule/student.php?func=dr&reid=680671&month1=9&year1=2016
//            https://de.ifmo.ru/--schedule/student.php?func=dr&reid=680673&month1=9&year1=2016
            try {
//                HttpsURLConnection con = (HttpsURLConnection) new URL(surl).openConnection();
//
//                con.setDoOutput(true);
//
//                ByteArrayOutputStream byteStream = new
//                        ByteArrayOutputStream(400);
//                PrintWriter out = new PrintWriter(byteStream, true);
//                String xml = "func=dr&reid=" +
//                        id +
//                        "&month1=" +
//                        month +
//                        "&year1=" +
//                        year;
//                out.write(xml);
//                out.flush();
//
//                con.setRequestMethod("GET");
//                con.setRequestProperty("Content-Length", String.valueOf(byteStream.size()));
//                con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");//установка свойств запроса
//                con.setRequestProperty("Content-Type",
//                        "application/x-www-form-urlencoded");
//
//                byteStream.writeTo(con.getOutputStream());
//                int responseCode = con.getResponseCode(); //получение кода ответа(200)
//
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(con.getInputStream(), "Windows-1251"));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//
//                //Try to interrupt earlier
//
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                Log.d(TAG, "doInBackground: ");
                String xml = "func=dr&reid=" +
                        id +
                        "&month1=" +
                        month +
                        "&year1=" +
                        year;
                String url = surl + "?" + xml;
                Document doc = Jsoup.connect(url).get();
                Log.d(TAG, "doInBackground: ");
            } catch (IOException e) {
                e.printStackTrace();
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.removeViewAt(pos);
            records.remove(pos);
            Toast.makeText(getContext(), "Отозвана", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View view = recyclerView.getChildAt(pos);

            ImageView img = (ImageView) view.findViewById(R.id.withdrawEnroll);
            ProgressBar progress = (ProgressBar) view.findViewById(R.id.withdrawProgress);
            progress.setEnabled(true);
            progress.setVisibility(View.VISIBLE);
            img.setVisibility(View.INVISIBLE);
        }


    }
}
