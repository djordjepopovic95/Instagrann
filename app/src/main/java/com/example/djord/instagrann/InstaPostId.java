package com.example.djord.instagrann;

import android.support.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class InstaPostId {

    @Exclude
    public String instaPostId;

    public <T extends InstaPostId> T withId(@NonNull final String id){
        this.instaPostId = id;
        return (T) this;
    }
}
