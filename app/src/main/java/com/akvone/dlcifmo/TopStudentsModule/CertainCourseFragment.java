package com.akvone.dlcifmo.TopStudentsModule;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.akvone.dlcifmo.FragmentWithLoader;
import com.akvone.dlcifmo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created on 01.08.2016.
 */
public class CertainCourseFragment extends FragmentWithLoader {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DEPARTMENT_ID = "depId";
    private static final String ARG_COURSE = "year";
    private static final String ARG_YEAR = "app";

    RecyclerView recyclerView;
    CertainCourseAdapter adapter;

    public static CertainCourseFragment newInstance(int departmentID, int course, String year) {
        CertainCourseFragment fragment = new CertainCourseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DEPARTMENT_ID, departmentID);
        args.putInt(ARG_COURSE, course);
        args.putString(ARG_YEAR, year);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.top_students_certain_container, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.rating_facility_progress);
        hidableView = view.findViewById(R.id.standard_cards);
        recyclerView = (RecyclerView) hidableView;

        adapter = new CertainCourseAdapter(new ArrayList<StudentInformation>());
        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        GetCertainCourseTask getCertainCourseTask = new GetCertainCourseTask();
        getCertainCourseTask.execute();
    }

    private class StudentInformation {
        public int ratingNumber;
        public String fullName;
        public String additionalInfo;
    }

    private class GetCertainCourseTask extends AsyncTask<Integer, Integer, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected String[] doInBackground(Integer... params) {
            Document doc = null;//Здесь хранится будет разобранный html документ
            String queryString = "depId="+(Integer)getArguments().get(ARG_DEPARTMENT_ID)
                    + "&year_="+(Integer) getArguments().get(ARG_COURSE)
                    + "&app_="+(String)getArguments().get(ARG_YEAR);
            try {
                doc = Jsoup.connect("http://de.ifmo.ru/index.php?doc_open=-tops.php&view=topStudent&"+queryString).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elements = doc.select("tr td div table tbody tr td.ptext table tbody tr td");
            String[] firstProcessed = elements.text().trim().split("    ");
            return firstProcessed;
        }

        @Override
        protected void onPostExecute(String[] processedString) {
            super.onPostExecute(processedString);

            for (int i=0;i<processedString.length;i++){
                processedString[i] = processedString[i].replace(" ","");
            }
            ArrayList<StudentInformation> arrayList = new ArrayList<>();
            for (String s : processedString) {
                StudentInformation pir = new StudentInformation();

                String[] ss = s.split(",");
                try {
                    pir.fullName=ss[0];
                    pir.additionalInfo=ss[1]+ss[2];
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    pir.fullName="-";
                    pir.additionalInfo="-";
                }
                arrayList.add(pir);
            }
            adapter.changeContent(arrayList);
            adapter.notifyDataSetChanged();
            showProgress(false);
        }
    }

    private class CertainCourseAdapter extends RecyclerView.Adapter<CertainCourseAdapter.ViewHolder> {

        private ArrayList<StudentInformation> peopleRatings = new ArrayList<>();

        // Pass in the contact array into the constructor
        public CertainCourseAdapter(ArrayList<StudentInformation> ratings) {
            peopleRatings = ratings;
        }

        public void changeContent(ArrayList<StudentInformation> ratings){
            peopleRatings = ratings;
        }

        // Provide a direct reference to each of the views within a data item
        // Used to cache the views within the item layout for fast access
        public class ViewHolder extends RecyclerView.ViewHolder {
            // Your holder should contain a member variable
            // for any view that will be set as you render a row
            public TextView fullNameTextView;
            public TextView additionalInfoTextView;

            // We also create a constructor that accepts the entire item row
            // and does the view lookups to find each subview
            public ViewHolder(View itemView) {
                // Stores the itemView in a public final member variable that can be used
                // to access the context from any ViewHolder instance.
                super(itemView);
                fullNameTextView = (TextView) itemView.findViewById(R.id.all_name);
                additionalInfoTextView = (TextView) itemView.findViewById(R.id.additional_info);
            }
        }

        // Usually involves inflating a layout from XML and returning the holder
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.top_students_certain_item, parent, false);

            // Return a new holder instance
            ViewHolder viewHolder = new ViewHolder(contactView);
            return viewHolder;
        }

        // Involves populating data into the item through holder
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            // Get the data model based on position
            // Set item views based on your views and data model
            viewHolder.fullNameTextView.setText(peopleRatings.get(position).fullName);
            viewHolder.additionalInfoTextView.setText(peopleRatings.get(position).additionalInfo);
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return peopleRatings.size();
        }
    }
}
