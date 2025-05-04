# Note.java - Code Explanation

## File Path
`app/src/main/java/com/example/mynotes/Note.java`

## Purpose
This class represents the data model for a note in the application. It defines the structure of a note and provides methods to access and modify its properties.

## Code Explanation

```java
package com.example.mynotes;

import com.google.firebase.Timestamp;

public class Note {
    private String id;
    private String title;
    private String content;
    private String userId;
    private Timestamp timestamp;

    // Empty constructor required for Firestore
    public Note() {
    }

    public Note(String title, String content, String userId, Timestamp timestamp) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.timestamp = timestamp;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}