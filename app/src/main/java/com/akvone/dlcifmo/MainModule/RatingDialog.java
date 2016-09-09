package com.akvone.dlcifmo.MainModule;

import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

/**
 * Created on 09.09.2016.
 */
public class RatingDialog {

    public void initDialog(Context context){
//        ListAdapter
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Важное сообщение!")
                .setMessage("Покормите кота!")
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
    }

    private class RatingListAdapter implements ListAdapter{

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }



}
