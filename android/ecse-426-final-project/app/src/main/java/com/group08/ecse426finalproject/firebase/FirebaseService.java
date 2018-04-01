package com.group08.ecse426finalproject.firebase;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    public void uploadBytesUnique(final byte[] bytes, final String path) {
        databaseReference.child(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer counter = dataSnapshot.getValue(Integer.class);
                if (counter == null) {
                    counter = -1;
                }
                databaseReference.child(path).setValue(counter + 1);
                int dotIndex = path.indexOf('.');
                uploadBytes(bytes, path.substring(0, dotIndex) + counter + path.substring(dotIndex));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Failed to read counter.");
            }
        });

    }
}
