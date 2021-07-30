package com.verify.docverify;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class SelectFragment extends Fragment {
    private ImageView goBackUpload, front_image, back_image;
    private TextView docTypeUpload, frontTextView, backTextView;
    private MaterialButton uploadButton;
    private static final int RESULT_OK = -1;
    private static final int REQUEST_FRONT = 20;
    private static final int REQUEST_BACK = 30;
    private static int pos = 0;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DatabaseReference docRef;
    private StorageReference docStorage;

    public SelectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select, container, false);

        Bundle typeBundle = this.getArguments();
        String type = typeBundle.getString("docType");

        goBackUpload = view.findViewById(R.id.goBackUpload);
        docTypeUpload = view.findViewById(R.id.docTypeUpload);
        front_image = view.findViewById(R.id.front_image);
        frontTextView = view.findViewById(R.id.frontTextView);
        back_image = view.findViewById(R.id.back_image);
        backTextView = view.findViewById(R.id.backTextView);
        uploadButton = view.findViewById(R.id.uploadButton);

        docRef = FirebaseDatabase.getInstance().getReference("Documents");
        docStorage = FirebaseStorage.getInstance().getReference("Documents");

        docTypeUpload.setText("Verify your identity with " + type);

        goBack();
        docType(type);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Bundle bundle = result.getData().getExtras();
                            Bitmap bitmap = (Bitmap) bundle.get("data");
                            if (pos == 0) {
                                front_image.setImageBitmap(bitmap);
                            } else if (pos == 1) {
                                back_image.setImageBitmap(bitmap);
                            }
                            String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap, "title", null);
                            Uri uri = Uri.parse(path);
                            uploadButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    uploadDocument(uri);
                                }
                            });
                        }
                    }
                });

        selectPhoto();

        return view;
    }

    private void goBack() {
        goBackUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    private void docType(String type) {
        if (type.equals("Aadhaar Card")) {
            back_image.setVisibility(View.VISIBLE);
            backTextView.setVisibility(View.VISIBLE);
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        }
    }

    private void selectPhoto() {
        front_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                openSelectDialog("front");
            }
        });
        back_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                openSelectDialog("back");
            }
        });
    }

    private void openSelectDialog(String loc) {
        Dialog selectLocationDialog = new Dialog(getContext());
        selectLocationDialog.setContentView(R.layout.image_layout);
        selectLocationDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ImageView openCamera, openGallery;
        openCamera = selectLocationDialog.findViewById(R.id.openCameraButton);
        openGallery = selectLocationDialog.findViewById(R.id.openGalleryButton);

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraMethod(loc);
                selectLocationDialog.dismiss();
            }
        });

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryMethod(loc);
                selectLocationDialog.dismiss();
            }
        });
        selectLocationDialog.show();
    }

    private void openCameraMethod(String loc) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (loc.equals("front")) {
            pos = 0;
        } else if (loc.equals("back")) {
            pos = 1;
        }
        activityResultLauncher.launch(intent);
    }

    private void openGalleryMethod(String loc) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (loc.equals("front")) {
            startActivityForResult(intent, REQUEST_FRONT);
        } else if (loc.equals("back")) {
            startActivityForResult(intent, REQUEST_BACK);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == REQUEST_FRONT) {
                Picasso.get().load(uri).into(front_image);
            } else if (requestCode == REQUEST_BACK) {
                Picasso.get().load(uri).into(back_image);
            }
        }
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDocument(data.getData());
            }
        });
    }

    private void uploadDocument(Uri uri) {
        if (uri != null) {
            ProgressDialog pd = new ProgressDialog(getContext());
            pd.setCancelable(false);
            pd.setMessage("Uploading Document");
            String docId = String.valueOf(System.currentTimeMillis());
            StorageReference ref = docStorage.child(docId + ".jpeg");
            ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    while (!task.isComplete()) ;
                    Uri uri1 = task.getResult();
                    String url = uri1.toString();

                    docRef.child(docId).setValue(url);
                    Picasso.get().load(R.drawable.id_front).into(front_image);
                    Picasso.get().load(R.drawable.id_back).into(back_image);
                    showSuccessDialog();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double prog = (snapshot.getBytesTransferred() / snapshot.getTotalByteCount()) * 100;
                    pd.setMessage((int) prog + "% Uploading");
                    pd.show();
                }
            });
        }
    }

    private void showSuccessDialog() {
        Dialog successDialog = new Dialog(getContext());
        successDialog.setContentView(R.layout.document_uploaded_layout);
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        successDialog.setCancelable(false);
        successDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                successDialog.dismiss();
                getActivity().finish();
            }
        }, 1500);

    }
}
