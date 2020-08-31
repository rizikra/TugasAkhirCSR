package com.rizik.training.instamate.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rizik.training.instamate.Model.UserData;
import com.rizik.training.instamate.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    private UserAdapter userAdapter;
    private List<UserData> users;
    private EditText editTextCari;
    private TextView textViewNoUser;
    private RecyclerView recyclerView;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_search, container, false);        recyclerView = view.findViewById(R.id.recycler_view_cari);
        textViewNoUser = view.findViewById(R.id.text_view_noUser);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        editTextCari = view.findViewById(R.id.e_text_cari);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(this.getContext(), users);
        recyclerView.setAdapter(userAdapter);

        editTextCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                cariUser(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        bacaUsers();

        return view;
    }
    private void cariUser(final String key) {
        final CollectionReference reference = FirebaseFirestore.getInstance().collection("users");
        reference.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        users.clear();
                        for (QueryDocumentSnapshot snapshot : Objects.requireNonNull(task.getResult())) {
                            UserData userData = snapshot.toObject(UserData.class);
                            if (userData.getUsername().contains(key)) {
                                users.add(userData);
                            }
                        }
                        userAdapter.notifyDataSetChanged();

                    }
                }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (users.isEmpty()) {
                    textViewNoUser.setVisibility(View.VISIBLE);
                    String template = "tidak ada user dengan nama " + key + " ditemukan";
                    textViewNoUser.setText(template);
                } else {
                    textViewNoUser.setVisibility(View.GONE);
                }
            }
        });

    }
    private void bacaUsers() {
        FirebaseFirestore.getInstance().collection("users")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (editTextCari.getText().toString().equals("")) {
                            users.clear();
                            assert value != null;
                            for (DocumentSnapshot snapshots : value) {
                                UserData userData = snapshots.toObject(UserData.class);
                                users.add(userData);
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
}