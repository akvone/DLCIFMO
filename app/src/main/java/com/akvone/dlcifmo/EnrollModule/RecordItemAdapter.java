package com.akvone.dlcifmo.EnrollModule;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akvone.dlcifmo.R;

import java.util.List;

/**
 * Created by 1 on 01.09.2016.
 */
public class RecordItemAdapter extends RecyclerView.Adapter<RecordItemAdapter.ViewHolder> {
    List<RecordItem> data;
    EnrollMainFragment enrollMainFragment;

    public RecordItemAdapter(List<RecordItem> data, EnrollMainFragment fragment) {
        this.data = data;
        enrollMainFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.enroll_record_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RecordItem item = data.get(position);
        holder.num.setText(item.num);
        holder.date.setText(item.date);
        holder.time.setText(item.begin + "-" + item.end);
        holder.pitched.setText(item.pitched);
        holder.status.setText(item.status);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView num;
        TextView date;
        TextView time;
        TextView pitched;
        TextView status;
        ImageView withdraw;
        public ViewHolder(View itemView) {
            super(itemView);
            num = (TextView) itemView.findViewById(R.id.record_num);
            date = ((TextView) itemView.findViewById(R.id.record_date));
            time = (TextView) itemView.findViewById(R.id.record_time);
            pitched = (TextView) itemView.findViewById(R.id.record_pitched);
            status = (TextView) itemView.findViewById(R.id.record_status);
            withdraw = (ImageView) itemView.findViewById(R.id.withdrawEnroll);
            withdraw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enrollMainFragment.withdraw(data.get(getAdapterPosition()).id, data.get(getAdapterPosition()).date, getAdapterPosition());
                    //WithdrawEnroll().execute(data.get(getAdapterPosition()).id)
                }
            });

        }
    }
}
