package com.example.djord.instagrann;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity{

    private Toolbar commentToolbar;
    private EditText comment_field;
    private ImageView comment_post_btn;
    private String blog_post_id;
    private RecyclerView comment_list;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentsList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private List<User> user_list;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments);
        commentToolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        comment_field = findViewById(R.id.comment_field);
        comment_post_btn = findViewById(R.id.comment_post_btn);
        comment_list = findViewById(R.id.comment_list);

        blog_post_id = getIntent().getStringExtra("blog_post_id");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();

        commentsList = new ArrayList<>();
        user_list = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentsList, user_list);
        // comment_list.setHasFixedSize(true);
        comment_list.setLayoutManager(new LinearLayoutManager(this));
        comment_list.setAdapter(commentsRecyclerAdapter);
        if (firebaseFirestore != null) {
            firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                    .addSnapshotListener(CommentsActivity.this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                            if (documentSnapshots != null && !documentSnapshots.isEmpty()) {

                                for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                                    if (doc != null && doc.getType() == DocumentChange.Type.ADDED) {

                                        String commentId = doc.getDocument().getId();
                                        final Comments comments = doc.getDocument().toObject(Comments.class);
                                        String user_id = doc.getDocument().getString("user_id");

                                        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task != null && task.isSuccessful()) {
                                                    User user = task.getResult().toObject(User.class);
                                                    user_list.add(user);
                                                    commentsList.add(comments);
                                                    commentsRecyclerAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });

                                    }
                                }

                            }

                        }
                    });
        }

        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentMessage = comment_field.getText().toString().trim();

                if(!commentMessage.isEmpty()){
                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMessage);
                    commentsMap.put("user_id", current_user_id);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());
                    if (firebaseFirestore != null) {
                        firebaseFirestore.collection("Posts").document(blog_post_id).collection("Comments").add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(CommentsActivity.this, "Error: Comment not posted. Try again later.", Toast.LENGTH_SHORT).show();
                                } else {
                                    comment_field.setText("");
                                }
                            }
                        });
                    }
                }
            }
        });

    }
}
