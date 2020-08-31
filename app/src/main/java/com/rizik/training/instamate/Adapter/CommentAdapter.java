package com.rizik.training.instamate.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rizik.training.instamate.MainActivity;
import com.rizik.training.instamate.Model.Comment;
import com.rizik.training.instamate.Model.UserData;
import com.rizik.training.instamate.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    private static final String TAG = "CommentAdapter";

    private Context context;
    private List<Comment> comments;
    public static final String ID_PUBRISHER = "idpengapload";

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    private FirebaseUser user;

    @NonNull
    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_comment_item, parent, false);

        return new CommentAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.MyViewHolder holder, int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = comments.get(position);

        holder.textViewText.setText(comment.getText());
        getUserInfo(holder.imageViewPp, holder.textViewMrComment, comment.getMrComment());

        holder.textViewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(ID_PUBRISHER, comment.getMrComment());
                context.startActivity(intent);
            }
        });
        holder.imageViewPp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(ID_PUBRISHER, comment.getMrComment());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewPp;
        public TextView textViewMrComment, textViewText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPp = itemView.findViewById(R.id.image_view_pp_item_comment);
            textViewMrComment = itemView.findViewById(R.id.text_view_username_commentar_item);
            textViewText = itemView.findViewById(R.id.text_view_item_commentar);

        }
    }

    private void getUserInfo(final ImageView imageViewPp, final TextView mrComment, String pengapload) {
        FirebaseFirestore.getInstance().collection("users").document(pengapload)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        UserData userData = value.toObject(UserData.class);
                        Glide.with(context).load(userData.getImageUrl()).into(imageViewPp);
                        mrComment.setText(userData.getUsername());
                    }
                });
    }
}
