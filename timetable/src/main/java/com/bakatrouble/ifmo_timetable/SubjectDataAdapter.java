package com.bakatrouble.ifmo_timetable;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by bakatrouble on 31.08.2015.
 */
public class SubjectDataAdapter extends RecyclerView.Adapter<SubjectDataAdapter.ViewHolder>  {
    public ArrayList<Subject> mDataset;
    ViewGroup mParent;
    public ArrayList<Integer> processed = new ArrayList<>();
    private TimetableActivity act;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SubjectDataAdapter(TimetableActivity act) {
        mDataset = new ArrayList<>();
        this.act = act;
    }

    public void clearDataset(){
        mDataset.clear();
    }

    public void updateDataset(ArrayList<Subject> newDataset){
        this.clearDataset();
        mDataset.addAll(newDataset);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SubjectDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        mParent = parent;

        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.subject_card, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView, this.act);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        if(processed.contains(position)){
            return;
        }
        processed.add(position);
        if(position > 0){
            String[] end = mDataset.get(position-1).time_end.split(":");
            String[] begin = mDataset.get(position).time_begin.split(":");
            int iEnd = Integer.parseInt(end[0]) * 60 + Integer.parseInt(end[1]);
            int iBegin = Integer.parseInt(begin[0]) * 60 + Integer.parseInt(begin[1]);
            if(iBegin - iEnd > 80){
                viewHolder.window.setVisibility(View.VISIBLE);
            }
        }
        viewHolder.time_begin.setText(mDataset.get(position).time_begin);
        viewHolder.time_end.setText(mDataset.get(position).time_end);
        viewHolder.title.setText(mDataset.get(position).title);
        viewHolder.teacher.setText(mDataset.get(position).teacher);
        if(mDataset.get(position).teacher.isEmpty())
            viewHolder.teacher.setVisibility(View.GONE);
        viewHolder.place.setText(mDataset.get(position).place);
        if(mDataset.get(position).addons.size() > 0){
            for(int i=0; i<mDataset.get(position).addons.size(); i++){
                viewHolder.teacher.setText(viewHolder.teacher.getText() + ", " + mDataset.get(position).addons.get(i).teacher);
            }
        }
        viewHolder.wrapper.setTag(R.id.subject_jid, mDataset.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.wrapper.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView time_begin;
        public TextView time_end;
        public TextView title;
        public TextView teacher;
        public TextView place;
        public RelativeLayout window;
        public RelativeLayout wrapper;
        public RelativeLayout card_root;
        private TimetableActivity act;

        public ViewHolder(View itemLayoutView, TimetableActivity act) {
            super(itemLayoutView);
            time_begin = (TextView) itemLayoutView.findViewById(R.id.time_begin);
            time_end = (TextView) itemLayoutView.findViewById(R.id.time_end);
            title = (TextView) itemLayoutView.findViewById(R.id.subject);
            teacher = (TextView) itemLayoutView.findViewById(R.id.teacher);
            place = (TextView) itemLayoutView.findViewById(R.id.place);
            window = (RelativeLayout) itemLayoutView.findViewById(R.id.window_text_wrapper);
            wrapper = (RelativeLayout) itemLayoutView.findViewById(R.id.card_wrapper);
            card_root = (RelativeLayout) itemLayoutView.findViewById(R.id.subject_root);
            this.act = act;
            wrapper.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {
            final Subject subj = (Subject)v.getTag(R.id.subject_jid);
            if(subj != null){
                if(subj.jid == null || subj.jid.equals("0")){
                    return false;
                }
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                Menu menu = popupMenu.getMenu();
                if(subj.type == SearchSuggestion.Type.Teacher){
                    String title = v.getResources().getString(R.string.open_group_schedule);
                    menu.add(0, 0, 0, String.format(title, subj.teacher)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            act.loadSchedule(subj.jid, SearchSuggestion.Type.Group);
                            return false;
                        }
                    });
                    for(int i=0; i<subj.addons.size(); i++){
                        final int index = i;
                        menu.add(0, 0, 0, String.format(title, subj.addons.get(i).teacher)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                act.loadSchedule(subj.addons.get(index).jid, SearchSuggestion.Type.Group);
                                return false;
                            }
                        });
                    }
                }else if(subj.type == SearchSuggestion.Type.Group){
                    String title = v.getResources().getString(R.string.open_teacher_schedule);
                    menu.add(0, 0, 0, title).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            act.loadSchedule(subj.jid, SearchSuggestion.Type.Teacher);
                            return false;
                        }
                    });
                }
                popupMenu.show();
            }
            return true;
        }
    }
}
