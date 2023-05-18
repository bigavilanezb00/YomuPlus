package com.example.yomuplus.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.yomuplus.MyApplication;
import com.example.yomuplus.R;
import com.example.yomuplus.adapters.AdapterComment;
import com.example.yomuplus.adapters.AdapterPdfFavorite;
import com.example.yomuplus.databinding.ActivityPdfDetailBinding;
import com.example.yomuplus.databinding.DialogCommentAddBinding;
import com.example.yomuplus.models.ModelComment;
import com.example.yomuplus.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    private ActivityPdfDetailBinding binding;

    String bookId, bookTitle, bookUrl;

    boolean isInMyFavorite = false;

    private FirebaseAuth firebaseAuth;

    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";

    private ProgressDialog progressDialog;

    private ArrayList<ModelComment> commentArrayList;
    private AdapterComment adapterComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        binding.downloadBookBtn.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor esperé");
        progressDialog.setCanceledOnTouchOutside(false);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();
        }

        loadBookDetails();
        loadComments();

        // contador de visualizaciones
        MyApplication.incrementBookViewsCount(bookId);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfViewActivity.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        binding.downloadBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG_DOWNLOAD, "onClick: Comprobando permisos");
                if (ContextCompat.checkSelfPermission(PdfDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG_DOWNLOAD, "onClick: Permisos concedidos, puedes descargar el libro");
                    MyApplication.downloadBook(PdfDetailActivity.this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else {
                    Log.d(TAG_DOWNLOAD, "onClick: Permisos no concedidos, pedir permisos");
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        binding.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null ) {
                    Toast.makeText(PdfDetailActivity.this, "Inicie sesión por favor.", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (isInMyFavorite) {
                        MyApplication.removeFromFavorite(PdfDetailActivity.this, bookId);
                    }
                    else {
                        MyApplication.addToFavorite(PdfDetailActivity.this, bookId);
                    }
                }
            }
        });

        binding.addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(PdfDetailActivity.this, "Inicia sesión por favor", Toast.LENGTH_SHORT).show();
                }
                else {
                    addCommentDialog();
                }
            }
        });

    }

    private void loadComments() {
        commentArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Libros");
        ref.child(bookId).child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelComment model = ds.getValue(ModelComment.class);
                            commentArrayList.add(model);
                        }
                        adapterComment = new AdapterComment(PdfDetailActivity.this, commentArrayList);
                        binding.commentsRv.setAdapter(adapterComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String comment = "";

    private void addCommentDialog() {
        DialogCommentAddBinding commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this));

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        builder.setView(commentAddBinding.getRoot());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        commentAddBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        commentAddBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment = commentAddBinding.commentEt.getText().toString().trim();

                if (TextUtils.isEmpty(comment)) {
                    Toast.makeText(PdfDetailActivity.this, "Introduce un comentario", Toast.LENGTH_SHORT).show();
                }
                else {
                    alertDialog.dismiss();
                    addComment();
                }
            }
        });

    }

    private void addComment() {
        progressDialog.setMessage("Agregando comentario");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("bookId", ""+bookId);
        hashMap.put("timestamp", ""+timestamp);
        hashMap.put("comment", ""+comment);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        // Ruta de la base de datos
        // Libros > bookId > Comments > commentId > commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Libros");
        ref.child(bookId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(PdfDetailActivity.this, "Comentario publicado", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfDetailActivity.this, "Fallo al publicar el comentario debido a "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG_DOWNLOAD, ": Permiso concedido");
                    MyApplication.downloadBook(this, ""+bookId, ""+bookTitle, ""+bookUrl);
                }
                else  {
                    Log.d(TAG_DOWNLOAD, "Permiso denegado...: ");
                    Toast.makeText(this, "Permiso denegado...", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Libros");
        ref.child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        binding.downloadBookBtn.setVisibility(View.VISIBLE);

                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );
                        MyApplication.loadPdfFromUrlSinglePage(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.pdfView,
                                binding.progressBar,
                                binding.pagesTv
                        );
                        MyApplication.loadPdfSize(
                                ""+bookUrl,
                                ""+bookTitle,
                                binding.sizeTv
                        );

                        binding.titleTv.setText(bookTitle);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace("null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkIsFavorite() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Favoritos").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavorite = snapshot.exists();

                        if (isInMyFavorite) {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0, 0);
                            binding.favoriteBtn.setText("Eliminar Favorito");
                        }
                        else {
                            binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0);
                            binding.favoriteBtn.setText("Agregar Favorito");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}