package com.example.yomuplus.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yomuplus.MyApplication;
import com.example.yomuplus.R;
import com.example.yomuplus.adapters.AdapterPdfFavorite;
import com.example.yomuplus.databinding.ActivityProfileBinding;
import com.example.yomuplus.models.ModelPdf;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;


    private ArrayList<ModelPdf> pdfArrayList;
    private AdapterPdfFavorite adapterPdfFavorite;

    private ProgressDialog progressDialog;

    private static final String TAG = "PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // reiniciamos la información del usuario
        binding.accountTypeTv.setText("N/A");
        binding.memberDateTv.setText("N/A");
        binding.favoriteBookCountTv.setText("N/A");
        binding.accountStatusTv.setText("N/A");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor esperé");
        progressDialog.setCanceledOnTouchOutside(false);

        loadUserInfo();
        loadFavoriteBooks();

        binding.profileEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ProfileEditActivity.class));
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.accountStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseUser.isEmailVerified()) {
                    Toast.makeText(ProfileActivity.this, "Usuario verificado", Toast.LENGTH_SHORT).show();

                }
                else {
                    emailVerificationDialog();
                }
            }
        });

    }

    private void emailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verifica correo")
                .setMessage("¿Estás seguro de querer recibir un correo a "+firebaseUser.getEmail()+" con las instrucciones para verificar tu cuenta?")
                .setPositiveButton("ENVIAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEmailVerification();
                    }
                })
                .setNegativeButton("CANCELAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void sendEmailVerification() {
        progressDialog.setMessage("Enviando correo con las instrucciones para verificar a "+firebaseUser.getEmail());
        progressDialog.show();

        firebaseUser.sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Instrucciones enviadas, verifique su correo "+firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileActivity.this, "Fallo debido a "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Cargando información de usuario del usuario "+firebaseAuth.getUid());

        if (firebaseUser.isEmailVerified()) {
            binding.accountStatusTv.setText("Verificado");
        }
        else {
            binding.accountStatusTv.setText("No verificado");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //obtenemos toda la informacion del usuario desde snapshot
                        String email = ""+snapshot.child("email").getValue();
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String userType = ""+snapshot.child("userType").getValue();

                        //formato de la fecha a dd/MM/yyyy
                        String formattedDate = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);
                        binding.memberDateTv.setText(formattedDate);
                        binding.accountTypeTv.setText(userType);

                        //colocamos la imagn utilizando glide
                        Glide.with(getApplicationContext())
                                .load(profileImage)
                                .placeholder(R.drawable.ic_persona_gray)
                                .into(binding.profileIv);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadFavoriteBooks() {
        //iniciamos array list
        pdfArrayList = new ArrayList<>();

        //cargamos los libros favoritos desde la base de datos
        // Users > userId > Favoritos
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favoritos")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //limpiamos la lista antes de agregas lso datos
                        pdfArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            // solo necesitams el bookId, obtenemos los demas detalles del adapter usando ese bookId
                            String bookId = ""+ds.child("bookId").getValue();

                            ModelPdf modelPdf = new ModelPdf();
                            modelPdf.setId(bookId);

                            pdfArrayList.add(modelPdf);
                        }

                        binding.favoriteBookCountTv.setText(""+pdfArrayList.size());
                        adapterPdfFavorite = new AdapterPdfFavorite(ProfileActivity.this, pdfArrayList);

                        binding.booksRv.setAdapter(adapterPdfFavorite);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}