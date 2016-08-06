
package com.akvone.dlcifmo.JournalModule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.akvone.dlcifmo.JournalModule.Adapters.MockAdapter;
import com.akvone.dlcifmo.JournalModule.Adapters.SubjectAdapter;
import com.akvone.dlcifmo.R;
import static com.akvone.dlcifmo.Constants.*;
public class JournalFragment extends Fragment {


    private static final int LAYOUT = R.layout.journal_fragment;

    RecyclerView recyclerView;
    SubjectAdapter subjectAdapter;

    View view;
    private Context context;

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

    public static JournalFragment newInstance() {
        JournalFragment fragment = new JournalFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Subject.subjects.size() == 0) {
            LoadJournalTask task = new LoadJournalTask(this);
            task.execute();
        }
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
    public void onDetach() {
        super.onDetach();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JournalFragment() {
        // Required empty public constructor
    }
}
