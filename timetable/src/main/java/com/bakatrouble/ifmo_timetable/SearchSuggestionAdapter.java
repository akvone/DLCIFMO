package com.bakatrouble.ifmo_timetable;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bakatrouble on 31.08.2015.
 */
public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder>  {
    public ArrayList<SearchSuggestion> mDataset;

    public SearchSuggestionAdapter() {
        mDataset = new ArrayList<>();
    }

    public void removeItem(int index){
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    public void clearDataset(){
        int length = mDataset.size();
        mDataset.clear();
        notifyItemRangeRemoved(0, length);
    }

    public void appendToDataset(ArrayList<SearchSuggestion> newDataset) {
        int length = mDataset.size();
        mDataset.addAll(newDataset);
        notifyItemRangeInserted(length, mDataset.size() - length);
    }

    public void updateDataset(ArrayList<SearchSuggestion> newDataset, boolean animate){
        int length = mDataset.size();
        mDataset.clear();
        mDataset.addAll(newDataset);
        if(animate){
            if(mDataset.size() > length){
                notifyItemRangeChanged(0, length);
                notifyItemRangeInserted(length, mDataset.size() - length);
            }else{
                notifyItemRangeChanged(0, mDataset.size());
                notifyItemRangeRemoved(mDataset.size(), length - mDataset.size());
            }
        }else{
            notifyDataSetChanged();
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchSuggestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.suggestion_row, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        viewHolder.suggestion_title.setText(mDataset.get(position).title);
        viewHolder.suggestion_icon.setImageDrawable(
                ContextCompat.getDrawable(viewHolder.suggestion_icon.getContext(),
                        (mDataset.get(position).source == SearchSuggestion.Source.Cached
                                ? R.drawable.ic_storage_white_24dp
                                : R.drawable.ic_search_white_24dp
                        )));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView suggestion_icon;
        public TextView suggestion_title;
        RelativeLayout container;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            suggestion_title = (TextView)itemLayoutView.findViewById(R.id.suggestion_title);
            suggestion_icon = (ImageView)itemLayoutView.findViewById(R.id.suggestion_icon);
            container = (RelativeLayout)itemLayoutView.findViewById(R.id.suggestion_row_container);
        }
    }
}
