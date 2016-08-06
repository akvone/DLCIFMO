package com.akvone.dlcifmo.JournalModule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akvone.dlcifmo.JournalModule.Adapters.PointsAdapter;
import com.akvone.dlcifmo.R;

public class PointsViewFragment extends Fragment {

    private static final int LAYOUT = R.layout.journal_fragment_points;
    View view;
    Context context;
    RecyclerView recyclerView;
    Subject subject;

    private static PointsViewFragment instance = null;

    public static PointsViewFragment getInstance(Context context, Subject item){
        if (instance == null) {
            instance = new PointsViewFragment();
            instance.context = context;
            instance.subject = item;
        }
        if (!instance.subject.equals(item)){
            instance.subject = item;
        }
        return instance;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(LAYOUT, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        if ((subject.getPoints() == null)||(subject.getPoints().size() == 0)){
            TextView textView = (TextView) view.findViewById(R.id.noPointsData);
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new PointsAdapter(subject));
        }
        return view;
    }

    public PointsViewFragment() {
        super();
    }
}
