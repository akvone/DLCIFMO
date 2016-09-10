package com.akvone.dlcifmo.MainModule;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.akvone.dlcifmo.MainModule.GetRatingAndMoreTask.OneYearRating;
import com.akvone.dlcifmo.R;

import java.util.ArrayList;

/**
 * Created on 09.09.2016.
 */

//TODO: Изменить "из" и "курс" на английской версии
public class RatingDialog {

    Context context;

    public RatingDialog(Context context){
        this.context = context;
    }

    public void initDialog(){
        RatingListAdapter adapter = new RatingListAdapter(context,R.layout.main_dialog_item,GetRatingAndMoreTask.fullRating);

        ListView listViewItems = new ListView(context);
        listViewItems.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(listViewItems).show();
    }

    private class RatingListAdapter extends ArrayAdapter<OneYearRating>{

        Context context;
        int layoutResourceID;
        ArrayList<OneYearRating> objects;

        public RatingListAdapter(Context context, int resource, ArrayList<OneYearRating> objects) {
            super(context, resource, objects);
            this.context = context;
            this.layoutResourceID = resource;
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            if (convertView == null) {
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                view = inflater.inflate(layoutResourceID, parent ,false);
            } else {
                view = convertView;
            }

            OneYearRating object = objects.get(position);

            ((TextView)view.findViewById(R.id.course_number)).setText(object.courseNumber);
            ((TextView)view.findViewById(R.id.faculty_name)).setText(object.facultyName);
            ((TextView)view.findViewById(R.id.position_in_rating)).setText(object.positionInRating);

            return view;
        }
    }



}
