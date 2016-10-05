
package com.akvone.dlcifmo.JournalModule;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.JournalModule.Adapters.ItemAdapter;
import com.akvone.dlcifmo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import draglistview.DragItem;
import draglistview.DragListView;

public class JournalFragment extends Fragment {
    private static final String TAG = "jf lcm";

    private static final int LAYOUT = R.layout.journal_fragment;
    public static JSONObject journal;

    MySwipeRefreshLayout mRefreshLayout;
    DragListView mDragListView;
    private ArrayList<Subject> mItemArray = new ArrayList<>();
    private String year;

    View view;
    private Menu optionsMenu;
    private boolean loadingJournal;
    private List<String> semesters = new ArrayList<>();

    public static boolean doNotSaveJournal;

    public static JournalFragment instance = null;

    public static JournalFragment getInstance(){
        if (instance == null){
            instance = new JournalFragment();
        }
        return instance;
    }

    public static JournalFragment newInstance() {
        doNotSaveJournal = false;
        Bundle args = new Bundle();
        JournalFragment fragment = new JournalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void delete(){
        instance = null;
    }

    public void saveJournal(JSONObject journal){
        Log.d("Save journal", "begin");
        //пробежаться по JSON и воткнуть актуальные веса.
        if (journal == null) return;
        try {
            JSONArray years = journal.getJSONArray("years");
            for (int i = 0; i < years.length(); i++) {
                JSONArray subs = years.getJSONObject(i).getJSONArray("subjects");
                for (int j = 0; j < subs.length(); j++) {

                    JSONObject subject = subs.getJSONObject(j);
                    int id = subject.getInt("id");
                    int weight = Journal.getInstance().getSubject(id).weight;
                    subject.put("weight", weight);
                }
            }
            FileOutputStream out = getContext().openFileOutput("journal.json", Context.MODE_PRIVATE);
            out.write(journal.toString().getBytes());
            out.close();
        } catch (JSONException e) {
            Log.d("Save journal", "fail");
            e.printStackTrace();
        } catch (IOException e){
            Log.d("Save journal", "file write fail");
        }
        Log.d("Save journal", "end");
    }

    public void setupListRecyclerView() {

        if (Journal.getInstance() != null) {
            ArrayList<Subject> items = Journal.getInstance().getYear(Journal.chosenYear).getSemester(Journal.chosenSemester);
            mItemArray = new ArrayList<>(items.size());
            mItemArray.ensureCapacity(items.size());
            for (int i = 0; i < items.size(); i++) {
                for (int j = 0; j < items.size(); j++) {
                    if (items.get(j).weight == i){
                        mItemArray.add(i, items.get(j));
                        break;
                    }
                }
            }
        }
        try {
            mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
            ItemAdapter adapter = new ItemAdapter(mItemArray, R.layout.journal_item, R.id.item_layout, true);
            mDragListView.setAdapter(adapter, true);
            mDragListView.setCanDragHorizontally(false);
            mDragListView.setCustomDragItem(new MyDragItem(getContext(), R.layout.journal_item));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateCards(){

        LoadJournalTask loadJournalTask = new LoadJournalTask();
        loadJournalTask.execute();
//
//        subjectAdapter = new SubjectAdapter(Subject.subjects, getActivity());
//        mRecyclerView.setAdapter(subjectAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.optionsMenu = menu;
        inflater.inflate(R.menu.journal_menu, menu);
        //я подарю торт тому,кто сможет объяснить, зачем я поместил этот код именно сюда.
        if (loadingJournal){
            setSwipeRefreshState(true);
        } else {
            setSwipeRefreshState(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == R.id.action_change_semester) {
            Journal j = Journal.getInstance();
            //TODO: унаследовать BaseELA или изменить вид итемов

            ArrayList<Map<String, String>> groupData;
            ArrayList<Map<String, String>> childItemData;
            ArrayList<ArrayList<Map<String, String>>> childData;
            Map<String, String> m;

            groupData = new ArrayList<>();
            childItemData = new ArrayList<>();
            childData = new ArrayList<>();
            m = new HashMap<>();
            int i = 0;
            for (Journal.Year y :
                    Journal.getInstance().getYears()) {
                childItemData = new ArrayList<>();
                m = new HashMap<>();
                m.put("yearName", y.getName() + ((Journal.chosenYear == i) ? " выбран" : ""));
                groupData.add(m);
                m = new HashMap<>();
                m.put("semName", getActivity().getString(R.string.first_semester)
                        + ((Journal.chosenSemester == 0) && (Journal.chosenYear == i) ? " выбран" : ""));
                childItemData.add(m);
                m = new HashMap<>();
                m.put("semName", getActivity().getString(R.string.second_semester)
                        + ((Journal.chosenSemester == 1) && (Journal.chosenYear == i) ? " выбран" : ""));
                childItemData.add(m);
                childData.add(childItemData);
                i++;
            }
            String[] groupFrom = {"yearName"};
            String[] childFrom = {"semName"};
            int[] groupTo = {android.R.id.text1};
            int[] childTo = {android.R.id.text1};

            SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
                    getActivity(),
                    groupData,
                    android.R.layout.simple_expandable_list_item_1,
                    groupFrom,
                    groupTo,
                    childData,
                    android.R.layout.simple_list_item_1,
                    childFrom,
                    childTo
            );

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            ExpandableListView elv = new ExpandableListView(getActivity());
            elv.setAdapter(adapter);
            builder.setView(elv);

            final AlertDialog dialog = builder.create();
            dialog.show();
            elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    Toast.makeText(getActivity(),
                            (groupPosition * 2 + childPosition + 1) + " семестр",
                            Toast.LENGTH_SHORT).show();
                    Journal.chosenSemester = childPosition;
                    Journal.chosenYear = groupPosition;
                    dialog.hide();
//                        Log.d("Subject", "Selected semester: " + (which + 1));
                    setupListRecyclerView();
                    return true;
                }
            });


//                semesters.clear();
//                for (int i = 0; i < j.countYears(); i++){
//                    Journal.Year y = j.getYear(i);
//                    semesters.add(y.getSemesters().get(0).getNumber() + " семестр " + y.getName() +
//                            ((i == Journal.chosenYear)&&(Journal.chosenSemester == 0) ? "выбран" : ""));
//                    semesters.add(y.getSemesters().get(1).getNumber() + " семестр " + y.getName() +
//                            ((i == Journal.chosenYear)&&(Journal.chosenSemester == 1) ? "выбран" : ""));
//                }
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle(R.string.action_chose_semester);
//                ListAdapter adapter = new ArrayAdapter<>(getActivity(),
//                        android.R.layout.select_dialog_singlechoice, semesters);
//                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Journal.chosenSemester = (which) % 2;
//                        Journal.chosenYear = which / 2;
//                        Log.d("Subject", "Selected semester: " + (which + 1));
//                        setupListRecyclerView();
//                    }
//                });
//                builder.create().show();
            //Change adapter
//                mLoadJournalTask = new LoadJournalTask(this);
//                mLoadJournalTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Lifecycle overrides
     */

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("lcm", "Fragment onAttach");
        if (Journal.getInstance() == null) {
            new LoadSavedJournal(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        ((AppCompatActivity) context).setTitle(context.getString(R.string.journalTitle));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.d("lcm", "Fragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("lcm", "Fragment onCreateView");
        view = inflater.inflate(LAYOUT, container, false);
        mRefreshLayout  = (MySwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                //Если начнём перетаскивать итем при включеном Refresh, получим мочу
                mRefreshLayout.setEnabled(false);
//                Toast.makeText(mDragListView.getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                mRefreshLayout.setEnabled(true);
                //set weight to subject
                //Адаптер сам меняет порядок предметов в mItemArray
                //Значит, weight можно положить как item.position
                //Или от него можно отказаться, оставив id для синхронизации с загруженным
                //а сохранять сортированый список
                //Сохранять сортированый список идея так себе, ибо
                //a) тыкать JSON
                //б) сортировка может и потеряться
                for (int i = 0; i <= ((fromPosition > toPosition) ? fromPosition : toPosition); i++) {
                    mItemArray.get(i).weight = i;
                }
                Journal.getInstance().setSemesterItems(mItemArray);
//                Toast.makeText(mDragListView.getContext(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
            }
        });

        mRefreshLayout.setScrollingView(mDragListView.getRecyclerView());
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent),
                ContextCompat.getColor(getContext(), R.color.colorPrimary),
                ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                updateCards();
                Log.d("Swipe", "Ну я как бы всё...");
            }
        });

        setupListRecyclerView();

