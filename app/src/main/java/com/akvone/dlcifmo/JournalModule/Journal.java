package com.akvone.dlcifmo.JournalModule;

import android.content.pm.ProviderInfo;
import android.util.Log;

import com.akvone.dlcifmo.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Journal {

    public static int chosenYear;
    public static int chosenSemester;
    public static boolean isAutumnSemester;
    public static JSONObject JSONJournal;
    private static int currentId;

    public static Journal instance;
    public static Journal getInstance() {
        return instance;
    }
    public static Journal newInstance(JSONObject object){
        Log.d("Journal", "begin creation");
        try {
            instance = new Journal(object);
            JournalFragment.journal  = object;
        } catch (JSONException e) {
            Log.d("Journal", "creation fail");
            e.printStackTrace();
        }
        Log.d("Journal", "end creation");
        return instance;
    }
    public static void delete(){
        JournalFragment.doNotSaveJournal = true;
        instance = null;
    }

    private ArrayList<Year> years;

    private Journal(JSONObject object) throws JSONException{
        currentId = 0;
        JSONJournal = object;
        JSONArray yrs = object.getJSONArray("years");
        years = new ArrayList<>(yrs.length());
        for (int i = 0; i < yrs.length(); i++) {
            years.add(new Year(yrs.getJSONObject(i)));
        }
        Calendar calendar = new GregorianCalendar();
        isAutumnSemester = calendar.get(Calendar.MONTH) > Calendar.JUNE;
        chosenYear = yrs.length()-1;
        chosenSemester = isAutumnSemester ? 0 : 1;
    }

    public void update(JSONObject data) throws JSONException {
        int id = 0;
        JSONArray years = data.getJSONArray("years");
        for (int i = 0; i < years.length(); i++) {
            JSONArray subs = years.getJSONObject(i).getJSONArray("subjects");
            for (int j = 0; j < subs.length(); j++) {
                if (id == 45){
                    System.out.printf("");
                }
                JSONObject subject = subs.getJSONObject(j);
                Subject s = new Subject(subject, id, 0);
                s.weight = getSubject(id).weight;
                setSubject(id++, s);

            }
        }
    }

    public class Year{
        String group;
        String year;
        ArrayList<Semester> semesters;
        private int lastId;

        Year(JSONObject object) throws JSONException{
            semesters = new ArrayList<>(2);
            year = object.getString("studyyear");
            group = object.getString("group");
            JSONArray subjects = object.getJSONArray("subjects");
            JSONObject s = subjects.getJSONObject(0);
            int se = Integer.parseInt(s.getString("semester"));
            se += se % 2;
            semesters.add(new Semester(se-1));
            semesters.add(new Semester(se));
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subject = subjects.getJSONObject(i);
                String sem = subject.getString("semester");
                int semester;
                try {
                    semester = Integer.parseInt(sem);
                } catch (NumberFormatException e) {
                    semester = 0;
                }
                int k = (semester % 2 == 1) ? 0 : 1;
                semesters.get(k).number = sem;
                semesters.get(k).subjects.add(new Subject(subject, currentId++, semesters.get(k).subjects.size()));
            }
            lastId = currentId-1;
        }

        public class Semester {
            String number;
            ArrayList<Subject> subjects;

            Semester (JSONObject object){
                subjects = new ArrayList<>();

            }
            Semester (int num){
                number = num+"";
                subjects = new ArrayList<>();
            }

            public String getNumber() {
                return number;
            }
        }
        public ArrayList<Subject> getSemester(int i){
            return semesters.get(i).subjects;
        }

        public String getName() {
            return year;
        }

        public ArrayList<Semester> getSemesters() {
            return semesters;
        }
        public ArrayList<Subject> getSubjects(){
            ArrayList<Subject> subs = new ArrayList<>();
            subs.addAll(getSemester(0));
            subs.addAll(getSemester(1));
            return  subs;
        }
    }

    public Year getYear(int i) {
        return years.get(i);
    }

    public ArrayList<Year> getYears() {
        return years;
    }
    public int countYears(){
        return years.size();
    }
    public Subject getSubject(int id){
        int i = 0;
        Year y;
        do {
            y = years.get(i++);
        } while (id > y.lastId);
        i = 0;

        for (Subject s :
                y.getSubjects()) {
            if (id == s.id){
                return s;
            }
        }
        return null;
    }
    public void setSubject(int id, Subject sub){
        int i = 0;
        Year y;
        do {
            y = years.get(i++);
        } while (id > y.lastId);
        i = 0;

        for (Subject s :
                y.getSubjects()) {
            if (id == s.id){
                s = sub;
                return;
            }
        }
    }
    public void setSemesterItems(ArrayList<Subject> data){
        years.get(chosenYear).semesters.get(chosenSemester).subjects = data;
    }
}
