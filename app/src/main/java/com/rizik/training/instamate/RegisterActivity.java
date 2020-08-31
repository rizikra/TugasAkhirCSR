package com.rizik.training.instamate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rizik.training.instamate.Model.UserData;

import java.util.Objects;

import static android.view.View.GONE;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText editTextUserName, editTextFullName, editTextPassword, editTextKonfirmPassword, editTextEmail;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ProgressBar loading;

    //    firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);

                String username = editTextUserName.getText().toString().trim().toLowerCase();
                String fullname = editTextFullName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (isValidEmail(email)) {
                    if (validasi()) {
                        register(username, password, email, fullname);
                    }
                } else {
                    if (!editTextEmail.getText().toString().isEmpty()) {
                        editTextEmail.setError("Email tidak valid");
                    } else {
                        editTextEmail.setError("Email tidak boleh kosong");
                    }
                    editTextEmail.requestFocus();
                    loading.setVisibility(GONE);
                }
            }
        });
    }

    private void init() {
        editTextEmail = findViewById(R.id.input_email);
        editTextFullName = findViewById(R.id.input_fullname);
        editTextUserName = findViewById(R.id.input_username);
        editTextPassword = findViewById(R.id.input_password);
        editTextKonfirmPassword = findViewById(R.id.input_confirmPassword);
        buttonRegister = findViewById(R.id.btn_register);
        textViewLogin = findViewById(R.id.tv_login);
        loading = findViewById(R.id.progressBar);

        //firebase
        auth = FirebaseAuth.getInstance();

    }

    private boolean validasi() {
        boolean valid = true;
        if (editTextPassword.getText().toString().matches("")) {
            editTextPassword.setError("Password tidak boleh kosong");
            editTextPassword.requestFocus();
            valid = false;
        } else if (editTextPassword.getText().toString().length() < 6) {
            editTextPassword.setError("Password harus berisi minimal 6 karakter");
            editTextPassword.requestFocus();
            valid = false;
        }

        if (!editTextKonfirmPassword.getText().toString().equalsIgnoreCase(editTextPassword.getText().toString())) {
            editTextKonfirmPassword.setError("Password tidak sama");
            editTextKonfirmPassword.requestFocus();
            valid = false;
        } else if (editTextKonfirmPassword.getText().toString().matches("")) {
            editTextKonfirmPassword.setError("Harap isi konfirmasi password");
            editTextKonfirmPassword.requestFocus();
            valid = false;
        }

        if (editTextFullName.getText().toString().matches("")) {
            editTextPassword.setError("Harap isi field ini");
            editTextPassword.requestFocus();
            valid = false;
        }
        if (editTextUserName.getText().toString().matches("")) {
            editTextPassword.setError("Harap isi field ini");
            editTextPassword.requestFocus();
            valid = false;
        }

        return valid;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void register(final String username, String password, String email, final String fullname) {
        auth.createUserWithEmailAndPassword(
                email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loading.setVisibility(GONE);
                            FirebaseUser user = auth.getCurrentUser();
                            String userId = user.getUid();
                            String urlImage = "https://firebasestorage.googleapis.com/v0/b/instamate-64302.appspot.com/o/profile.png?alt=media&token=66afae02-5ef5-4104-bf1c-c177a55bd13a";
                            UserData userData = new UserData(userId, username, fullname, "", urlImage);

                            db = FirebaseFirestore.getInstance();
                            db.collection("users").document(userId).set(userData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loading.setVisibility(GONE);
                                                Toast.makeText(RegisterActivity.this, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show();
                                                Intent intentLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                                                intentLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intentLogin);
                                            }
                                        }
                                    });
                        } else {
                            String errorcode = ((FirebaseAuthException) Objects.requireNonNull(task.getException())).getErrorCode();
                            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(errorcode)) {
                                loading.setVisibility(View.GONE);
                                editTextEmail.setError("Email sudah terdaftar, harap login saja");
                                editTextEmail.requestFocus();
                            }
                        }
                    }
                });
    }
}