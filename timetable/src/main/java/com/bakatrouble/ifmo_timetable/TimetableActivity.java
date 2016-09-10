package com.bakatrouble.ifmo_timetable;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.views.llm.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class TimetableActivity extends AppCompatActivity {
    public static final String PREFS_FILE = "preferences";

    SectionsPagerAdapter mSectionsPagerAdapter;
    boolean mOddWeek = false;
    ViewPager mViewPager;
    TabLayout mTabLayout;
    Toolbar mToolbar;
    LinearLayout mSearchBar;
    boolean mSearchBarVisible = false;
    ImageView mDim;
    Menu mMenu;
    EditText mSearchInput;
    InputMethodManager mIMM;
    ImageView mSearchClear, mSearchBack;
    RecyclerView mSearchSuggestions;
    FrameLayout mRootView;
    View mLineDivider;
    SearchSuggestionAdapter mSuggestionAdapter;
    SearchInputListener mSearchInputListener;

    public static int week = 2;
    public static long gid = -1;
    public static int state = 0;
    // 0 = no default
    // 1 = loading
    // 2 = list

    public static TimetableDBHelper DBHelper;
    SharedPreferences mPreferences;
    RequestQueue mRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Looper.getMainLooper().getThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ifmo_timetable_log.txt");
                    file.delete();
                    try {
                        PrintStream ps = new PrintStream(file);
                        ex.printStackTrace(ps);
                        ps.close();

                    } catch (IOException e) {
                    }
                }
                ex.printStackTrace();
                Toast.makeText(TimetableActivity.this, "Приложение было завершено с ошибкой\nЛог ошибки находится в корне карты памяти в файле ifmo_timetable_log.txt", Toast.LENGTH_LONG).show();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

        this.setContentView(R.layout.activity_timetable);
        findViews();
        setListeners();
        createObjects();

        mSearchBar.setVisibility(View.INVISIBLE);
        this.setSupportActionBar(mToolbar);

        mPreferences = getSharedPreferences(PREFS_FILE, 0);
        if(savedInstanceState == null){
            readSettings();
        }else{
            week = savedInstanceState.getInt("week", week);
            gid = savedInstanceState.getLong("gid", gid);
            setAppState(gid != -1 ? AppState.LIST : AppState.DEFAULT_NOT_SET, TimetableActivity.DBHelper.getTitleByInternalId(gid));
            state = savedInstanceState.getInt("state", state);
            mOddWeek = savedInstanceState.getBoolean("mOddWeek", mOddWeek);
            mSearchBarVisible = savedInstanceState.getBoolean("mSearchBarVisible", false);
            if(mSearchBarVisible){
                mSearchBar.setVisibility(View.VISIBLE);
                mDim.setVisibility(View.VISIBLE);
                String query = savedInstanceState.getString("searchBarText");
                mSearchInput.setText(query);
            }
        }

        mIMM = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setTabsFromPagerAdapter(mSectionsPagerAdapter);

        mSearchInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        mSearchClear.setAlpha(0f);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mSearchSuggestions.setLayoutManager(layoutManager);
        mSearchSuggestions.setAdapter(mSuggestionAdapter);

        mViewPager.setCurrentItem(getInitialTab());

        checkUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setCurrentItem(getInitialTab());
        if(mPreferences.contains("week")){
            Calendar c = Calendar.getInstance();
            if((mPreferences.getInt("week", -1) - c.get(Calendar.WEEK_OF_YEAR)) % 2 == 0){
                mOddWeek = mPreferences.getBoolean("odd_week", false);
            }else{
                mOddWeek = !mPreferences.getBoolean("odd_week", false);
            }
            week = mOddWeek ? 1 : 2;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRequestQueue.cancelAll("__suggestion_loader");
        mRequestQueue.cancelAll("__timetable_loader");
        outState.putInt("week", week);
        outState.putLong("gid", gid);
        outState.putInt("state", state);
        outState.putBoolean("mOddWeek", mOddWeek);
        outState.putBoolean("mSearchBarVisible", mSearchBarVisible);
        if(mSearchBarVisible){
            outState.putString("searchBarText", mSearchInput.getText().toString());
        }
    }

    private void readSettings(){

        gid = mPreferences.getLong("default_id", -1);
        if(gid != -1){
            setAppState(AppState.LIST, TimetableActivity.DBHelper.getTitleByInternalId(gid));
        }else{
            setAppState(AppState.DEFAULT_NOT_SET, "");
        }
        if(mPreferences.contains("week")){
            Calendar c = Calendar.getInstance();
            if((mPreferences.getInt("week", -1) - c.get(Calendar.WEEK_OF_YEAR)) % 2 == 0){
                mOddWeek = mPreferences.getBoolean("odd_week", false);
            }else{
                mOddWeek = !mPreferences.getBoolean("odd_week", false);
            }
            week = mOddWeek ? 1 : 2;
        }
        try {
            if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode > mPreferences.getInt("version", 0)){
                mPreferences.edit().putInt("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).apply();

                Bundle args = new Bundle();
                args.putString("title", getResources().getString(R.string.changelog_title));
                args.putCharSequence("html", Html.fromHtml(getResources().getString(R.string.changelog)));

                FragmentHtmlDialog dialog = new FragmentHtmlDialog();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(), "changelog");
            }
        }catch(PackageManager.NameNotFoundException e){}
    }

    private void findViews(){
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mDim = (ImageView)findViewById(R.id.dim);
        mLineDivider = findViewById(R.id.line_divider);
        mSearchSuggestions = (RecyclerView)findViewById(R.id.search_suggestions);
        mSearchBack = (ImageView)findViewById(R.id.search_back);
        mSearchClear = (ImageView)findViewById(R.id.search_clear);
        mSearchInput = (EditText)findViewById(R.id.search_input);
        mSearchBar = (LinearLayout)findViewById(R.id.search_bar);
        mRootView = (FrameLayout)findViewById(R.id.root_layout);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
    }

    private void setListeners(){
        ItemClickSupport.addTo(mSearchSuggestions).setOnItemClickListener(new SuggestionClickListener());
        ItemClickSupport.addTo(mSearchSuggestions).setOnItemLongClickListener(new SuggestionLongClickListener());
        mSearchBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchBarVisible) {
                    onOptionsItemSelected(mMenu.findItem(R.id.action_search));
                }
            }
        });
        mSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInput.setText("");
            }
        });
        mSearchInputListener = new SearchInputListener();
        mSearchInput.addTextChangedListener(mSearchInputListener);
        mDim.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mSearchBarVisible) {
                    onOptionsItemSelected(mMenu.findItem(R.id.action_search));
                }
                return true;
            }
        });
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTabLayout.setScrollPosition(position, positionOffset, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
            }
        });
    }

    private void createObjects(){
        mRequestQueue = Volley.newRequestQueue(this);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSuggestionAdapter = new SearchSuggestionAdapter();
        TimetableActivity.DBHelper = new TimetableDBHelper(this, 6);
    }

    public void loadSchedule(final String pid, final SearchSuggestion.Type type){
        String url = "http://orir.ifmo.ru/mobile/API2.0/index.php";
        mRequestQueue.cancelAll("__timetable_loader");
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("a", response);
                if(response.isEmpty()){
                    setAppState(AppState.REVERT, null);
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.schedule_loading_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                try{
                    JSONArray res = new JSONArray(response);
                    JSONObject obj = res.getJSONObject(0);
                    JSONArray schedule = obj.getJSONArray("schedule");
                    boolean isGroup = obj.has("gr");

                    Calendar c = Calendar.getInstance();
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putInt("week", c.get(Calendar.WEEK_OF_YEAR));
                    editor.putBoolean("odd_week", obj.getInt("week_number") % 2 == 0);
                    editor.apply();
                    week = obj.getInt("week_number") % 2 == 0 ? 1 : 2;

                    long gid = DBHelper.getIdByPid(obj.getString(isGroup ? "gr" : "pid"));
                    if(gid == -1) {
                        gid = TimetableActivity.DBHelper.addGroup(
                                obj.getString(isGroup ? "gr" : "pid"),
                                isGroup ? SearchSuggestion.Type.Group : SearchSuggestion.Type.Teacher,
                                obj.getString(isGroup ? "gr" : "person")
                        );
                    }else{
                        DBHelper.removeFromCache(gid, false);
                    }

                    TimetableActivity.gid = gid;
                    for(int i=0; i<schedule.length(); i++){
                        JSONObject subj = schedule.getJSONObject(i);
                        String status = "";
                        if(!subj.getString("status").isEmpty()){
                            status += " (";
                            switch (subj.getString("status")){
                                case "лабораторная":
                                    status += "лаб)";
                                    break;
                                case "практика":
                                    status += "прак)";
                                    break;
                                case "лекция":
                                    status += "лек)";
                                    break;
                                default:
                                    status += subj.getString("status") + ")";
                                    break;
                            }
                        }
                        TimetableActivity.DBHelper.addSubject(new Subject(
                                subj.getString("title") + status,
                                isGroup ? subj.getString("person") : subj.getString("gr"),
                                subj.getString("time1"),
                                subj.getString("time2"),
                                subj.getString("room") + " (" + subj.getString("place") + ")",
                                gid,
                                subj.getInt("day_week"),
                                subj.getInt("week_type"),
                                isGroup ? subj.getString("pid") : subj.getString("gr"),
                                isGroup ? SearchSuggestion.Type.Group : SearchSuggestion.Type.Teacher
                        ));
                    }
                    setAppState(AppState.LIST, TimetableActivity.DBHelper.getTitleByInternalId(gid));
                    mSectionsPagerAdapter.notifyDataSetChanged();
                }catch(JSONException e){
                    Log.e("a", e.toString());
                    setAppState(AppState.REVERT, null);
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.schedule_loading_error), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("a", error.toString());
                setAppState(AppState.REVERT, null);
                Toast.makeText(getBaseContext(), getResources().getString(R.string.schedule_loading_error), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("login", "ifmo01");
                params.put("pass", "01ifmo04");
                params.put("module", "schedule_lessons");
                params.put(type == SearchSuggestion.Type.Teacher ? "pid" : "gr", pid);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

            @Override
            public Object getTag(){
                return "__timetable_loader";
            }
        };
        mRequestQueue.add(request);
    }

    private void updateSchedule(long internal_id){
        String pid = DBHelper.getPidByid(internal_id);
        SearchSuggestion.Type type = DBHelper.getTypeByid(internal_id);
        if(type == null)
            return;
        setAppState(AppState.LOADING, "");
        loadSchedule(pid, type);
    }

    private ArrayList<SearchSuggestion> fillSuggestionsFromCache(String s){
        ArrayList<SearchSuggestion> cacheSugg = TimetableActivity.DBHelper.searchInCache(s);
        if(cacheSugg.size() > 0){
            mSuggestionAdapter.updateDataset(cacheSugg, false);
            mLineDivider.setVisibility(View.VISIBLE);
            mSearchSuggestions.setVisibility(View.VISIBLE);
        }else{
            mSuggestionAdapter.clearDataset();
        }
        return cacheSugg;
    }

    enum AppState{
        DEFAULT_NOT_SET,
        LOADING,
        LIST,
        REVERT
    }

    long[] stateBackup;
    String oldTitle;

    private void setAppState(AppState state, String title){
        switch(state){
            case DEFAULT_NOT_SET:
                stateBackup = new long[]{TimetableActivity.state, TimetableActivity.gid};
                if(getSupportActionBar() != null && getSupportActionBar().getTitle() != null)
                    oldTitle = getSupportActionBar().getTitle().toString();
                else
                    oldTitle = "";
                TimetableActivity.state = 0;
                if(mMenu != null)
                    mMenu.findItem(R.id.action_set_default).setVisible(false);
                break;
            case LOADING:
                stateBackup = new long[]{TimetableActivity.state, TimetableActivity.gid};
                if(getSupportActionBar() != null && getSupportActionBar().getTitle() != null)
                    oldTitle = getSupportActionBar().getTitle().toString();
                else
                    oldTitle = "";
                TimetableActivity.state = 1;
                if(mMenu != null)
                    mMenu.findItem(R.id.action_set_default).setVisible(false);
                break;
            case LIST:
                stateBackup = new long[]{TimetableActivity.state, TimetableActivity.gid};
                if(getSupportActionBar() != null && getSupportActionBar().getTitle() != null)
                    oldTitle = getSupportActionBar().getTitle().toString();
                else
                    oldTitle = "";
                TimetableActivity.state = 2;
                if(mMenu != null)
                    mMenu.findItem(R.id.action_set_default).setVisible(true);
                break;
            case REVERT:
                TimetableActivity.state = (int)stateBackup[0];
                TimetableActivity.gid = (int)stateBackup[1];
                if(!oldTitle.isEmpty())
                    title = oldTitle;
                else
                    title = null;
                if(mMenu != null)
                    mMenu.findItem(R.id.action_set_default).setVisible(TimetableActivity.state == 2);
                break;
        }
        mSectionsPagerAdapter.notifyDataSetChanged();
        if(title != null && title.isEmpty()){
            title = getResources().getString(R.string.app_name);
        }
        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    private class SearchInputListener implements TextWatcher{
        boolean hasCyrillic(String str){
            for(int i=0; i<str.length(); i++){
                if(
                        Character.UnicodeBlock.CYRILLIC.equals(Character.UnicodeBlock.of(str.charAt(i))) ||
                        Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY.equals(Character.UnicodeBlock.of(str.charAt(i)))
                        ){
                    return true;
                }
            }
            return false;
        }
        boolean hasPID(ArrayList<SearchSuggestion> list, String pid){
            for(int i=0; i<list.size(); i++){
                if(list.get(i).id.equals(pid))
                    return true;
            }
            return false;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
        @Override
        public void onTextChanged(final CharSequence s, int start, int before, int count) {
            final ArrayList<SearchSuggestion> cacheSugg = fillSuggestionsFromCache(s.toString());
            if(s.length() == 0){
                if(cacheSugg.size() == 0){
                    mSearchClear.setAlpha(0f);
                    mLineDivider.setVisibility(View.GONE);
                    mSearchSuggestions.setVisibility(View.GONE);
                    mSuggestionAdapter.clearDataset();
                }
            }else{
                mSearchClear.setAlpha(1f);
                mRequestQueue.cancelAll("__suggestion_loader");
                String url = "http://orir.ifmo.ru/mobile/API2.0/index.php";
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.isEmpty()){
                            if(cacheSugg.size() == 0){
                                mLineDivider.setVisibility(View.GONE);
                                mSearchSuggestions.setVisibility(View.GONE);
                                mSuggestionAdapter.clearDataset();
                            }
                            return;
                        }
                        try {
                            JSONArray res = new JSONArray(response);
                            ArrayList<SearchSuggestion> sugg = new ArrayList<>();
                            for(int i=0; i<res.length(); i++){
                                JSONObject item = (JSONObject)res.get(i);
                                if(hasPID(cacheSugg, item.getString("id")))
                                    continue;
                                sugg.add(
                                        new SearchSuggestion(
                                                SearchSuggestion.Source.Found,
                                                item.getString("id"),
                                                item.getString("title"),
                                                item.getString("id").matches("\\d+") ? SearchSuggestion.Type.Teacher : SearchSuggestion.Type.Group
                                        )
                                );
                            }
                            if(cacheSugg.size() == 0)
                                mSuggestionAdapter.clearDataset();
                            mSuggestionAdapter.appendToDataset(sugg);
                            if(sugg.size() > 0){
                                mLineDivider.setVisibility(View.VISIBLE);
                                mSearchSuggestions.setVisibility(View.VISIBLE);
                            }
                        }catch (JSONException e){
                            Log.e("a", e.toString());
                            if(cacheSugg.size() == 0){
                                mLineDivider.setVisibility(View.GONE);
                                mSearchSuggestions.setVisibility(View.GONE);
                                mSuggestionAdapter.clearDataset();
                            }
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.search_error), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("a", error.toString());
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.search_error), Toast.LENGTH_SHORT).show();
                        if(cacheSugg.size() == 0){
                            mLineDivider.setVisibility(View.GONE);
                            mSearchSuggestions.setVisibility(View.GONE);
                            mSuggestionAdapter.clearDataset();
                        }
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> params = new HashMap<>();
                        params.put("login", "ifmo01");
                        params.put("pass", "01ifmo04");
                        params.put("module", "schedule_lessons_search");
                            params.put(hasCyrillic(s.toString()) ? "lastname" : "gr", s.toString()); //Uri.encode());
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String,String> params = new HashMap<>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        return params;
                    }

                    @Override
                    public Object getTag(){
                        return "__suggestion_loader";
                    }
                };
                mRequestQueue.add(request);
            }
        }
    }

    private class SuggestionLongClickListener implements ItemClickSupport.OnItemLongClickListener{
        @Override
        public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
            final SearchSuggestion suggestion = mSuggestionAdapter.mDataset.get(position);
            if(suggestion.source == SearchSuggestion.Source.Cached){
                TimetableActivity.DBHelper.removeFromCache(suggestion.internal_id, true);
                Toast.makeText(TimetableActivity.this, R.string.cache_removed, Toast.LENGTH_SHORT).show();
                if(suggestion.internal_id == gid){
                    gid = -1;
                    setAppState(AppState.LIST, null);
                }
                mSuggestionAdapter.removeItem(position);
                return true;
            }
            return false;
        }
    }

    private class SuggestionClickListener implements ItemClickSupport.OnItemClickListener{
        @Override
        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            final SearchSuggestion suggestion = mSuggestionAdapter.mDataset.get(position);
            if (mSearchBarVisible) {
                onOptionsItemSelected(mMenu.findItem(R.id.action_search));
            }
            if(getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.app_name);
            setAppState(AppState.LOADING, TimetableActivity.DBHelper.getTitleByInternalId(gid));
            if(suggestion.source == SearchSuggestion.Source.Cached){
                TimetableActivity.gid = suggestion.internal_id;
                setAppState(AppState.LIST, TimetableActivity.DBHelper.getTitleByInternalId(gid));
                return;
            }
            loadSchedule(suggestion.id, suggestion.type);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetable, menu);
        menu.findItem(R.id.action_odd_even).setTitle(mOddWeek ? R.string.odd_week : R.string.even_week);
        menu.findItem(R.id.action_set_default).setVisible(TimetableActivity.state == 2);
        mMenu = menu;
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mSearchBarVisible){
            onOptionsItemSelected(mMenu.findItem(R.id.action_search));
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_odd_even){
            mOddWeek = !mOddWeek;
            if(mOddWeek) {
                item.setTitle(getString(R.string.odd_week));
                TimetableActivity.week = 1;
            }else {
                item.setTitle(getString(R.string.even_week));
                TimetableActivity.week = 2;
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
            return true;
        }else if(id == R.id.action_search){
            mSearchBarVisible = !mSearchBarVisible;
            int finalRadius = Math.max(mSearchBar.getWidth(), mSearchBar.getHeight());
            final View search = findViewById(id);
            int[] pos = new int[2];
            search.getLocationInWindow(pos);
            int xPos = pos[0] + search.getWidth()/2;
            int yPos = pos[1];
            if(mSearchBarVisible){
                fillSuggestionsFromCache("");
                SupportAnimator anim = ViewAnimationUtils.createCircularReveal(mSearchBar, xPos, yPos, 0, finalRadius);
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(mDim, "alpha", 0f, 1f);
                mDim.setVisibility(View.VISIBLE);
                mSearchBar.setVisibility(View.VISIBLE);
                if(mSearchInput.requestFocus()){
                    mIMM.showSoftInput(mSearchInput, InputMethodManager.SHOW_IMPLICIT);
                }
                anim.start();
                anim2.start();
            }else{
                SupportAnimator anim = ViewAnimationUtils.createCircularReveal(mSearchBar, xPos, yPos, finalRadius, 0);
                anim.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {
                    }

                    @Override
                    public void onAnimationEnd() {
                        mSearchBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel() {
                    }

                    @Override
                    public void onAnimationRepeat() {
                    }
                });
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(mDim, "alpha", 1f, 0f);
                anim2.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mDim.setVisibility(View.INVISIBLE);
                        mSearchInput.setText("");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                mSearchInput.clearFocus();
                mIMM.hideSoftInputFromWindow(mSearchInput.getWindowToken(), 0);
                anim.start();
                anim2.start();
            }
            return true;
        }else if(id == R.id.action_set_default){
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putLong("default_id", gid);
            editor.apply();
            Toast.makeText(TimetableActivity.this, R.string.default_was_set, Toast.LENGTH_SHORT).show();
        }else if(id == R.id.action_refresh){
            updateSchedule(gid);
        }else if(id == R.id.action_today){
            if(mPreferences.contains("week")){
                Calendar c = Calendar.getInstance();
                if((mPreferences.getInt("week", -1) - c.get(Calendar.WEEK_OF_YEAR)) % 2 == 0){
                    mOddWeek = mPreferences.getBoolean("odd_week", false);
                }else{
                    mOddWeek = !mPreferences.getBoolean("odd_week", false);
                }
                week = mOddWeek ? 1 : 2;
            }
            MenuItem weekItem = mMenu.findItem(R.id.action_odd_even);
            if(mOddWeek) {
                weekItem.setTitle(getString(R.string.odd_week));
                TimetableActivity.week = 1;
            }else {
                weekItem.setTitle(getString(R.string.even_week));
                TimetableActivity.week = 2;
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
            mViewPager.setCurrentItem(getInitialTab());
        }else if(id == R.id.action_about) {
            FragmentHtmlDialog dialog = new FragmentHtmlDialog();
            Bundle args = new Bundle();
            args.putString("title", getResources().getString(R.string.app_name));
            args.putCharSequence("html",
                    Html.fromHtml(getResources().getString(R.string.about_text))
            );
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "about_dialog");
        }

        return super.onOptionsItemSelected(item);
    }

    private int getInitialTab(){
        Calendar c = Calendar.getInstance();
        int tab = (c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek()) % 7;
        if(tab < 0)
            tab += 7;
        return tab;
    }

    private void checkUpdates(){
        String url = "http://storage.bakatrouble.tk/.timetable_ver";
        JsonRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode < response.getInt("version")){
                        String link = response.getString("link");

                        FragmentHtmlDialog dialog = new FragmentHtmlDialog();
                        Bundle args = new Bundle();
                        args.putString("title", getResources().getString(R.string.updates_available));
                        args.putCharSequence("html",
                                Html.fromHtml(String.format(getResources().getString(R.string.updates_text), link, link))
                        );
                        dialog.setArguments(args);
                        dialog.show(getSupportFragmentManager(), "updates_dialog");
                    }
                }catch(Exception e){}
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        public TimetableFragment[] mFragments = new TimetableFragment[7];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Parcelable saveState(){
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            mFragments[position] = TimetableFragment.newInstance(position, TimetableActivity.this);
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            Calendar c = Calendar.getInstance(l);
            if(position >= 0 && position < 7){
                int day = ((position + c.getFirstDayOfWeek()) % 7);
                c.set(Calendar.DAY_OF_WEEK, day);
                return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, l);
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
