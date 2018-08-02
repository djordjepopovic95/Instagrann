package com.example.djord.instagrann;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public List<User> userList;
    public Context context;

    public CommentsRecyclerAdapter(List<Comments> commentsList, List<User> userList){

        this.commentsList = commentsList;
        this.userList = userList;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, int position) {

        //  holder.setIsRecyclable(false);

        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

        String username = userList.get(position).getName();
        String userImage = userList.get(position).getImage();

        holder.setUserData(username, userImage);

    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {
            System.out.println(commentsList.size());
            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private ImageView comment_image;
        private TextView comment_username;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setUserData(String name, String image){
            comment_image = mView.findViewById(R.id.comment_image);
            comment_username = mView.findViewById(R.id.comment_username);

            comment_username.setText(name);
            comment_username.setTypeface(null, Typeface.BOLD);

            RequestOptions placeholderOptions = new RequestOptions();
            placeholderOptions.placeholder(R.drawable.profilekph);
            Glide.with(context).applyDefaultRequestOptions(placeholderOptions).load(image).into(comment_image);
        }
    }

}
