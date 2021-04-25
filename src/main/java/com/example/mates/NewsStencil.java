package com.example.mates;

import java.util.ArrayList;

public class NewsStencil implements Comparable<NewsStencil>{
    public NewsStencil(){

    }
    public NewsStencil(String id, String ownerId, String groupPhotoUrl, String groupName, String date, String text, ArrayList<String> mediaUrl, ArrayList<String> videos, String link){
        this.id = id;
        this.ownerId = ownerId;
        this.groupPhotoUrl = groupPhotoUrl;
        this.groupName = groupName;
        this.date = date;
        this.text = text;
        this.mediaUrl = mediaUrl;
        this.link = link;
        this.videos = videos;
    }
    public String id,
            ownerId,
            groupPhotoUrl,
            groupName,
            date,
            text;
    public ArrayList<String> mediaUrl;
    public ArrayList<String> videos;
    public String link;

    @Override
    public int compareTo(NewsStencil o) {
        if (Integer.parseInt(this.date) >= Integer.parseInt(o.date)) {
            return 1;
        }
        return 0;
    }
}