//        mDragListView.setCustomDragItem();
//        mRecyclerView.setAdapter(new SubjectAdapter(Subject.subjects, getActivity()));
//        if (Subject.subjects.size() == 0)
//        {
//            SharedPreferences preferences = getActivity().getSharedPreferences(PREF_MOCK_FILE, Context.MODE_PRIVATE);
//            int amount = preferences.getInt(PREF_MOCK_SUBJECTS_AMOUNT, 0);
//            if (amount != 0) {
//                String[] names = new String[amount];
//                int[] exam = new int[amount];
//                float[] points = new float[amount];
//                for (int i = 0; i < amount; i++)
//                {
//                    names[i] = preferences.getString(PREF_MOCK_SUBJECT_NAME+i, "Subject name is lost");
//                    exam[i] = preferences.getInt(PREF_MOCK_SUBJECT_TYPE+i, 0);
//                    points[i] = preferences.getFloat(PREF_MOCK_SUBJECT_POINTS+i, -1);
//                }
//                mRecyclerView.setAdapter(new MockAdapter(names, exam, points, getActivity()));
//            }
//        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.journalTitle);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        if (!doNotSaveJournal) {
            saveJournal(journal);
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        //save journal
        Log.d("Journal Fragment", "onDestroy: "+ doNotSaveJournal);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setSwipeRefreshState(final boolean refreshing) {

        if (mRefreshLayout != null) {
            if (refreshing) {
                mRefreshLayout.setRefreshing(true);
            } else {
                mRefreshLayout.setRefreshing(false);

            }
        }

    }

    public void setLoadingJournal(boolean loadingJournal) {
        this.loadingJournal = loadingJournal;
    }

    public List<String> getSemesters() {
        return semesters;
    }

    private static class MyDragItem extends DragItem {

        public MyDragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            CharSequence type = ((TextView) clickedView.findViewById(R.id.subjectType)).getText();
            CharSequence title = ((TextView) clickedView.findViewById(R.id.title)).getText();
            CharSequence points = ((TextView) clickedView.findViewById(R.id.points)).getText();
            ((TextView) dragView.findViewById(R.id.subjectType)).setText(type);
            ((TextView) dragView.findViewById(R.id.title)).setText(title);
            ((TextView) dragView.findViewById(R.id.points)).setText(points);
            dragView.setBackgroundColor(dragView.getResources().getColor(R.color.colorGrey));
        }


    }

