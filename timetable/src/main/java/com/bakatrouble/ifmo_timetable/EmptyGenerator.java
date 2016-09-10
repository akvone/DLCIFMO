package com.bakatrouble.ifmo_timetable;

import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class EmptyGenerator {
    Generator[] generators;

    EmptyGenerator(final LayoutInflater inflater){
        generators = new Generator[]{
            new Generator(){
                @Override
                public View getView(){
                    CardView sorry = (CardView)inflater.inflate(R.layout.message_card, null);
                    ((TextView)sorry.findViewById(R.id.message_text)).setText(R.string.no_lessons_sorry);
                    return sorry;
                }
            }
        };
    }

    public View generateEmpty(){
        return generators[new Random().nextInt(generators.length)].getView();
    }

    private interface Generator{
        View getView();
    }
}
