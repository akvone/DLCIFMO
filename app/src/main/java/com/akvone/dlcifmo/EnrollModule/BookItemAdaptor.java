package com.akvone.dlcifmo.EnrollModule;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    public BookItemAdaptor(List<String> places){
        for (int i = 0; i < times.length-1; i++){
            Item item = new Item();
            item.interval = times[i]+" - " + times[i+1];
            if (i < places.size()) {
                item.place = places.get(i);
            } else {
                item.place = "☺";
            }
            item.checked = false;
            data.add(item);
        }

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
                    data.get(getAdapterPosition()).checked = !data.get(getAdapterPosition()).checked;
                    if (data.get(getAdapterPosition()).checked){
                        itemView.setBackgroundColor(Color.CYAN);
                    } else {
                        itemView.setBackgroundColor(Color.TRANSPARENT);
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