//    private class JournalWriter{
//        //Это, похоже, занмает 3 секунды, да ещё и не работает
//        private JsonWriter writer;
//        private OutputStream out;
////        "years":[{
////            "group":"0000",
////            "studyyear":"2012/2013",
////            "subjects":[{
////                "name":"Предмет",
////                "semester":"1",
////                "marks":[{
////                    "mark":"зачет",
////                    "markdate":"01.01.2013",
////                    "worktype":"Зачет"
////                }],
////                "points":[{
////                    "variable":"Семестр 1",
////                    "max":"100", "limit":"60", "value":"80"
////                }]
//
//
//        public JournalWriter(OutputStream out) throws IOException{
//            this.out = out;
//            writer = new JsonWriter(new OutputStreamWriter(out, "Windows-1251"));
//
//        }
//        public void write() throws IOException{
//            String oldYear = "";
//            writer.setIndent("  ");
//            writer.beginObject(); // core object
//            writer.name("years").beginArray();
//            for (Subject s :
//                    Subject.subjects) {
//                if (!oldYear.equals(s.getStudyYear())){
//                    if (!oldYear.equals("")){
//                        writer.endArray(); //end of subject array
//                        writer.endObject(); // end of one year
//                    }
//                    oldYear = s.getStudyYear();
//                    writer.beginObject(); //begin of new year
//                    writer.name("group").value(s.getGroup());
//                    writer.name("studyyear").value(s.getStudyYear());
//                    writer.name("subjects").beginArray();
//                }
//                writeSubject(s);
//            }
//            writer.endArray(); //end year array
//            writer.endObject(); //end core object
//            writer.flush();
//            writer.close();
//        }
//
//        private void writeSubject(Subject s) throws IOException{
//            writer.beginObject();
//            writer.name("name").value(s.getName());
//            writer.name("semester").value(s.getSemester() + "");
//            writer.name("marks").beginArray();
//            for (Subject.Marks m :
//                    s.getMarks()) {
//                writer.beginObject();
//                writer.name("mark").value(m.getMark());
//                writer.name("markdate").value(m.getMarkDate());
//                writer.name("worktype").value(m.getTp());
//                writer.endObject();
//            }
//            writer.endArray();
//            if (s.getPoints().size() != 0){
//                writer.name("points").beginArray();
//                for (Subject.Points p :
//                        s.getPoints()) {
//                    writer.beginObject();
//                    writer.name("variable").value(p.getVariable());
//                    writer.name("max").value(p.getMax());
//                    writer.name("limit").value(p.getLimit());
//                    writer.name("value").value(p.getValue());
//                    writer.endObject();
//                }
//                writer.endArray();
//            }
//            writer.endObject();
//        }
//    }

    public JournalFragment() {
        // Required empty public constructor
    }
}
