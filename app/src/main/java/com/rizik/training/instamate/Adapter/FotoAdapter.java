package com.rizik.training.instamate.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.rizik.training.instamate.Fragment.PostDetailFragment;
import com.rizik.training.instamate.MainActivity;
import com.rizik.training.instamate.Model.Post;
import com.rizik.training.instamate.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.MyViewHolder> {
    private static final String TAG = "FotoAdapter";

    public FotoAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    private Context context;
    private List<Post> postList;

    @NonNull
    @Override
    public FotoAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_foto_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoAdapter.MyViewHolder holder, int position) {
        final Post postingan = postList.get(position);
        Glide.with(context).load(postingan.getGambar()).into(holder.imageViewPostFoto);

        holder.imageViewPostFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE).edit();
                editor.putString(CommentActivity.ID_POST, postingan.getIdUpload());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new PostDetailFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewPostFoto;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewPostFoto = itemView.findViewById(R.id.image_view_post);
        }
    }
}

