package com.akvone.dlcifmo.TopStudentsModule;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.akvone.dlcifmo.FragmentWithLoader;
import com.akvone.dlcifmo.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AllCoursesFragment extends FragmentWithLoader {

    private static final String ARG_HTTP_STRING = "httpString";

    ExpandableListView expandableListView;
    AllCoursesExpandableListAdapter adapter;

    public static AllCoursesFragment newInstance(String httpString) {
        AllCoursesFragment fragment = new AllCoursesFragment();
        Bundle args = new Bundle();
        if (httpString!=null) {
            args.putString(ARG_HTTP_STRING, httpString);
        }
        else{
            args.putString(ARG_HTTP_STRING, "http://de.ifmo.ru/index.php?node=8");
        }
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.top_students_all_container, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.rating_facility_progress);
        hidableView = view.findViewById(R.id.expandableListView);
        expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                int departmentID = adapter.getAllFaculties().get(groupPosition).courses
                        .get(childPosition).departmentID;
                int courseNumber = adapter.getAllFaculties().get(groupPosition).courses
                        .get(childPosition).courseNumber;
                String year = adapter.getAllFaculties().get(groupPosition).courses
                        .get(childPosition).years;
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.main_activity_container, CertainCourseFragment.newInstance(departmentID,courseNumber,year));
                ft.addToBackStack(null);
                ft.commit();
                return false;
            }
        });

        adapter = new AllCoursesExpandableListAdapter(new ArrayList<FacultyInformation>());
        expandableListView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        GetAllCoursesTask getAllCoursesTask = new GetAllCoursesTask();
        getAllCoursesTask.execute();
    }

    private class FacultyInformation {
        public String fullname;
        public String abbreviation;
        public ArrayList<CourseInformation> courses = new ArrayList<>();
    }

    private class CourseInformation{
        public int departmentID;
        public int courseNumber;
        public String years;
    }

    private class GetAllCoursesTask extends AsyncTask<Integer, Integer, ArrayList<FacultyInformation>> {
        private int numberOfCoursesOnEachFaculty = 4;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected ArrayList<FacultyInformation> doInBackground(Integer... params) {
            Document doc = null;//Здесь хранится будет разобранный html документ
            String queryString = (String) getArguments().get(ARG_HTTP_STRING);
            try {
                doc = Jsoup.connect(queryString).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements elementsWithATag = doc.select("tr td div table tbody tr td.ptext table tbody tr td a");
            Elements elementsWithFacultyName = doc.select("tr td div table tbody tr td.ptext table tbody tr td");
            String[] facultyNames = elementsWithFacultyName.text()
                    .replace(" ","").replace("курс","").replaceAll("[0-9].","").replaceAll("[ ]{2,}","@")
                    .split("@");
            ArrayList<FacultyInformation> allFaculties = new ArrayList<>();
            for (int i=0, j = 0; j<elementsWithATag.size()/numberOfCoursesOnEachFaculty;j++){
                FacultyInformation facultyInformation = new FacultyInformation();
                facultyInformation.fullname = facultyNames[j];
                ArrayList<CourseInformation> arrayList = new ArrayList<>();

                CourseInformation courseInformation;
                String tagContent;
                Pattern pattern;
                Matcher matcher;
                for (int k = 0;k<numberOfCoursesOnEachFaculty;k++,i++){
                    courseInformation = new CourseInformation();
                    tagContent = elementsWithATag.get(i).attributes().get("href");
                    pattern = Pattern.compile("&depId=([0-9]+)");
                    matcher = pattern.matcher(tagContent);
                    if (matcher.find())
                        courseInformation.departmentID = Integer.parseInt(matcher.group(1));
                    pattern = Pattern.compile("&year_=([0-9]+)");
                    matcher = pattern.matcher(tagContent);
                    if (matcher.find())
                        courseInformation.courseNumber = Integer.parseInt(matcher.group(1));
                    pattern = Pattern.compile("&app_=([0-9]+/[0-9]+)");
                    matcher = pattern.matcher(tagContent);
                    if (matcher.find()) courseInformation.years = matcher.group(1);
                    facultyInformation.courses.add(courseInformation);
                }
                allFaculties.add(facultyInformation);

            }
            Log.d("2", "doInBackground: ");
            return allFaculties;
        }

        @Override
        protected void onPostExecute(ArrayList<FacultyInformation> allFaculties) {
            adapter.changeContent(allFaculties);
            adapter.notifyDataSetChanged();
            super.onPostExecute(allFaculties);
            showProgress(false);
        }
    }

    public class AllCoursesExpandableListAdapter extends BaseExpandableListAdapter {

        private ArrayList<FacultyInformation> allFaculties = new ArrayList<>();

        public ArrayList<FacultyInformation> getAllFaculties() {
            return allFaculties;
        }

        public AllCoursesExpandableListAdapter(ArrayList<FacultyInformation> allFaculties) {
            this.allFaculties = allFaculties;
        }

        public void changeContent(ArrayList<FacultyInformation> allFaculties){
            this.allFaculties = allFaculties;
        }


        @Override
        public Object getChild(int listPosition, int expandedListPosition) {
            return allFaculties.get(listPosition).courses.get(expandedListPosition).courseNumber;
        }

        @Override
        public long getChildId(int listPosition, int expandedListPosition) {
            return expandedListPosition;
        }

        @Override
        public View getChildView(int listPosition, final int expandedListPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final String expandedListText = getChild(listPosition, expandedListPosition).toString();
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.top_students_all_items, null);
            }
            TextView expandedListTextView = (TextView) convertView
                    .findViewById(R.id.expandedListItem);
            expandedListTextView.setText(expandedListText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int listPosition) {
            return allFaculties.get(listPosition).courses.size();
        }

        @Override
        public Object getGroup(int listPosition) {
            return allFaculties.get(listPosition).fullname;
        }

        @Override
        public int getGroupCount() {
            return allFaculties.size();
        }

        @Override
        public long getGroupId(int listPosition) {
            return listPosition;
        }

        @Override
        public View getGroupView(int listPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String listTitle = (String) getGroup(listPosition);
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.top_students_all_group, null);
            }
            TextView listTitleTextView = (TextView) convertView
                    .findViewById(R.id.listTitle);
            listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int listPosition, int expandedListPosition) {
            return true;
        }

    }
}