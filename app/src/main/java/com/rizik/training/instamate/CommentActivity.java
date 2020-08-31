package com.rizik.training.instamate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rizik.training.instamate.Adapter.CommentAdapter;
import com.rizik.training.instamate.Model.Comment;
import com.rizik.training.instamate.Model.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    private static final String TAG = "CommentActivity";

    public static final String ID_POST = "postid";
    private EditText editTextComment;
    private ImageView imageViewPp;
    private TextView textViewKirimComment;
    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<Comment> comments;

    private String postid, publisherId;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();

        Toolbar toolbar = findViewById(R.id.toolbar_comment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        comments = new ArrayList<>();
        adapter = new CommentAdapter(this, comments);
        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        postid = intent.getStringExtra(ID_POST);
        publisherId = intent.getStringExtra(CommentAdapter.ID_PUBRISHER);

        textViewKirimComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextComment.getText().toString().matches("")) {
                    Toast.makeText(CommentActivity.this, "anda tidak bisa mengirimkan komentar kosong", Toast.LENGTH_SHORT).show();
                } else {
                    tambahDataKomentar();
                }
            }
        });

        getPp();
        bacaComment();

    }

    public void tambahDataKomentar() {
        Map<String, Object> dataComment = new HashMap<>();
        dataComment.put("text", editTextComment.getText().toString().trim());
        dataComment.put("mrcomment", user.getUid());
        FirebaseFirestore.getInstance().collection("comments").document(postid).collection(postid)
                .add(dataComment).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Toast.makeText(CommentActivity.this, "komen berhasil dikirim", Toast.LENGTH_SHORT).show();
                addNotifikasi();
                editTextComment.setText("");
            }
        });
    }

    private void addNotifikasi() {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("notifikasi")
                .document(publisherId);
        Map<String, Object> dataNotifikasi = new HashMap<>();
        dataNotifikasi.put("id_user", user.getUid());
        dataNotifikasi.put("text", "mengomentari: " + editTextComment.getText().toString());
        dataNotifikasi.put("idupload", postid);
        dataNotifikasi.put("ispost", true);
        reference.set(dataNotifikasi);
    }

    private void getPp() {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                UserData userData = task.getResult().toObject(UserData.class);
                Log.d(TAG, "onComplete: datauser " + userData);
                Glide.with(getApplicationContext()).load(userData.getImageUrl()).into(imageViewPp);
            }
        });
    }

    private void init() {
        editTextComment = findViewById(R.id.e_text_tambah_commentar);
        imageViewPp = findViewById(R.id.image_view_pp_comment);
        textViewKirimComment = findViewById(R.id.text_kirim_comment);
        recyclerView = findViewById(R.id.recycler_view_comment);
    }

    private void bacaComment() {
        FirebaseFirestore.getInstance().collection("comments").document(postid).collection(postid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                        comments.clear();
                        for (DocumentSnapshot snapshot : value) {
                            Comment comment = snapshot.toObject(Comment.class);
                            comments.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

    }
}