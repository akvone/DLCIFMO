package com.akvone.dlcifmo.JournalModule.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.akvone.dlcifmo.JournalModule.PointsViewFragment;
import com.akvone.dlcifmo.JournalModule.Subject;
import com.akvone.dlcifmo.OnFragmentInteractionListener;
import com.akvone.dlcifmo.R;
import static com.akvone.dlcifmo.Constants.*;

import java.util.ArrayList;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectHolder> {

    private List<Subject> data = new ArrayList<>();
    private Context context;

    public SubjectAdapter(List<Subject> data, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_MOCK_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences
                .edit();
        int i = 0;
        for (Subject s :
                data) {
            if (s.getSemester() == Subject.CHOSEN_SEMESTER) {
                this.data.add(s);
                //save to device
                int type;
                if (Subject.CURRENT_SEMESTER == Subject.CHOSEN_SEMESTER) {
                    switch (s.getType()){
                        case "Зачет":
                            type = SUBJECT_TYPE_CREDIT;
                            break;
                        case "Экзамен":
                            type = SUBJECT_TYPE_EXAM;
                            break;
                        case "Курсовая работа":
                            type = SUBJECT_TYPE_COURSE;
                            break;
                        default:
                            type = 0;
                            break;
                    }
                    editor.putString(PREF_MOCK_SUBJECT_NAME+i, s.getName());
                    editor.putInt(PREF_MOCK_SUBJECT_TYPE+i, type);
                    editor.putFloat(PREF_MOCK_SUBJECT_POINTS+i++, s.getTotalPoints().floatValue());
                }
            }
            if (Subject.CURRENT_SEMESTER == Subject.CHOSEN_SEMESTER) {
                editor.putInt(PREF_MOCK_SUBJECTS_AMOUNT, i);
                editor.apply();
            }
        }

        this.context = context;
    }

    @Override
    public SubjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //определяем содержимое RecycleView как journal_item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_item, parent, false);

        return new SubjectHolder(view);
    }


    @Override
    public void onBindViewHolder(SubjectHolder holder, int position) {
        //Заполнение данных cardView
        Subject item = data.get(position);
        holder.title.setText(item.getName());
        String s = item.getTotalPoints() + "";
        holder.points.setText(s);
        if (item.isClosed()){
//                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.closedSubject, null));
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorClosedSubject));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.cardView.setCardBackgroundColor(context.getColor(R.color.colorClosedSubject));
            }
        }
        holder.cardView.setOnClickListener(new onCardClickListener(item));
        holder.type.setText(item.getType());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class SubjectHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView title;
        TextView type;
        TextView points;

        public SubjectHolder(View itemView) {
            super(itemView);

//            cardView = (CardView) itemView.findViewById(R.id.card_view);
            title = (TextView) itemView.findViewById(R.id.title);
            type = (TextView) itemView.findViewById(R.id.subjectType);
            points = (TextView) itemView.findViewById(R.id.points);
        }
    }

    private class onCardClickListener implements  CardView.OnClickListener{
        Subject subject;
        public onCardClickListener(Subject item){
            subject = item;
        }
        @Override
        public void onClick(View v) {
            OnFragmentInteractionListener activity = (OnFragmentInteractionListener) context;
            activity.changeFragment(PointsViewFragment.getInstance(context, subject));
        }
    }
}
