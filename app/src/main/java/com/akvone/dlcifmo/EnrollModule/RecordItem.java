package com.akvone.dlcifmo.EnrollModule;

import org.jsoup.nodes.Element;

/**
 * Created by 1 on 01.09.2016.
 */
public class RecordItem {
    String num;
    String date;
    String begin;
    String end;
    String pitched;
    String status;
    String id;

    protected RecordItem(Element item){
        int i = 0;
        num = item.child(i++).html();
        date = item.child(i++).html();
        begin = item.child(i++).html();
        end = item.child(i++).html();
        pitched = item.child(i).html();
        status = item.select("font").first().html();
        String onClick = item.select("font").attr("onClick");
        try {
            id = onClick.substring(5 +onClick.indexOf("reid="), onClick.indexOf("&month"));
            Integer.parseInt(id);
        } catch (Exception e) {
            try {
                id = onClick.substring(5 +onClick.indexOf("reid="), onClick.indexOf("reid=") + 11);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
