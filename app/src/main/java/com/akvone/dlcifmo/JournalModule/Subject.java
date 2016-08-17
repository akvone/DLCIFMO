package com.akvone.dlcifmo.JournalModule;

import android.util.Log;

import com.akvone.dlcifmo.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by 1 on 16.08.2016.
 */
public class Subject {
    public static final double LIMIT = 59.9999;
    String name;
    int type;
    double totalPoints;
    boolean closed = false;
    ArrayList<Points> points;
    ArrayList<Mark> marks;
    int id;
    int weight;

    Subject(JSONObject data, int id, int wght)throws JSONException {
        try {
            this.id = data.getInt("id");
            weight = data.getInt("weight");
        } catch (JSONException  e)
        {
            Log.d("Subject", "no saved id");
            data.put("id", id);
            data.put("weight", wght);
            this.id = id;
            weight = wght;
        }
        points = new ArrayList<>();
        marks = new ArrayList<>();
        name = data.getString("name");
        for (int i = 0; i < data.getJSONArray("marks").length(); i++){
            //Похоже, что курсовая и экзамен могут идти по одному предмету отдельными оценками
            marks.add(new Mark(data.getJSONArray("marks").getJSONObject(i)));
        }
        try {
            JSONArray jsonPoints = data.getJSONArray("points");
            for (int i = 0; i < jsonPoints.length(); i++)
            {
                points.add(new Points(jsonPoints.getJSONObject(i)));
            }
        } catch (JSONException e) {
            totalPoints = 0;
        }
    }
    public class Points {
        private String max;
        private String limit;
        private String value;
        private String variable;

        public Points(JSONObject data) throws JSONException {
            max = data.getString("max");
            limit = data.getString("limit");
            value = data.getString("value");
            variable = data.getString("variable");
            if (variable.contains("Семестр")){
                value = value.replace(',','.');
                totalPoints = Double.parseDouble(value);
                if (totalPoints>LIMIT){
                    closed = true;
                }
            }


        }

        public String getMax() {
            return max;
        }

        public String getVariable() {
            return variable;
        }

        public String getValue() {
            return value;
        }

        public String getLimit() {
            return limit;
        }
    }

    public class Mark {
        private String markDate;
        private String mark;
        private String tp;
        public Mark(JSONObject data){
            try {
                mark = data.getString("mark");
                markDate = data.getString("markdate");
                tp = data.getString("worktype");
                switch (tp){
                    case "Зачет":
                        type |= Constants.SUBJECT_TYPE_CREDIT;
                        break;
                    case "Экзамен":
                        type |= Constants.SUBJECT_TYPE_EXAM;
                        break;
                    default: //TODO get course string
                        type |= Constants.SUBJECT_TYPE_COURSE;
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String getMarkDate() {
            return markDate;
        }

        public String getTp() {
            return tp;
        }

        public String getMark() {
            return mark;
        }
    }


    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public boolean isClosed() {
        return closed;
    }

    public ArrayList<Points> getPoints() {
        return points;
    }

    public ArrayList<Mark> getMarks() {
        return marks;
    }

    public int getId() {
        return id;
    }
}