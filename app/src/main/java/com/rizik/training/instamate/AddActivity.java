package com.rizik.training.instamate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {
    private static final String TAG = "AddActivity";

    private Uri imageUri;
    private String uriKu = "";
    private StorageTask uploadTask;
    private StorageReference storageReference;
    private ImageView imageViewClose, imageViewChoose;
    private TextView upload;
    private EditText editTextDeskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        init();

        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadGambar();
            }
        });

        CropImage.activity().setAspectRatio(1, 1)
                .start(AddActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            imageViewChoose.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "ada yang salah, tergantung amal perbuatan \uD83D\uDE14 ", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddActivity.this, MainActivity.class));
            finish();
        }
    }

    public static String ekstensiFile(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }
        return extension;
    }

    private void uploadGambar() {
        final ProgressDialog loadingUpload = new ProgressDialog(AddActivity.this);
        loadingUpload.setMessage("sedang diupload, harap tunggu...");
        loadingUpload.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + ekstensiFile(getApplicationContext(), imageUri));
            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isComplete()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        uriKu = downloadUrl.toString();

                        CollectionReference reference = FirebaseFirestore.getInstance().collection("photos");
                        String idUpload = reference.document().getId();

                        Map<String, Object> dataUpload = new HashMap<>();
                        dataUpload.put("idupload", idUpload);
                        dataUpload.put("gambar", uriKu);
                        dataUpload.put("deskripsi", editTextDeskripsi.getText().toString().trim());
                        dataUpload.put("pengapload", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        reference.document(idUpload).set(dataUpload).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadingUpload.dismiss();
                                    startActivity(new Intent(AddActivity.this, MainActivity.class));
                                    Toast.makeText(getApplicationContext(), "Berhasil input database", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(AddActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(AddActivity.this, "anda belum memilih gambar", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Toast.makeText(AddActivity.this, "on canceleled listener", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddActivity.this, "on failure, " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void init() {
        editTextDeskripsi = findViewById(R.id.e_text_deskripsi_add);
        imageViewClose = findViewById(R.id.image_view_close);
        upload = findViewById(R.id.t_view_upload_bar);
        imageViewChoose = findViewById(R.id.image_view_pilih_foto);

        //Firebase
        storageReference = FirebaseStorage.getInstance().getReference("photos");
    }
}
