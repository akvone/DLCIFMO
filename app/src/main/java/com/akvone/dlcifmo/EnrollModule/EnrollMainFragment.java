package com.akvone.dlcifmo.EnrollModule;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Toast;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.MainModule.MainActivity;
import com.akvone.dlcifmo.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by 1 on 20.08.2016.
 */
public class EnrollMainFragment extends Fragment{
    private static final int LAYOUT = R.layout.enroll_main;
    public static final String TAG = "Enroll fragment";

    private boolean refreshing = false;
    private View view;
    private String login;
    private String pass;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipe;
    private FloatingActionButton mFab;
    private ScrollView mScrollView;
    protected static ArrayList<RecordItem> records = new ArrayList<>();
    private static final int TYPE_DECLINED = 0;
    private static final int TYPE_WITHDRAWED = 1;
    private static final int TYPE_ACCEPTED = 2;
    private static final int TYPE_CONSIDERING = 3;
    private static int retryAttempts = 0;
    private String mLoggedUser;


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
    static boolean logged = false;
    public static EnrollMainFragment instance;
    public static EnrollMainFragment getInstance() {
        if (instance == null) {
            EnrollMainFragment fragment = new EnrollMainFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            instance = fragment;
        }
        return instance;
    }
    public static void delete(){
        instance = null;
        records = null;
        retryAttempts = 0;
        logged = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.enroll_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 15);

