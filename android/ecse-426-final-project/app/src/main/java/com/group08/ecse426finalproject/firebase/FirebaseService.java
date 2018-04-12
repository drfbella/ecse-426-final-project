package com.group08.ecse426finalproject.firebase;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private DatabaseReference databaseReference;
    private StorageReference storageRef;

    public FirebaseService()
    {
        storageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void uploadBytes(byte[] bytes, String path)
    {
        storageRef.child(path).putBytes(bytes)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d(TAG, "Uploaded URL: " + downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Upload failed");
                    }
                });
    }

    public void uploadBytesUnique(final byte[] bytes, final String path, final String extension) {
        uploadBytes(bytes, path + (System.currentTimeMillis() / 1000) + '.' + extension);
    }
}
