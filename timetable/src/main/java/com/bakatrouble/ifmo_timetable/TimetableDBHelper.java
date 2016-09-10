package com.bakatrouble.ifmo_timetable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class TimetableDBHelper extends SQLiteOpenHelper {

    static final String INDEX_TABLE = "cache_index";
    static final String CACHE_TABLE = "cache";
    static final String INDEX_ID = "id";
    static final String INDEX_PID = "pid";
    static final String INDEX_TITLE = "title";
    static final String INDEX_TYPE = "`type`";
    static final String CACHE_ID = "id";
    static final String CACHE_INDEX_ID = "index_id";
    static final String CACHE_TITLE = "title";
    static final String CACHE_TIME_BEGIN = "time_begin";
    static final String CACHE_TIME_END = "time_end";
    static final String CACHE_PLACE = "place";
    static final String CACHE_TEACHER = "teacher";
    static final String CACHE_WEEK = "week";
    static final String CACHE_DAY = "day";
    static final String CACHE_JOINED_ID = "jid";
    static final String CACHE_TYPE = "`type`";

    final String CREATE_INDEX_TABLE =
                    "CREATE TABLE "+INDEX_TABLE+"("+
                    INDEX_ID+" INTEGER PRIMARY KEY, "+
                    INDEX_PID+" TEXT, "+
                    INDEX_TYPE+" INTEGER, "+
                    INDEX_TITLE+" TEXT"+
                    ")";
    final String CREATE_CACHE_TABLE =
                    "CREATE TABLE "+CACHE_TABLE+"("+
                    CACHE_ID+" INTEGER PRIMARY KEY, "+
                    CACHE_INDEX_ID+" INTEGER, "+
                    CACHE_TITLE+" TEXT, "+
                    CACHE_TIME_BEGIN+" TEXT, "+
                    CACHE_TIME_END+" TEXT, "+
                    CACHE_PLACE+" TEXT, "+
                    CACHE_TEACHER+" TEXT, "+
                    CACHE_WEEK+" INTEGER, "+
                    CACHE_DAY+" INTEGER, "+
                    CACHE_JOINED_ID+" TEXT, "+
                    CACHE_TYPE+" INTEGER"+
                    ")";

    static final String DB_NAME = "timetable.db";
    Context mContext;

    public TimetableDBHelper(Context context, int dbVer){
        super(context, DB_NAME, null, dbVer);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_INDEX_TABLE);
        db.execSQL(CREATE_CACHE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 4){
            mContext.getSharedPreferences(TimetableActivity.PREFS_FILE, 2).edit().clear().apply();
            db.execSQL("DROP TABLE IF EXISTS "+INDEX_TABLE);
            db.execSQL("DROP TABLE IF EXISTS "+CACHE_TABLE);
            onCreate(db);
        }
        if(oldVersion < 6){
            db.execSQL("ALTER TABLE "+CACHE_TABLE+" ADD COLUMN "+CACHE_JOINED_ID+" TEXT");
        }
        if(oldVersion < 6){
            db.execSQL("ALTER TABLE "+CACHE_TABLE+" ADD COLUMN "+CACHE_TYPE+" INTEGER");
        }
    }

    public long getIdByPid(String pid){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"id"};
        String[] selectionArgs = {pid};
        Cursor c = db.query(INDEX_TABLE, columns, INDEX_PID + " = ?", selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
            c.close();
        }
        return -1;
    }

    public String getPidByid(long id){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"pid"};
        String[] selectionArgs = {id+""};
        Cursor c = db.query(INDEX_TABLE, columns, INDEX_ID + " = ?", selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
            c.close();
        }
        return null;
    }

    public SearchSuggestion.Type getTypeByid(long id){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"type"};
        String[] selectionArgs = {id+""};
        Cursor c = db.query(INDEX_TABLE, columns, INDEX_ID + " = ?", selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getInt(0) == 1 ? SearchSuggestion.Type.Group : SearchSuggestion.Type.Teacher;
            }
            c.close();
        }
        return null;
    }

    public ArrayList<Subject> getSubjects(int week, int day, long id){
        ArrayList<Subject> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {CACHE_TITLE, CACHE_TEACHER, CACHE_TIME_BEGIN, CACHE_TIME_END, CACHE_PLACE, CACHE_JOINED_ID, CACHE_TYPE};
        String selection = CACHE_INDEX_ID+" = ? and "+CACHE_DAY+" = ? and ("+CACHE_WEEK+" = ? or "+CACHE_WEEK+" = 0)";
        String[] selectionArgs = {id+"", day+"", week+""};
        Cursor c = db.query(CACHE_TABLE, columns, selection, selectionArgs, null, null, CACHE_ID);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    if(out.size() > 0 && out.get(out.size()-1).time_begin.equals(c.getString(2))){
                        out.get(out.size()-1).addons.add(new Subject(c.getString(0), c.getString(1), c.getString(2), c.getString(3),
                                c.getString(4), c.getString(5), c.getInt(6) == 1 ? SearchSuggestion.Type.Group : SearchSuggestion.Type.Teacher));
                    }else{
                        out.add(new Subject(c.getString(0), c.getString(1), c.getString(2), c.getString(3),
                                c.getString(4), c.getString(5), c.getInt(6) == 1 ? SearchSuggestion.Type.Group : SearchSuggestion.Type.Teacher));
                    }
                } while (c.moveToNext());
            }
            c.close();
        }
        return out;
    }

    public long addGroup(String pid, SearchSuggestion.Type type, String title){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(INDEX_PID, pid);
        cv.put(INDEX_TYPE, type == SearchSuggestion.Type.Group ? 1 : 2);
        cv.put(INDEX_TITLE, title);
        return db.insert(INDEX_TABLE, "null", cv);
    }

    public long addSubject(Subject subject){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(CACHE_INDEX_ID, subject.id);
        cv.put(CACHE_DAY, subject.day);
        cv.put(CACHE_WEEK, subject.week);
        cv.put(CACHE_TITLE, subject.title);
        cv.put(CACHE_TEACHER, subject.teacher);
        cv.put(CACHE_TIME_BEGIN, subject.time_begin);
        cv.put(CACHE_TIME_END, subject.time_end);
        cv.put(CACHE_PLACE, subject.place);
        cv.put(CACHE_JOINED_ID, subject.jid);
        cv.put(CACHE_TYPE, subject.type == SearchSuggestion.Type.Group ? 1 : 2);
        return db.insert(CACHE_TABLE, "null", cv);
    }

    public ArrayList<SearchSuggestion> searchInCache(String query){
        ArrayList<SearchSuggestion> out = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {INDEX_ID, INDEX_PID, INDEX_TITLE, INDEX_TYPE};
        String selection = INDEX_TITLE+" LIKE ?";
        String[] selectionArgs = {query+"%"};
        Cursor c = db.query(INDEX_TABLE, columns, selection, selectionArgs, null, null, CACHE_ID);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    out.add(new SearchSuggestion(SearchSuggestion.Source.Cached, c.getLong(0), c.getString(1), c.getString(2),
                            c.getInt(3) == 1 ? SearchSuggestion.Type.Group : SearchSuggestion.Type.Teacher));
                } while (c.moveToNext());
            }
            c.close();
        }
        return out;
    }

    public void removeFromCache(long internal_id, boolean index){
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {internal_id+""};
        if(index)
            db.delete(INDEX_TABLE, INDEX_ID+" = ?", whereArgs);
        db.delete(CACHE_TABLE, CACHE_INDEX_ID+" = ?", whereArgs);
    }

    public String getTitleByInternalId(long internal_id){
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"title"};
        String[] selectionArgs = {internal_id+""};
        Cursor c = db.query(INDEX_TABLE, columns, INDEX_ID + " = ?", selectionArgs, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
            c.close();
        }
        return null;
    }
}
