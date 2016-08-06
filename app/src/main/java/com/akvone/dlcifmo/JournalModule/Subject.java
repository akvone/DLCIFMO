package com.akvone.dlcifmo.JournalModule;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private final double LIMIT = 60;
    private String name;
    private int semester;
    private String type;
    private boolean closed = false;
//    private String group;
//    private String studyYear;
    private Marks marks;
    private List<Points> points = new ArrayList<>();
    protected double totalPoints;
    public static List<Subject> subjects = new ArrayList<>();
    public static int CHOSEN_SEMESTER = 4;
    public static int CURRENT_SEMESTER = 4;
    public static boolean isAutumnSemester;
    public static int years = 0; // Заполнится при получении журнала

    public Subject(JSONObject data) throws JSONException {
        name = data.getString("name");
        semester = data.getInt("semester");
        marks = new Marks(data.getJSONArray("marks").getJSONObject(0));
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

        public Points(JSONObject data) throws JSONException{
            max = data.getString("max");
            limit = data.getString("limit");
            value = data.getString("value");
            variable = data.getString("variable");
            if (variable.contains("Семестр")){
                value = value.replace(',','.');
                totalPoints = Double.parseDouble(value);
                if (totalPoints>LIMIT+10){ //TODO: remove +10
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
    }

    private class Marks{
        private String markDate;
        private String mark;

        public Marks(JSONObject data){
            try {
                mark = data.getString("mark");
                markDate = data.getString("markdate");
                type = data.getString("worktype");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public String getName() {
        return name;
    }

    public Double getTotalPoints() {
        return totalPoints;
    }

    public boolean isClosed() {
        return closed;
    }

    public int getSemester() {
        return semester;
    }

    public String getType() {
        return type;
    }

    public List<Points> getPoints() {
        return points;
    }
}
