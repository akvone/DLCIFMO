
package com.akvone.dlcifmo.JournalModule;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import android.widget.ListAdapter;

import com.akvone.dlcifmo.JournalModule.Adapters.MockAdapter;
import com.akvone.dlcifmo.JournalModule.Adapters.SubjectAdapter;
import com.akvone.dlcifmo.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.akvone.dlcifmo.Constants.*;
public class JournalFragment extends Fragment {


    private static final int LAYOUT = R.layout.journal_fragment;

    RecyclerView recyclerView;
    SubjectAdapter subjectAdapter;

    View view;
    private Context context;
    private Menu optionsMenu;
    private LoadJournalTask mLoadJournalTask;
    private boolean loadingJournal;
    private List<String> semesters = new ArrayList<>();

    public static JournalFragment instance = null;

    public static JournalFragment getInstance(){
        if (instance == null){
            instance = new JournalFragment();
        }
        return instance;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (recyclerView.getAdapter() == null)
        {
            recyclerView.setAdapter(subjectAdapter);
        }
    }

    public void updateCards(){

        subjectAdapter = new SubjectAdapter(Subject.subjects, context);
        recyclerView.setAdapter(subjectAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (Subject.subjects.size() == 0) {
            mLoadJournalTask = new LoadJournalTask(this);
            mLoadJournalTask.execute();
        }
        Calendar calendar = new GregorianCalendar();
        Subject.isAutumnSemester = calendar.get(Calendar.MONTH) > 5;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SubjectAdapter(Subject.subjects, context));
        if (Subject.subjects.size() == 0)
        {
            SharedPreferences preferences = context.getSharedPreferences(PREF_MOCK_FILE, Context.MODE_PRIVATE);
            int amount = preferences.getInt(PREF_MOCK_SUBJECTS_AMOUNT, 0);
            if (amount != 0) {
                String[] names = new String[amount];
                int[] exam = new int[amount];
                float[] points = new float[amount];
                for (int i = 0; i < amount; i++)
                {
                    names[i] = preferences.getString(PREF_MOCK_SUBJECT_NAME+i, "Subject name is lost");
                    exam[i] = preferences.getInt(PREF_MOCK_SUBJECT_TYPE+i, 0);
                    points[i] = preferences.getFloat(PREF_MOCK_SUBJECT_POINTS+i, -1);
                }
                recyclerView.setAdapter(new MockAdapter(names, exam, points, context));
            }
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        instance.setContext(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.optionsMenu = menu;
        inflater.inflate(R.menu.journal_menu, menu);
        if (loadingJournal){
            setRefreshActionButtonState(true);
        } else {
            setRefreshActionButtonState(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                mLoadJournalTask = new LoadJournalTask(this);
                mLoadJournalTask.execute();
                return true;
            case R.id.action_change_semester:
                semesters.clear();
                for (int j = 1; j <= Subject.years*2; j++){
                    semesters.add(j + " семестр" + ((j == Subject.CHOSEN_SEMESTER) ? " (выбран)" : ""));
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.action_chose_semester);
                ListAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice, semesters);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Subject.CHOSEN_SEMESTER = which + 1;
                        Log.d("Subject", "Selected semester: " + (which +1));
                        updateCards();
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

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setContext(Context context) {
        this.context = context;
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
}
