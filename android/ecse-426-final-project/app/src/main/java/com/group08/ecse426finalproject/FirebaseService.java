package com.group08.ecse426finalproject;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private StorageReference storageRef;

    public FirebaseService()
    {
        storageRef = FirebaseStorage.getInstance().getReference();
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
}
