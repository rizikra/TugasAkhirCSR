package com.rizik.training.instamate.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.rizik.training.instamate.CommentActivity;
import com.rizik.training.instamate.Fragment.PostDetailFragment;
import com.rizik.training.instamate.Fragment.ProfileFragment;
import com.rizik.training.instamate.MainActivity;
import com.rizik.training.instamate.Model.Post;
import com.rizik.training.instamate.Model.UserData;
import com.rizik.training.instamate.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder>  {
public Context context;
public List<Post> listPost;
private static final String TAG="PostAdapter";
public static final String ID_UPLOAD = "idUpload";

public PostAdapter(Context context, List<Post> listPost) {
        this.context = context;
        this.listPost = listPost;
}

    private FirebaseUser user;

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_post_item, parent, false);

        return new PostAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = listPost.get(position);
        Glide.with(context).load(post.getGambar())
                .apply(new RequestOptions().placeholder(R.drawable.profile)).into(holder.imageViewPostinganGambar);

        if (post.getDeskripsi().equals("")) {
            holder.textViewDeskripsi.setVisibility(View.GONE);
        } else {
            holder.textViewDeskripsi.setVisibility(View.VISIBLE);
            holder.textViewDeskripsi.setText(post.getDeskripsi());
        }

        infoPengapload(holder.imageViewPp, holder.textViewUsername, holder.textViewPengapload, post.getUploader());
        disukai(post.getIdUpload(), holder.imageViewSuka);
        penyuka(holder.textViewJumlahSuka, post.getIdUpload());
        getComments(post.getIdUpload(), holder.textViewComment);

        holder.imageViewSuka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.imageViewSuka.getTag().equals("sukai")) {
                    final Map<String, Object> data = new HashMap<>();
                    data.put(user.getUid(), true);
                    FirebaseFirestore.getInstance().collection("suka")
                            .document(post.getIdUpload()).set(data, SetOptions.merge());

                    addNotifikasi(post.getUploader(), post.getIdUpload());
                } else {
                    final Map<String, Object> data = new HashMap<>();
                    data.put(user.getUid(), FieldValue.delete());
                    final DocumentReference document = FirebaseFirestore.getInstance().collection("suka")
                            .document(post.getIdUpload());
                    document.update(data);
                }
            }
        });

        holder.imageViewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra(CommentActivity.ID_POST, post.getIdUpload());
                intent.putExtra(CommentAdapter.ID_PUBRISHER, post.getUploader());
                context.startActivity(intent);
            }
        });
        holder.textViewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra(CommentActivity.ID_POST, post.getIdUpload());
                intent.putExtra(CommentAdapter.ID_PUBRISHER, post.getUploader());
                context.startActivity(intent);
            }
        });
        holder.imageViewPp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE).edit();
                editor.putString(MainActivity.KEY, post.getUploader());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new ProfileFragment()).commit();
            }
        });
        holder.textViewUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE).edit();
                editor.putString(MainActivity.KEY, post.getUploader());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new ProfileFragment()).commit();
            }
        });
        holder.textViewPengapload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE).edit();
                editor.putString(MainActivity.KEY, post.getUploader());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new ProfileFragment()).commit();
            }
        });

        holder.imageViewPostinganGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE).edit();
                editor.putString(CommentActivity.ID_POST, post.getUploader());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new PostDetailFragment()).commit();
            }
        });
    }
    @Override
    public int getItemCount() {
        return listPost.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewPp, imageViewPostinganGambar, imageViewSuka, imageViewComment, imageViewSimpan;
        public TextView textViewUsername, textViewJumlahSuka, textViewPengapload, textViewDeskripsi, textViewComment;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewComment = itemView.findViewById(R.id.image_view_comment);
            imageViewPp = itemView.findViewById(R.id.image_view_pp_postingan);
            imageViewPostinganGambar = itemView.findViewById(R.id.image_view_postingan_gambar);
            imageViewSuka = itemView.findViewById(R.id.image_view_suka);
            imageViewSimpan = itemView.findViewById(R.id.image_view_simpan_postingan);
            textViewComment = itemView.findViewById(R.id.text_view_commentar);
            textViewUsername = itemView.findViewById(R.id.text_view_usename_home);
            textViewJumlahSuka = itemView.findViewById(R.id.text_view_jumlah_suka);
            textViewPengapload = itemView.findViewById(R.id.text_view_pengapload);
            textViewDeskripsi = itemView.findViewById(R.id.text_view_deskripsi_postingan);
        }
    }

    private void infoPengapload(final ImageView pp, final TextView username, final TextView pengapload, String idUser) {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(idUser);
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                UserData userData = value.toObject(UserData.class);
                assert userData != null;
                Glide.with(context).load(userData.getImageUrl()).into(pp);
                username.setText(userData.getUsername());
                pengapload.setText(userData.getUsername());
            }
        });
    }

    private void disukai(final String idpost, final ImageView imageViewPostinganGambar) {
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("suka")
                .document(idpost);
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                String id = user.getUid();
                if (value.exists()) {
                    if (value.get(id) != null && value.getBoolean(id)) {
                        imageViewPostinganGambar.setTag("disukai");
                        imageViewPostinganGambar.setImageResource(R.drawable.ic_like_post);
                    } else {
                        imageViewPostinganGambar.setTag("sukai");
                        imageViewPostinganGambar.setImageResource(R.drawable.ic_like);
                    }
                } else {
                    imageViewPostinganGambar.setTag("sukai");
                }

            }
        });
    }

    private void penyuka(final TextView textViewSukai, final String idPost) {
        FirebaseFirestore.getInstance().collection("suka").document(idPost)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        int jumlah = 0;
                        String template = "0 menyukai";
                        if (value.exists()) {
                            jumlah = value.getData().size();
                            template = jumlah + " menyukai";
                        }
                        textViewSukai.setText(template);
                    }
                });
    }
    private void getComments(final String idupload, final TextView komentar) {
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("comments").document(idupload);
        reference.collection(idupload).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int jumlah = 0;
                String template = "0 berkomentar";
                for (DocumentSnapshot snapshot : value) {
                    if (snapshot.exists()) {
                        jumlah++;
                        template = jumlah + " berkomentar";
                    }
                }
                komentar.setText(template);
            }
        });
    }
    private void addNotifikasi(String userid, String idupload) {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("notifikasi")
                .document(userid);
        Map<String, Object> dataNotifikasi = new HashMap<>();
        dataNotifikasi.put("id_user", user.getUid());
        dataNotifikasi.put("text", "menyukai postingan");
        dataNotifikasi.put("idupload", idupload);
        dataNotifikasi.put("ispost", true);
        reference.set(dataNotifikasi);
    }
}