        ArrayAdapter<String> month = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_list_item_single_choice);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Выбеите месяц");
        dialogBuilder.setAdapter(month, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setSwipeRefreshState(true);
                new LoadRecords().execute(which);
            }
        });
        dialogBuilder.create().show();

        StringBuilder builder;
        for (int i = 0; i < 9; i++) {
            builder = new StringBuilder();
            builder.append(getResources().getStringArray(R.array.month_names)[calendar.get(Calendar.MONTH)]);
            builder.append(" ");
            builder.append(calendar.get(Calendar.YEAR));
            month.add(builder.toString());
            calendar.add(Calendar.DATE, 30);
//            switch (calendar.getMaximum(Calendar.MONTH)){
//                case Calendar.JANUARY:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.FEBRUARY:
//                    builder.append("Февраль");
//                    break;
//                case Calendar.MARCH:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.APRIL:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.MAY:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.JUNE:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.JULY:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.AUGUST:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.SEPTEMBER:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.OCTOBER:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.NOVEMBER:
//                    builder.append(getString(R.string.january));
//                    break;
//                case Calendar.DECEMBER:
//                    builder.append(getString(R.string.january));
//                    break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called, records" + records.toString());
        view = inflater.inflate(LAYOUT, container, false);

        mScrollView = (ScrollView) view.findViewById(R.id.enroll_scroll);

        recyclerView = (RecyclerView) view.findViewById(R.id.enrollRecords);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecordItemAdapter adapter = new RecordItemAdapter(records, this);
        recyclerView.setAdapter(adapter);
        if ((records != null)&&(records.size() != 0)){
//            adapter = new RecordItemAdapter(records, this);
            mScrollView.setVisibility(View.GONE);
            adapter.swap(records);

        }

        mSwipe = (SwipeRefreshLayout) view.findViewById(R.id.enroll_refresh_main);
        mSwipe.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent),
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (logged) {
                    new LoadRecords().execute();
                } else {
                    login(null, null);
                }

            }
        });
        setSwipeRefreshState(refreshing);

        mFab = (FloatingActionButton) view.findViewById(R.id.datePickerFAB);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FABonClick: just in case if you want to know, data has " + records.size() + " entries");
                EnrollDatePickerFragment dialogFragment = new EnrollDatePickerFragment();
                dialogFragment.setListener(mListener);
                dialogFragment.show(getActivity().getSupportFragmentManager(), null);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_CURRENT_USER_DATA_FILE, Context.MODE_PRIVATE);
        login = preferences.getString(Constants.PREF_LOGIN, "");
        pass = preferences.getString(Constants.PREF_PASSWORD, "");
        if (!logged) {
            new LoginTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,login, pass);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.enroll);
    }

    void updateData(){

        new LoadRecords().execute();
    }
    void updateRecords(){
        if (mScrollView != null && recyclerView != null) {
            if (records != null ) {
                if (records.size() != 0) {
                    mScrollView.setVisibility(View.GONE);
                } else {
                    mScrollView.setVisibility(View.VISIBLE);
                }
            }
//        RecordItemAdapter adapter = new RecordItemAdapter(records, this);
//        recyclerView.setAdapter(adapter);
            ((RecordItemAdapter) recyclerView.getAdapter()).swap(records);
        }
        Log.d(TAG, "updateRecords: records" + records.toString());

    }
    void withdraw(final String id, final String date,final int pos){
        String status =
                ((RecordItemAdapter.ViewHolder) recyclerView.getChildViewHolder(recyclerView.getChildAt(pos))).status.getText().toString();


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        switch (status){
            case "снята":
            case "отклонена":
                builder.setTitle("К сожалению, вы не можете её отозвать");
                builder.setPositiveButton("Okay", null);
                break;
            case "рассматривается":
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WithdrawEnroll(pos, TYPE_CONSIDERING).execute(id, date);
                    }
                });
                builder.setNegativeButton("Нет", null);
                builder.setTitle("Вы действительно хотите отзвать заявку?");
                break;
            case "удовлетворена":
                builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new WithdrawEnroll(pos, TYPE_ACCEPTED).execute(id, date);
                    }
                });
                builder.setNegativeButton("Нет", null);
                builder.setTitle("Вы действительно хотите отзвать заявку?");
                break;
            // TODO: REFACTOR
        }
        builder.create().show();
    }
    public void login(@Nullable String lg, @Nullable String  pw){
        if (lg != null && pw != null){
            new LoginTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,lg, pw);
        } else
        new LoginTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,login, pass);
    }
    public void setSwipeRefreshState(final boolean refreshing) {
        this.refreshing = refreshing;
        if (mSwipe != null) {
            mSwipe.post(new Runnable() {
                @Override
                public void run() {
                    mSwipe.setRefreshing(refreshing);
                }
            });
        }

    }



    public class LoginTask extends AsyncTask<String,Integer,Integer> {
        final int NO_INTERNET = 2;
        final int DATA_LOAD_FAIL = 4;


        @Override
        protected void onPreExecute() {
            setSwipeRefreshState(true);
            Log.d(TAG, "onPreExecute: LoginTask");
            records = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            //две по три подряд нельзя
            //нельзя более 3 академ. часов подряд
//            mFab.setVisibility(View.VISIBLE);
//            new LoadRecords().execute();
            retryAttempts = 0;
            Log.d(TAG, "onPostExecute: login: " + records.toString());
            setSwipeRefreshState(false);
            updateRecords();
            logged = true;
            try {
                Toast.makeText(getContext(), "Login finished", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(TAG, "doInBackground: Login");
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
                con.setConnectTimeout(10000);
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
                Document doc = Jsoup.parse(response.toString());
                mLoggedUser = doc.select("input[name=\"logged_user\"]").attr("value");
                //TODO: checkout other months
                if (!mLoggedUser.equals("")) {
                    Element head = doc.select("tr.table_bottomlogoline").first();
                    Element root = head.parent();
                    for (int i = 1; i < root.children().size(); i++) {
                        records.add(new RecordItem(root.child(i)));
                    }
                } else {
                    cancel(true);
                }
                Log.d(TAG, "doInBackground: LoginTask");
            } catch (UnknownHostException e){
                e.printStackTrace();
                cancel(true);
                return NO_INTERNET;
            }
            catch (IOException e) {
                e.printStackTrace();
                cancel(true);
                return DATA_LOAD_FAIL;
            }
            return null;
        }

        @Override
        protected void onCancelled(Integer result) {
            super.onCancelled();
            setSwipeRefreshState(false);
            switch (result){
                case NO_INTERNET:
                    String string = getContext().getString(R.string.error_swipe) + "login";
                    string = "Я интернета не чувствую!";
                    Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                    break;
                case DATA_LOAD_FAIL:
                    if (retryAttempts++ < 4) {
                        Log.d(TAG, "onCancelled: try to login again");
                        login(null, null);
                    } else retryAttempts = 0;
                    break;
            }
        }
    }
    private class LoadRecords extends AsyncTask<Integer,Integer,Integer> {

        final int NO_INTERNET = 2;
        final int DATA_LOAD_FAIL = 4;

        @Override
        protected Integer doInBackground(Integer... params) {
            try {

                if (params.length != 0 && mLoggedUser != null){
                    Calendar calendar = new GregorianCalendar();
                    calendar.add(Calendar.MONTH, params[0]);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int year = calendar.get(Calendar.YEAR);
                    String jsCookie = "JSESSIONID";
                    String phpCookie = "PHPSESSID";
                    String jsCookieVal = "";
                    String phpCookieVal = "";

                    
                    CookieManager manager = MainActivity.cookieManager;
                    for (HttpCookie c :
                            manager.getCookieStore().getCookies()) {
                        if (c.getName().equals(jsCookie)){
                            jsCookieVal = c.getValue();
                        }
                        if (c.getName().equals(phpCookie)){
                            phpCookieVal = c.getValue();
                        }
                    }

                    Connection.Response response = Jsoup.connect("https://de.ifmo.ru/--schedule/student.php")
                            .header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8")
                            .cookie(jsCookie, jsCookieVal)
                            .cookie(phpCookie, phpCookieVal)
                            .data("view", "%2F")
                            .data("logged_user", mLoggedUser)
                            .data("month1", "" + month)
                            .data("year1", "" + year)
                            .method(Connection.Method.POST)
                            .timeout(10000)
                            .execute();
                    Document doc = response.parse();

                    Element head = doc.select("tr.table_bottomlogoline").first();
                    if (head != null) {
                        Element root = head.parent();
                        for (int i = 1; i < root.children().size(); i++) {
                            records.add(new RecordItem(root.child(i)));
                        }
                    } else {
                        //TODO: нет элементов в ответе
                    }
                    Log.d(TAG, "doInBackground: ");
                } else {
                    Document doc = Jsoup.connect("https://de.ifmo.ru/--schedule/student.php").timeout(10000).get();
                    mLoggedUser = doc.select("input[name=\"logged_user\"]").attr("value");
                    Element head = doc.select("tr.table_bottomlogoline").first();
                    if (head != null) {
                        Element root = head.parent();
                        for (int i = 1; i < root.children().size(); i++) {
                            records.add(new RecordItem(root.child(i)));
                        }
                    } else {
                        //TODO: нет элементов в ответе
                    }
                }
                Log.d(TAG, "doInBackground: ");

            } catch (UnknownHostException e){
                e.printStackTrace();
                cancel(true);
                return NO_INTERNET;
            }

            catch (Exception e) {
                e.printStackTrace();
                cancel(true);
                return DATA_LOAD_FAIL;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: LoadRecords");
            super.onPreExecute();
            records = new ArrayList<>();
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);
            setSwipeRefreshState(false);
            retryAttempts = 0;
            updateRecords();
        }

        @Override
        protected void onCancelled(Integer result) {
            super.onCancelled();
            setSwipeRefreshState(false);
            switch (result){
                case NO_INTERNET:
                    String string = getContext().getString(R.string.error_swipe) + "login";
                    string = "Я интернета не чувствую!";
                    Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                    break;
                case DATA_LOAD_FAIL:
                    if (retryAttempts++ < 4) {
                        Log.d(TAG, "onCancelled: try to load records again");
                        new LoadRecords().execute();
                    } else retryAttempts = 0;
                    break;
            }
//            Toast.makeText(getContext(), R.string.error_swipe, Toast.LENGTH_SHORT).show();
        }
    }
    protected class WithdrawEnroll extends AsyncTask<String,Integer,Integer> {
        int type;
        int pos;
        static final int NO_INTERNET = 2;
        static final int DATA_LOAD_FAIL = 4;

        WithdrawEnroll(int pos, int type) {
            this.pos = pos;
            this.type = type;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String id = params[0];
            String date = params[1];
            int month = Integer.parseInt(date.substring(3, 5));
            String year = date.substring(6);
            String function;
            if (type == TYPE_ACCEPTED){
                function = "sdr";
            } else function = "dr";

//            >?????????????
            String surl = "https://de.ifmo.ru/--schedule/student.php";
            //func=dr&reid=680667&month1=9&year1=2016
//            func=dr&reid=680671&month1=9&year1=2016
//            https://de.ifmo.ru/--schedule/student.php?func=dr&reid=680673&month1=9&year1=2016
//            https://de.ifmo.ru/--schedule/student.php?func=dr&reid=680671&month1=9&year1=2016
//            https://de.ifmo.ru/--schedule/student.php?func=dr&reid=680673&month1=9&year1=2016
            try {
                String xml = "func=" +
                        function +
                        "&reid=" +
                        id +
                        "&month1=" +
                        month +
                        "&year1=" +
                        year;
                String url = surl + "?" + xml;
                Document doc = Jsoup.connect(url).get();
                Log.d(TAG, "doInBackground: ");
            }catch (UnknownHostException e){
                e.printStackTrace();
                cancel(true);
                return NO_INTERNET;
            }

            catch (Exception e) {
                e.printStackTrace();
                cancel(true);
                return DATA_LOAD_FAIL;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);
//            recyclerView.removeViewAt(pos);
//            recyclerView.refreshDrawableState();
            setSwipeRefreshState(false);
            try {
                records.remove(pos);
                updateData();
                Toast.makeText(getContext(), "Отозвана", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to remove recyclerView item", e);
                Toast.makeText(getContext(), "Ошибка при отзыве", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected void onCancelled(Integer result) {
            super.onCancelled();
            setSwipeRefreshState(false);
            switch (result){
                case NO_INTERNET:
                    String string = getContext().getString(R.string.error_swipe) + "login";
                    string = "Я интернета не чувствую!";
                    Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                    break;
                case DATA_LOAD_FAIL:
                    string = "Error happened during withdraw";
                    Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
//            Toast.makeText(getContext(), R.string.error_swipe, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
//                View view = recyclerView.getChildAt(pos);
//
//                ImageView img = (ImageView) view.findViewById(R.id.withdrawEnroll);
//                ProgressBar progress = (ProgressBar) view.findViewById(R.id.withdrawProgress);
//                progress.setEnabled(true);
//                progress.setVisibility(View.VISIBLE);
//                img.setVisibility(View.INVISIBLE);
                setSwipeRefreshState(true);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Ошибка при отзыве", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onPreExecute: Withdraw ini failure", e);
            }
        }


    }
}
