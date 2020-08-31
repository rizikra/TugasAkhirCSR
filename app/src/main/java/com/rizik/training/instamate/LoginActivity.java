package com.rizik.training.instamate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.rizik.training.instamate.Model.UserData;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar loadingLogin;
    private ImageView imageViewLoginGoogle;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initWidgets();

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingLogin.setVisibility(View.VISIBLE);

                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (validasi()) {
                    if (RegisterActivity.isValidEmail(email)) {
                        loginEmail(email, password);
                    } else {
                        editTextEmail.setError("email tidak valid");
                        editTextEmail.requestFocus();
                    }
                }
            }
        });
        //Login Google

        imageViewLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginGoogle();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
    }

    private void initWidgets() {
        editTextEmail = findViewById(R.id.input_email);
        editTextPassword = findViewById(R.id.input_password);
        buttonLogin = findViewById(R.id.btn_login);
        textViewRegister = findViewById(R.id.link_signUp);
        loadingLogin = findViewById(R.id.progressBar);
        imageViewLoginGoogle = findViewById(R.id.loginGoogle);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
    }

    private boolean validasi() {
        boolean valid = true;
        if (editTextPassword.getText().toString().matches("")) {
            editTextPassword.setError("Harap isi password");
            editTextPassword.requestFocus();
            valid = false;
        }
        if (editTextEmail.getText().toString().matches("")) {
            editTextEmail.setError("Email tidak boleh kosong");
            editTextEmail.requestFocus();
            valid = false;
        }
        loadingLogin.setVisibility(View.GONE);

        return valid;
    }

    private void loginEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            db = FirebaseFirestore.getInstance();
                            DocumentReference reference = db.collection("users").document(mAuth.getCurrentUser().getUid());
                            reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                    loadingLogin.setVisibility(View.GONE);
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            String errorcode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                            switch (errorcode) {
                                case "ERROR_WRONG_PASSWORD":
                                    loadingLogin.setVisibility(View.GONE);
                                    editTextPassword.setError("Password salah");
                                    editTextPassword.requestFocus();
                                    break;
                                case "ERROR_USER_NOT_FOUND":
                                    loadingLogin.setVisibility(View.GONE);
                                    editTextEmail.setError("Email belum terdaftar");
                                    editTextEmail.requestFocus();
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = accountTask.getResult(ApiException.class);
            assert account != null;
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            e.printStackTrace();
            Toast.makeText(this, "Status Code " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    db = FirebaseFirestore.getInstance();
                    DocumentReference reference = db.collection("users").document(mAuth.getCurrentUser().getUid());
                    reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            loadingLogin.setVisibility(View.GONE);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                    String userId = mAuth.getCurrentUser().getUid();
                    String nama = mAuth.getCurrentUser().getDisplayName().toLowerCase();
                    String username = nama.replace(" ", ".");
                    String fullname = mAuth.getCurrentUser().getDisplayName();
                    String urlImage = "https://firebasestorage.googleapis.com/v0/b/instamate-64302.appspot.com/o/profile.png?alt=media&token=66afae02-5ef5-4104-bf1c-c177a55bd13a";
                    UserData userData = new UserData(userId, username, fullname, "", urlImage);

                    db.collection("users").document(userId).set(userData);
                }
            }
        });
    }
    private void loginGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }
}
