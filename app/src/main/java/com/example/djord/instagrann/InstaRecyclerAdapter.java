package com.example.djord.instagrann;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.text.format.DateFormat;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class InstaRecyclerAdapter extends RecyclerView.Adapter<InstaRecyclerAdapter.ViewHolder> {

    public List<InstaPost> blog_list;
    public List<User> user_list;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public InstaRecyclerAdapter(List<InstaPost> blog_list, List<User> user_list) {
        this.blog_list = blog_list;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        //holder.setIsRecyclable(false);

        final String instaPostId = blog_list.get(position).instaPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();
        String desc_data = blog_list.get(position).getDesc();
        holder.setDescText(desc_data);
        String image_url = blog_list.get(position).getImage_url();
        String thumb_url = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url, thumb_url);
        final String blog_user_id = blog_list.get(position).getUser_id();

        /*
        if (blog_user_id.equals(currentUserId)) {
            holder.blogDeleteBtn.setEnabled(true);
            holder.blogDeleteBtn.setVisibility(View.VISIBLE);
        }
        */

        String username = user_list.get(position).getName();
        String userImage = user_list.get(position).getImage();

        holder.setUserData(username, userImage);

        long milliseconds = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("dd.MM.yyyy.", new Date(milliseconds)).toString();
        holder.setDate(dateString);

        firebaseFirestore.collection("Posts/").document(instaPostId).collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    int count = documentSnapshots.size();

                    holder.updateLikesCount(count);

                } else {

                    holder.updateLikesCount(0);

                }

            }
        });

        firebaseFirestore.collection("Posts/").document(instaPostId).collection("Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_add));

                } else {

                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));

                }

            }
        });


        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/").document(instaPostId).collection("Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/").document(instaPostId).collection("Likes").document(currentUserId).set(likesMap);

                        } else {
                            firebaseFirestore.collection("Posts/").document(instaPostId).collection("Likes").document(currentUserId).delete();

                        }
                    }
                });

            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", instaPostId);
                context.startActivity(commentIntent);
            }
        });

        holder.blogDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                firebaseFirestore.collection("Posts").document(instaPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        blog_list.remove(position);
                                        user_list.remove(position);
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null);

                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUsername;
        private CircleImageView blogUserImage;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private Button blogDeleteBtn;
        private ImageView blogCommentBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like_button);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_btn);
            blogDeleteBtn = mView.findViewById(R.id.blog_delete_button);
        }

        public void setDescText(String text) {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(text);
        }

        public void setBlogImage(String downloadUri, String thumbUri) {

            blogImageView = mView.findViewById(R.id.blog_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.placeholder);
            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(Glide.with(context).load(thumbUri)).into(blogImageView);
        }

        public void setDate(String date) {
            blogDate = mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }

        public void setUserData(String name, String image) {
            blogUserImage = mView.findViewById(R.id.acc_user_icon);
            blogUsername = mView.findViewById(R.id.blog_username);

            blogUsername.setText(name);
            blogUsername.setTypeface(null, Typeface.BOLD);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.profilekph);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(image).into(blogUserImage);
        }

        public void updateLikesCount(int count) {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            if (count != 1) {
                blogLikeCount.setText(count + " likes");
            } else {
                blogLikeCount.setText(count + " like");
            }
        }
    }
}
