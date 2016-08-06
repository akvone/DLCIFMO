package com.akvone.dlcifmo.JournalModule.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akvone.dlcifmo.JournalModule.Subject;
import com.akvone.dlcifmo.R;

public class PointsAdapter extends RecyclerView.Adapter<PointsAdapter.PointsHolder>  {

    Subject subject;

    public PointsAdapter(Subject subject){
        this.subject = subject;
    }

    @Override
    public PointsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_points_item, parent, false);

        return new PointsHolder(view);
    }

    @Override
    public void onBindViewHolder(PointsHolder holder, int position) {
        Subject.Points point;
        point = subject.getPoints().get(position);
        holder.title.setText(point.getVariable());
        String points = point.getValue() + " из " + point.getMax();
        holder.points.setText(points);
//        holder.index.setText(point.);


    }

    @Override
    public int getItemCount() {
        return subject.getPoints().size();
    }

    public class PointsHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView points;
        public PointsHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.pointTitle);
            points = (TextView) itemView.findViewById(R.id.points);
        }
    }
}
