package com.example.djord.instagrann;


import android.app.Notification;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    private TextView mNotifData;
    //private Toolbar notificationToolbar;
    //private String blog_post_id;
    private RecyclerView notification_list;
    private NotificationRecyclerAdapter notificationsRecyclerAdapter;
    private List<Comments> notificationsList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private List<User> user_list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        final View view =  inflater.inflate(R.layout.fragment_notification, container, false);

        //notificationToolbar = view.findViewById(R.id.notification_toolbar);
        //setSupportActionBar(notificationToolbar);
        //getSupportActionBar().setTitle("Notifications");


        notification_list = view.findViewById(R.id.notification_list);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        notificationsList = new ArrayList<>();
        user_list = new ArrayList<>();
        notificationsRecyclerAdapter = new NotificationRecyclerAdapter(getActivity(), notificationsList, user_list);
        notification_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        notification_list.setAdapter(notificationsRecyclerAdapter);
        if (firebaseFirestore != null) {
            firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                        if(documentSnapshots != null) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getDocument().get("user_id").equals(current_user_id)) {

                                String postId = doc.getDocument().getId();
                                if (firebaseFirestore != null) {
                                    firebaseFirestore.collection("Posts").document(postId).collection("Comments").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                            if(documentSnapshots != null) {
                                                for (DocumentChange doc1 : documentSnapshots.getDocumentChanges()) {

                                                    final Comments comments = doc1.getDocument().toObject(Comments.class);
                                                    String user_id = doc1.getDocument().getString("user_id");

                                                    firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task != null && task.isSuccessful()) {
                                                                User user = task.getResult().toObject(User.class);
                                                                user_list.add(user);
                                                                notificationsList.add(comments);
                                                                notificationsRecyclerAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }}
                    }
                }
            });
        }

        //String dataMessage = getIntent().getStringExtra("message");
        //String userId = getIntent().getStringExtra("from_id");

        //mNotifData = (TextView) findViewById(R.id.notif_text);
        //mNotifData.setText(userId + ": " + dataMessage);

        return view;
    }

}
