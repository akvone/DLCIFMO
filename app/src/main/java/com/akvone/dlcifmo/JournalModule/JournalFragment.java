
package com.akvone.dlcifmo.JournalModule;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.JournalModule.Adapters.ItemAdapter;
import com.akvone.dlcifmo.JournalModule.Adapters.SubjectAdapter;
import com.akvone.dlcifmo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import draglistview.DragItem;
import draglistview.DragListView;

public class JournalFragment extends Fragment {


    private static final int LAYOUT = R.layout.journal_fragment;

    MySwipeRefreshLayout mRefreshLayout;
    DragListView mDragListView;
    SubjectAdapter subjectAdapter;
    private ArrayList<Subject> mItemArray = new ArrayList<>();

    View view;
    private Menu optionsMenu;
    private boolean loadingJournal;
    private List<String> semesters = new ArrayList<>();

    public static JournalFragment instance = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        mRefreshLayout  = (MySwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mDragListView = (DragListView) view.findViewById(R.id.drag_list_view);
        mDragListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mDragListView.setDragListListener(new DragListView.DragListListenerAdapter() {
            @Override
            public void onItemDragStarted(int position) {
                //Если начнём перетаскивать итем при включеном Refresh, получим мочу
                mRefreshLayout.setEnabled(false);
                Toast.makeText(mDragListView.getContext(), "Start - position: " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onItemDragEnded(int fromPosition, int toPosition) {
                mRefreshLayout.setEnabled(true);
                Toast.makeText(mDragListView.getContext(), "End - position: " + toPosition, Toast.LENGTH_SHORT).show();
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

    public void saveJournal(JSONObject journal){
        Log.d("Save journal", "begin");
        try {
            FileOutputStream out = getContext().openFileOutput("journal.json", Context.MODE_PRIVATE);
            out.write(journal.toString().getBytes());
//            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Save journal", "fail");
        }
        Log.d("Save journal", "end");
    }

    public void setupListRecyclerView() {
        mItemArray.clear();
        int i = 0;
        for (Subject s :
                Subject.subjects) {
            if (s.getSemester() == Subject.CHOSEN_SEMESTER){
                s.id = i++;
                mItemArray.add(s);
            }
        }
        mDragListView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter adapter = new ItemAdapter(mItemArray, R.layout.journal_item, R.id.points, false);
        mDragListView.setAdapter(adapter, true);
        mDragListView.setCanDragHorizontally(false);
        mDragListView.setCustomDragItem(new MyDragItem(getContext(), R.layout.journal_item));
    }

    public static JournalFragment getInstance(){
        if (instance == null){
            instance = new JournalFragment();
        }
        return instance;
    }

    public static JournalFragment newInstance() {

        Bundle args = new Bundle();
        JournalFragment fragment = new JournalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    public void updateCards(){

        LoadJournalTask loadJournalTask = new LoadJournalTask(JournalFragment.getInstance());
        loadJournalTask.execute();
//
//        subjectAdapter = new SubjectAdapter(Subject.subjects, getActivity());
//        mRecyclerView.setAdapter(subjectAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Log.d("journal", "Fragment onCreate");

        if (Subject.subjects.size() == 0) {
//            new LoadSavedJournal(this).execute();
            LoadJournalTask loadJournalTask = new LoadJournalTask(this);
            loadJournalTask.execute();
        }
        Calendar calendar = new GregorianCalendar();
        Subject.isAutumnSemester = calendar.get(Calendar.MONTH) > 5;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.optionsMenu = menu;
        inflater.inflate(R.menu.journal_menu, menu);
        if (loadingJournal){
            setSwipeRefreshState(true);
        } else {
            setSwipeRefreshState(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_change_semester:
                semesters.clear();
                for (int j = 1; j <= Subject.years*2; j++){
                    semesters.add(j + " семестр" + ((j == Subject.CHOSEN_SEMESTER) ? " (выбран)" : ""));
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.action_chose_semester);
                ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.select_dialog_singlechoice, semesters);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Subject.CHOSEN_SEMESTER = which + 1;
                        Log.d("Subject", "Selected semester: " + (which +1));
                        setupListRecyclerView();
                    }
                });
                builder.create().show();
                //Change adapter
//                mLoadJournalTask = new LoadJournalTask(this);
//                mLoadJournalTask.execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public JournalFragment() {
        // Required empty public constructor
    }


    public List<String> getSemesters() {
        return semesters;
    }

    public void setLoadingJournal(boolean loadingJournal) {
        this.loadingJournal = loadingJournal;
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
            dragView.setBackgroundColor(dragView.getResources().getColor(R.color.colorPrimary));
        }
    }
}
