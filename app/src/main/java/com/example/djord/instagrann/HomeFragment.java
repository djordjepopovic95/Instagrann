package com.example.djord.instagrann;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private RecyclerView blog_list_view;
    private List<InstaPost> blog_list;
    private InstaRecyclerAdapter instaRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstLoad = true;
    private List<User> user_list;


    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);
        instaRecyclerAdapter = new InstaRecyclerAdapter(blog_list, user_list);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(instaRecyclerAdapter);


        firebaseFirestore = FirebaseFirestore.getInstance();

        blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                if(reachedBottom){
                    loadMorePosts();
                }
            }
        });

        Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {

                    if (isFirstLoad) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        blog_list.clear();
                        user_list.clear();
                    }
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String instaPostId = doc.getDocument().getId();

                            final InstaPost instaPost = doc.getDocument().toObject(InstaPost.class).withId(instaPostId);

                            String blogUserId = doc.getDocument().getString("user_id");

                            firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);

                                        if (isFirstLoad) {
                                            user_list.add(user);
                                            blog_list.add(instaPost);
                                        } else {
                                            blog_list.add(0, instaPost);
                                            user_list.add(0, user);

                                        }
                                        instaRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }
                            });


                        }
                    }
                    isFirstLoad = false;
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePosts(){
        Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(3);

        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String instaPostId = doc.getDocument().getId();
                            String blogUserId = doc.getDocument().getString("user_id");

                            final InstaPost instaPost = doc.getDocument().toObject(InstaPost.class).withId(instaPostId);
                            firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);


                                        user_list.add(user);
                                        blog_list.add(instaPost);

                                        instaRecyclerAdapter.notifyDataSetChanged();

                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

}
