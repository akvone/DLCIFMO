/**
 * Copyright 2014 Magnus Woxblom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akvone.dlcifmo.JournalModule.Adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.JournalModule.PointsViewFragment;
import com.akvone.dlcifmo.JournalModule.Subject;
import com.akvone.dlcifmo.R;

import java.util.ArrayList;

import draglistview.DragItemAdapter;

public class ItemAdapter extends DragItemAdapter<Subject, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;

    public ItemAdapter(ArrayList<Subject> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
//        String text = mItemList.get(position).second;
//        holder.mText.setText(text);
//        holder.itemView.setTag(text);

        Subject item = mItemList.get(position);
        holder.title.setText(item.getName());
        String s = item.getTotalPoints() + "";
        holder.points.setText(s);
        holder.subject = item;
        if (item.isClosed()){
//                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.closedSubject, null));
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorClosedSubject));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                holder.cardView.setCardBackgroundColor(context.getColor(R.color.colorClosedSubject));
//            }
        }
//        holder.cardView.setOnClickListener(new onCardClickListener(item));
        holder.type.setText(item.getType());
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).id;

//        return mItemList.indexOf(mItemList.get(position));
//        ?????
//        return position;
    }
    private class onCardClickListener implements  CardView.OnClickListener{
        Subject subject;
        public onCardClickListener(Subject item){
            subject = item;
        }
        @Override
        public void onClick(View v) {
            //support FA
            FragmentActivity activity = (FragmentActivity) v.getContext();
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, PointsViewFragment.getInstance(v.getContext(), subject))
                    .addToBackStack(null)
                    .commit();
        }
    }

    public class ViewHolder extends DragItemAdapter<Subject, ItemAdapter.ViewHolder>.ViewHolder {
        CardView cardView;
        TextView title;
        TextView type;
        TextView points;
        Subject subject;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);

//            cardView = (CardView) itemView.findViewById(R.id.card_view);
            title = (TextView) itemView.findViewById(R.id.title);
            type = (TextView) itemView.findViewById(R.id.subjectType);
            points = (TextView) itemView.findViewById(R.id.points);
        }

//        public TextView mText;
//
//        public ViewHolder(final View itemView) {
//            super(itemView, mGrabHandleId);
//            mText = (TextView) itemView.findViewById(R.id.text);
//        }


        @Override
        public void onItemClicked(View view) {
            if (subject != null){
                FragmentActivity activity = (FragmentActivity) view.getContext();
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_activity_container, PointsViewFragment.getInstance(view.getContext(), subject))
                        .addToBackStack(null)
                        .commit();
            }
            Toast.makeText(view.getContext(), "Item clicked", Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onItemLongClicked(View view) {
            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
