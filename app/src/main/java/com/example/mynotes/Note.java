package com.example.mynotes;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class Note {
    @DocumentId
    private String id;
    private String title;
    private String content;
    private Timestamp timestamp;
    private String userId;

    // Required empty constructor for Firestore
    public Note() {
    }

    public Note(String title, String content, String userId) {
        this.title = title;
        this.content = content;
        this.timestamp = Timestamp.now();
        this.userId = userId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return timestamp != null ? timestamp.toDate() : new Date();
    }
}