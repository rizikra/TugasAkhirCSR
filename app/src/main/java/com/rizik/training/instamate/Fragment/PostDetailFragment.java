package com.rizik.training.instamate.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rizik.training.instamate.Adapter.PostAdapter;
import com.rizik.training.instamate.CommentActivity;
import com.rizik.training.instamate.MainActivity;
import com.rizik.training.instamate.Model.Post;
import com.rizik.training.instamate.R;

import java.util.ArrayList;
import java.util.List;

public class PostDetailFragment extends Fragment {
    private static final String TAG = "PostDetailFragment";

    private RecyclerView recyclerViewPostingan;
    private PostAdapter adapter;
    private List<Post> list;
    private String idupload;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        SharedPreferences preferences = getContext().getSharedPreferences(MainActivity.DATA_UID, Context.MODE_PRIVATE);
        idupload = preferences.getString(CommentActivity.ID_POST, "none");
        recyclerViewPostingan = view.findViewById(R.id.recycler_view_post_detail);
        recyclerViewPostingan.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewPostingan.setLayoutManager(linearLayoutManager);

        list = new ArrayList<>();
        adapter = new PostAdapter(getContext(), list);
        recyclerViewPostingan.setAdapter(adapter);
        bacaPostingan();

        return view;
    }
    private void bacaPostingan() {
        DocumentReference reference = FirebaseFirestore.getInstance().collection("photos")
                .document(idupload);
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                list.clear();
                Post postingan = value.toObject(Post.class);
                list.add(postingan);
                adapter.notifyDataSetChanged();
            }
        });
    }
}