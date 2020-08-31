package com.rizik.training.instamate.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rizik.training.instamate.Fragment.ProfileFragment;
import com.rizik.training.instamate.MainActivity;
import com.rizik.training.instamate.Model.UserData;
import com.rizik.training.instamate.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    private static final String TAG = "UserAdapter";

    private Context context;
    private List<UserData> users;

    public UserAdapter(Context context, List<UserData> users) {
        this.context = context;
        this.users = users;
    }

    //firebase
    private FirebaseUser user;

    @NonNull
    @Override
    public UserAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_user_item, parent, false);

        return new UserAdapter.MyViewHolder(view);
    }

    private void addNotifikasi(String userid) {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("notifikasi")
                .document(userid);
        Map<String, Object> dataNotifikasi = new HashMap<>();
        dataNotifikasi.put("userId", user.getUid());
        dataNotifikasi.put("text", "Mulai Mengikuti");
        dataNotifikasi.put("idUpload", "");
        dataNotifikasi.put("isPost", false);
        reference.set(dataNotifikasi);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.MyViewHolder holder, int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        final UserData userData = users.get(position);
        holder.buttonFollow.setVisibility(View.VISIBLE);

        holder.textViewUsername.setText(userData.getUsername());
        holder.textViewFullName.setText(userData.getFullname());
        Glide.with(context).load(userData.getImageUrl()).into(holder.imageViewPp);

        isFollowing(userData.getUserId(), holder.buttonFollow, user.getUid());

        if (userData.getUserId().equals(user.getUid())) {
            holder.buttonFollow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = context.getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE).edit();
                editor.putString(MainActivity.KEY, userData.getUserId());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.container_fragment, new ProfileFragment()).commit();
            }
        });

        holder.buttonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.buttonFollow.getText().toString().equals("Ikuti")) {
                    Map<String, Object> dataFollowing = new HashMap<>();
                    dataFollowing.put(userData.getUserId(), true);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("follow").document(user.getUid())
                            .collection("following").document(userData.getUserId()).set(dataFollowing);

                    Map<String, Object> dataFollower = new HashMap<>();
                    dataFollower.put(user.getUid(), true);
                    db.collection("follow").document(userData.getUserId())
                            .collection("followers").document(user.getUid()).set(dataFollowing);

                    addNotifikasi(userData.getUserId());
                } else {

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("follow").document(user.getUid())
                            .collection("following").document(userData.getUserId()).delete();
                    db.collection("follow").document(userData.getUserId())
                            .collection("followers").document(user.getUid()).delete();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewUsername, textViewFullName;
        public CircleImageView imageViewPp;
        public Button buttonFollow;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewUsername = itemView.findViewById(R.id.t_view_username);
            textViewFullName = itemView.findViewById(R.id.t_view_fullnama);
            imageViewPp = itemView.findViewById(R.id.pp);
            buttonFollow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(final String userId, final Button button, final String following) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("follow").document(following).collection("following").document(userId);
        ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null && value.exists()) {
                    button.setText("Berhenti Mengikuti");
                    button.setBackground(context.getResources().getDrawable(R.drawable.custom_unfoll));
                } else {
                    button.setText("Ikuti");
                    button.setBackground(context.getResources().getDrawable(R.drawable.custom_follow));
                }
            }
        });
    }

}
