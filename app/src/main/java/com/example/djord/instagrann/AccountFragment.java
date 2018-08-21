package com.example.djord.instagrann;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private RecyclerView acc_list_view;
    private List<InstaPost> acc_list;
    private AccRecyclerAdapter accRecyclerAdapter;
    private DocumentSnapshot lastVisible;
    private Boolean isFirstLoad = true;
    private List<User> user_list;

    private String userId;
    private CircleImageView userImage;
    private Uri mainImageURI = null;
    private android.support.v7.widget.AppCompatTextView username;
    private Bitmap compressedImageFile;
    private Button edit_profile;



    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_account, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        userId = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        edit_profile = view.findViewById(R.id.acc_editprofile_btn);
        userImage = view.findViewById(R.id.acc_user_icon);
        username = view.findViewById(R.id.acc_username);

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(view.getContext(), SetupActivity.class);
                startActivity(settingsIntent);
            }
        });

        if (firebaseFirestore != null) {
            firebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task != null && task.isSuccessful()) {
                        if (task.getResult().exists()) {

                            String name = task.getResult().getString("name");
                            String image = task.getResult().getString("image");

                            mainImageURI = Uri.parse(image);

                            username.setText(name);
                            RequestOptions placeholderRequest = new RequestOptions();
                            placeholderRequest.placeholder(R.drawable.profiledefault);
                            Glide.with(view).setDefaultRequestOptions(placeholderRequest).load(image).into(userImage);
                        }

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(getView().getContext(), "(FIRESTORE Retrieve Error): " + error, Toast.LENGTH_LONG).show();

                    }
                }
            });
        }

        acc_list = new ArrayList<>();
        user_list = new ArrayList<>();
        acc_list_view = view.findViewById(R.id.acc_list_view);
        accRecyclerAdapter = new AccRecyclerAdapter(acc_list, user_list);
        acc_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        acc_list_view.setAdapter(accRecyclerAdapter);



        acc_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                if(reachedBottom){
                    loadMorePosts();
                }
            }
        });

        if (firebaseFirestore != null) {
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (documentSnapshots != null && !documentSnapshots.isEmpty()) {

                        if (isFirstLoad) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            acc_list.clear();
                            user_list.clear();
                        }
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String instaPostId = doc.getDocument().getId();

                                final InstaPost instaPost = doc.getDocument().toObject(InstaPost.class).withId(instaPostId);

                                final String blogUserId = doc.getDocument().getString("user_id");

                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task != null && task.isSuccessful()) {
                                            User user = task.getResult().toObject(User.class);

                                            if (isFirstLoad) {
                                                if (blogUserId.equals(userId)) {
                                                    user_list.add(user);
                                                    acc_list.add(instaPost);
                                                }
                                            } else {
                                                if (blogUserId.equals(userId)) {
                                                    acc_list.add(0, instaPost);
                                                    user_list.add(0, user);
                                                }
                                            }
                                            accRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    }
                                });


                            }
                        }
                        isFirstLoad = false;
                    }
                }
            });
        }
        return view;
    }

    public void loadMorePosts(){
        if (firebaseFirestore != null) {
            Query nextQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        if(documentSnapshots != null) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String instaPostId = doc.getDocument().getId();
                                String blogUserId = doc.getDocument().getString("user_id");

                                final InstaPost instaPost = doc.getDocument().toObject(InstaPost.class).withId(instaPostId);
                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task != null && task.isSuccessful()) {
                                            User user = task.getResult().toObject(User.class);


                                            user_list.add(user);
                                            acc_list.add(instaPost);

                                            accRecyclerAdapter.notifyDataSetChanged();

                                        }
                                    }
                                });
                            }
                        }}
                    }
                }
            });
        }
    }

}
