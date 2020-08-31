package com.rizik.training.instamate.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rizik.training.instamate.Adapter.FotoAdapter;
import com.rizik.training.instamate.EditProfileActivity;
import com.rizik.training.instamate.LoginActivity;
import com.rizik.training.instamate.MainActivity;
import com.rizik.training.instamate.Model.Post;
import com.rizik.training.instamate.Model.UserData;
import com.rizik.training.instamate.PhotoProfileActivity;
import com.rizik.training.instamate.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private ImageView imageViewPp, imageViewPengaturan;
    private TextView textViewPostingan, textViewPengikut, textViewMengikuti, textViewBiodata, textViewFullname, textViewUsername;
    private Button buttonEditProfile;
    private ImageButton imageButtonSimpanGambar, imageButtonGambarSaya;
    private GoogleSignInClient client;
    private RecyclerView recyclerView;
    private List<Post> postList;
    private FotoAdapter fotoAdapter;

    private FirebaseUser user;
    private String profileId;
    public static final String KEY_IMAGE = "idimage";
    public static final String USERNAME = "username";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        init(view);

        SharedPreferences preferences = Objects.requireNonNull(getContext()).getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE);
        profileId = preferences.getString(MainActivity.KEY, "none");

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        fotoAdapter = new FotoAdapter(getContext(), postList);
        recyclerView.setAdapter(fotoAdapter);

        getUserInfo();
        getDataFollowers();
        getNrPostingan();
        foto();

        if (profileId.equals(user.getUid())) {
            buttonEditProfile.setText("Edit Profile");
        } else {
            cekFollow();
            imageButtonSimpanGambar.setVisibility(View.GONE);
        }

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textButton = buttonEditProfile.getText().toString();
                if (textButton.equals("Edit Profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (textButton.equals("ikuti")) {
                    Map<String, Object> dataFollowing = new HashMap<>();
                    dataFollowing.put(profileId, true);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("follow").document(user.getUid())
                            .collection("following").document(profileId).set(dataFollowing);
                    Map<String, Object> dataFollower = new HashMap<>();
                    dataFollower.put(user.getUid(), true);
                    db.collection("follow").document(profileId)
                            .collection("followers").document(user.getUid()).set(dataFollowing);
                    addNotifikasi();
                } else if (textButton.equals("berhenti mengikuti")) {
                    Map<String, Object> dataFollowing = new HashMap<>();
                    dataFollowing.put(profileId, true);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("follow").document(user.getUid())
                            .collection("following").document(profileId).delete();
                    Map<String, Object> dataFollower = new HashMap<>();
                    dataFollower.put(user.getUid(), true);
                    db.collection("follow").document(profileId)
                            .collection("followers").document(user.getUid()).delete();
                }
            }
        });

        imageViewPengaturan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                alert.setTitle("Woro Woro :D");
                alert.setCancelable(false);
                alert.setMessage("Yakin ingin logout?");
                alert.setPositiveButton("logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout();
                        Toast.makeText(getContext(), "logout", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("gajadidah", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alert.create().show();
            }
        });

        imageViewPp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users")
                        .document(profileId);
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (getContext() == null) {
                            return;
                        }

                        UserData userData = value.toObject(UserData.class);
                        Intent intent = new Intent(getContext(), PhotoProfileActivity.class);
                        intent.putExtra(KEY_IMAGE, userData.getImageUrl());
                        intent.putExtra(USERNAME, userData.getUsername());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        client = GoogleSignIn.getClient(this.getContext(), gso);
        return view;
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        imageViewPp = view.findViewById(R.id.image_view_pp_profile);
        imageViewPengaturan = view.findViewById(R.id.image_view_pengaturan);
        textViewPostingan = view.findViewById(R.id.post);
        textViewBiodata = view.findViewById(R.id.bio_profile);
        textViewPengikut = view.findViewById(R.id.followers);
        textViewMengikuti = view.findViewById(R.id.following);
        textViewFullname = view.findViewById(R.id.text_view_fullname);
        textViewUsername = view.findViewById(R.id.t_view_username_profile);
        buttonEditProfile = view.findViewById(R.id.btn_edit_profile);
        imageButtonSimpanGambar = view.findViewById(R.id.postingan_tersimpan);
        imageButtonGambarSaya = view.findViewById(R.id.postingan_koleksi);
        recyclerView = view.findViewById(R.id.recycler_view_postingan);
    }
    private void getUserInfo() {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("users")
                .document(profileId);
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (getContext() == null) {
                    return;
                }
                assert value != null;
                UserData userData = value.toObject(UserData.class);
                assert userData != null;
                Glide.with(getContext()).load(userData.getImageUrl()).into(imageViewPp);
                textViewUsername.setText(userData.getUsername());
                textViewFullname.setText(userData.getFullname());
                textViewBiodata.setText(userData.getBio());
            }
        });
    }

    private void cekFollow() {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("follow")
                .document(user.getUid()).collection("following");

        collectionReference.document(profileId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                assert value != null;
                if (value.exists()) {
                    buttonEditProfile.setText("berhenti mengikuti");
                    buttonEditProfile.setBackground(getResources().getDrawable(R.drawable.custom_unfoll));
                } else {
                    buttonEditProfile.setBackground(getResources().getDrawable(R.drawable.custom_follow));
                    buttonEditProfile.setText("ikuti");

                }
            }
        });
    }

    private void getDataFollowers() {
        CollectionReference collection1 = FirebaseFirestore.getInstance().collection("follow")
                .document(profileId).collection("followers");

        collection1.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<String> jumlah = new ArrayList<>();
                assert value != null;
                for (DocumentSnapshot snapshot : value) {
                    jumlah.add(value.getDocumentChanges().toString());
                    Log.d(TAG, "onEvent: getDataFollowers" + snapshot);
                }

                textViewPengikut.setText(String.valueOf(jumlah.size()));
            }
        });

        CollectionReference collection2 = FirebaseFirestore.getInstance().collection("follow")
                .document(profileId).collection("following");

        collection2.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<String> jumlah = new ArrayList<>();
                assert value != null;
                for (DocumentSnapshot snapshot : value) {
                    jumlah.add(value.getDocumentChanges().toString());
                    Log.d(TAG, "onEvent: getDataFollowers" + snapshot);
                }

                textViewMengikuti.setText(String.valueOf(jumlah.size()));
            }
        });
    }

    private void getNrPostingan() {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("photos");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                int i = 0;
                assert value != null;
                for (DocumentSnapshot snapshot : value) {
                    Post post = snapshot.toObject(Post.class);
                    assert post != null;
                    if (post.getUploader().equals(profileId)) {
                        i++;
                    }
                }
                textViewPostingan.setText(String.valueOf(i));
            }
        });
    }

    private void addNotifikasi() {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("notifikasi")
                .document(profileId);
        Map<String, Object> dataNotifikasi = new HashMap<>();
        dataNotifikasi.put("id_user", user.getUid());
        dataNotifikasi.put("text", "muai mengikuti anda");
        dataNotifikasi.put("idupload", "");
        dataNotifikasi.put("ispost", false);
        reference.set(dataNotifikasi);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        client.signOut();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Objects.requireNonNull(this.getActivity()).finish();
    }

    private void foto() {
        FirebaseFirestore.getInstance().collection("photos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentSnapshot snapshot : value) {
                            Post post = snapshot.toObject(Post.class);
                            if (post.getUploader().equals(profileId)) {
                                postList.add(post);
                            }
                        }
                        Collections.reverse(postList);
                        fotoAdapter.notifyDataSetChanged();
                    }
                });
    }
}