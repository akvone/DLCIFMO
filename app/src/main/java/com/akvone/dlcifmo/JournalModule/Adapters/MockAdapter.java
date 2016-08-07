package com.akvone.dlcifmo.JournalModule.Adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.R;

public class MockAdapter extends RecyclerView.Adapter<MockAdapter.MockHolder>{



    String[] names;
    int[] type;
    float[] points;
    private Context context;

    public MockAdapter(String[] names, int[] type, float[] points, Context context) {
        this.names = names;
        this.type = type;
        this.points = points;
        this.context = context;
    }

    @Override
    public MockHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_item, parent, false);

        return new MockHolder(view);
    }

    @Override
    public void onBindViewHolder(MockHolder holder, int position) {
        holder.title.setText(names[position]);
        switch (type[position]){
            case Constants.SUBJECT_TYPE_CREDIT:
                holder.type.setText("Зачет");
                break;
            case Constants.SUBJECT_TYPE_EXAM:
                holder.type.setText("Экзамен");
                break;
            case Constants.SUBJECT_TYPE_COURSE:
                holder.type.setText("Курсовая работа");
                break;
            default:
                holder.type.setText("Чет я хз");
                break;
        }
        boolean isClosed = (points[position] >= 60);
        if (isClosed){
//                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.closedSubject, null));
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorClosedSubject));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.cardView.setCardBackgroundColor(context.getColor(R.color.colorClosedSubject));
            }
        }
        holder.points.setText(points[position]+"");
    }

    @Override
    public int getItemCount() {
        return names.length;
    }

    public class MockHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView title;
        TextView type;
        TextView points;

        public MockHolder(View itemView) {
            super(itemView);

//            cardView = (CardView) itemView.findViewById(R.id.card_view);
            title = (TextView) itemView.findViewById(R.id.title);
            type = (TextView) itemView.findViewById(R.id.subjectType);
            points = (TextView) itemView.findViewById(R.id.points);
        }
    }
}
