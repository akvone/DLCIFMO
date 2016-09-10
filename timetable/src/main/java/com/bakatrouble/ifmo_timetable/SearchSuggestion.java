package com.bakatrouble.ifmo_timetable;

public class SearchSuggestion {
    enum Source{
        Cached,
        Found
    }

    enum Type{
        Teacher,
        Group
    }

    public Source source;
    public String title;
    public String id;
    public Type type;
    public long internal_id;

    public SearchSuggestion(Source src, long id, String pid, String title, Type type){
        this.source = src;
        this.internal_id = id;
        this.id = pid;
        this.title = title;
        this.type = type;
    }

    public SearchSuggestion(Source src, String id, String title, Type type){
        this.source = src;
        this.id = id;
        this.title = title;
        this.type = type;
    }
}
