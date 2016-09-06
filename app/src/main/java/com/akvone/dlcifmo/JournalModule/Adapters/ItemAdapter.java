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

import android.app.Activity;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.Constants;
import com.akvone.dlcifmo.EnrollModule.OnFragmentInteractionListener;
import com.akvone.dlcifmo.JournalModule.MySwipeRefreshLayout;
import com.akvone.dlcifmo.JournalModule.PointsViewFragment;
import com.akvone.dlcifmo.JournalModule.Subject;
import com.akvone.dlcifmo.R;

import java.util.ArrayList;

import draglistview.DragItemAdapter;
import draglistview.DragListView;

public class ItemAdapter extends DragItemAdapter<Subject, ItemAdapter.ViewHolder> {

    private int mLayoutId;
    private int mGrabHandleId;
    private Context context;

    public ItemAdapter(ArrayList<Subject> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        super(dragOnLongPress);
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        setHasStableIds(true);
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(mLayoutId, parent, false);
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
//            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.colorClosedSubject));
            holder.title.setTextColor(context.getResources().getColor(R.color.colorClosedSubject));
            holder.points.setTextColor(context.getResources().getColor(R.color.colorClosedSubject));
            holder.type.setTextColor(context.getResources().getColor(R.color.colorClosedSubject));
//                holder.cardView.setBackgroundColor(context.getResources().getColor(R.color.closedSubject, null));
//            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorClosedSubject));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                holder.cardView.setCardBackgroundColor(context.getColor(R.color.colorClosedSubject));
//            }
        }
//        holder.cardView.setOnClickListener(new onCardClickListener(item));
        switch (item.getType()) {
            case Constants.SUBJECT_TYPE_CREDIT:
                holder.type.setText(R.string.subjectTypeCredit);
                break;
            case Constants.SUBJECT_TYPE_EXAM:
                holder.type.setText(R.string.subjectTypeExam);
                break;
            case Constants.SUBJECT_TYPE_COURSE:
                holder.type.setText(R.string.subjectTypeCourse);
                break;
            case Constants.SUBJECT_TYPE_COURSE|Constants.SUBJECT_TYPE_EXAM:
                String ss = context.getString(R.string.subjectTypeExam) + ", "
                        + context.getString(R.string.subjectTypeCourse);
                holder.type.setText(ss);
                break;
            default:
                holder.type.setText(R.string.subjectTypeCredit);
                break;
        }
    }

    @Override
    public long getItemId(int position) {
        return mItemList.get(position).getId();
//        return mItemList.indexOf(mItemList.get(position));
//        ?????
//        return position;
    }

    public class ViewHolder extends DragItemAdapter<Subject, ItemAdapter.ViewHolder>.ViewHolder {
        TextView title;
        TextView type;
        TextView points;
        Subject subject;

        public ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId);

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
//            FrameLayout swipe = (FrameLayout) ((Activity) context).findViewById(R.id.jf_main);
//            View v = View.inflate(context, R.layout.journal_item, swipe);
//            ((TextView) v.findViewById(R.id.title)).setText(((TextView) v.findViewById(R.id.title)).getText());
//            ((TextView) v.findViewById(R.id.subjectType)).setText(((TextView) v.findViewById(R.id.subjectType)).getText());
//            ((TextView) v.findViewById(R.id.points)).setText(((TextView) v.findViewById(R.id.points)).getText());
//            v.setY(view.getY());
//            v.setX(view.getX());
//            TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0
//                , Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, -v.getY());
//            ta.setDuration(2500);
//            ta.setFillAfter(true);
//            ta.setZAdjustment(Animation.ZORDER_TOP);
//            v.startAnimation(ta);
//            DragListView dragListView = (DragListView) ((Activity) context).findViewById(R.id.drag_list_view);
////            dragListView.setVisibility(View.GONE);
//            OnFragmentInteractionListener listener = (OnFragmentInteractionListener) context;
////            listener.changeFragment(PointsViewFragment.getInstance(context, subject));
//            dragListView.setVisibility(View.INVISIBLE);
//            FrameLayout layout = (FrameLayout)  ((Activity) context).findViewById(R.id.pointsContainer);
//            ((AppCompatActivity) context).getSupportFragmentManager()
//                    .beginTransaction()
//                    .add(R.id.pointsContainer, PointsViewFragment.getInstance(context, subject))
//                    .addToBackStack(null)
//                    .commit();
        }

        @Override
        public boolean onItemLongClicked(View view) {
//            Toast.makeText(view.getContext(), "Item long clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
}
