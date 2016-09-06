package com.akvone.dlcifmo.EnrollModule;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.akvone.dlcifmo.R;

import java.util.ArrayList;
import java.util.List;

public class BookItemAdaptor extends RecyclerView.Adapter<BookItemAdaptor.Holder>  {
    final String[] times = {"10:00", "10:40", "11:20", "12:00", "12:40", "13:20", "14:00", "14:40", "15:20", "16:00", "16:40", "17:20", "18:00"};
    List<Item> data = new ArrayList<>();

    public class Item {
        String interval;
        String place;
        boolean checked;
        boolean booked;
    }

    public BookItemAdaptor(List<String> places, String date){
        ArrayList<RecordItem> suitableRecords = new ArrayList<>();
        for (RecordItem it :
                EnrollMainFragment.records) {
            if (it.date.equals(date)) {
                suitableRecords.add(it);
            }
        }
        boolean closeNext =false;
        RecordItem hangingRecord = null;
        for (int i = 0; i < places.size(); i++){
            Item item = new Item();
            if (closeNext){
                item.booked = true;
                if (hangingRecord.end.equals(times[i+1])){
                    closeNext = false;
                }
            } else {
                RecordItem record = startsWith(times[i], suitableRecords);
                if (record != null){
                    item.booked = true;
                    if (!recordEndsWith(record, times[i+1])){
                        closeNext = true;
                        hangingRecord = record;
                    }
                }
            }
            item.interval = times[i]+" - " + times[i+1];
            item.place = places.get(i);
            item.checked = false;
            data.add(item);
        }

    }

    private RecordItem startsWith(String time, ArrayList<RecordItem> records){
        if (records == null) return null;
        for (RecordItem item:
             records) {
            if (item.begin.equals(time) && !item.status.equals("отклонена")) return item;
        }
        return null;
    }

    private boolean recordEndsWith(RecordItem rec, String time){
        return rec.end.equals(time);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.enroll_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Item item = data.get(position);
        holder.title.setText(item.interval);
        holder.places.setText(item.place+" свободных мест");
        if (item.booked){
            holder.itemView.setBackgroundColor(Color.GREEN);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        TextView title;
        TextView places;

        public Holder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.registrationItemTitle);
            places  = (TextView) itemView.findViewById(R.id.registrationItemPlaces);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Item item = data.get(getAdapterPosition());
                    if (!item.booked) {
                        item.checked = !item.checked;
                        if (item.checked) {
                            itemView.setBackgroundColor(Color.CYAN);
                        } else {
                            itemView.setBackgroundColor(Color.TRANSPARENT);
                        }
                        int row = 0;
                        for (Item i :
                                data) {
                            if (i.booked || i.checked ) {
                                row++;
                                if (row == 4){item.checked = false;
                                    int colorFrom = Color.RED;
//                            int colorFrom = getResources().getColor(R.color.red);
                                    int colorTo = Color.TRANSPARENT;
//                            int colorTo = getResources().getColor(R.color.blue);
                                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                                    colorAnimation.setDuration(250); // milliseconds
                                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animator) {
                                            itemView.setBackgroundColor((int) animator.getAnimatedValue());
                                        }

                                    });
                                    colorAnimation.start();
                                    break;
                                }
                            } else row = 0;
                        }
                    }
                }
            });

        }
    }

    public String[] getTimes() {
        return times;
    }

    public List<Item> getData() {
        return data;
    }
}
