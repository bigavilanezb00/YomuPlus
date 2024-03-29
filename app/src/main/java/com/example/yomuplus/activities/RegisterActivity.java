package com.example.yomuplus.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.yomuplus.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //view binding
    private ActivityRegisterBinding binding;

    //firebase
    private FirebaseAuth firebaseAuth;

    //dialogo de progreso
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //iniciamos firebase

        firebaseAuth = FirebaseAuth.getInstance();

        //iniciamos diálogo de progreso

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Por favor espera");
        progressDialog.setCanceledOnTouchOutside(false);

        //habilitamos el boton para ir atrás

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //habilitamos boton registro
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String name = "", email = "", password = "";
    private void validateData() {
        /*Antes de crear la cuentahacemos algunas valifaciones de datos*/

        //cogemos los datos
        name = binding.nombreEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cPaswword = binding.cPasswordEt.getText().toString().trim();

        //validamos los datos
        if (TextUtils.isEmpty(name)) {
            // si el nombre esta vacio lo tiene que introducir
            Toast.makeText(this, "Introduce tu nombre", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // si el correo no es valido no permite continuar
            Toast.makeText(this, "Correo electrónico no valido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            //si la contraseña esta vacia no permite avanzar sin intriducir una
            Toast.makeText(this, "Introduce la contraseña", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cPaswword)) {
            // si esta vacio no permite avanzar hasta introducir la confirmación
            Toast.makeText(this, "Confirma la contraseña", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cPaswword)) {
            // si las contraseñas no son la misma no permite avanzar
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        } else {
            // si todos los datos son validos crea la cuenta
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //mostramos el progreso
        progressDialog.setMessage("Creando la cuenta");
        progressDialog.show();

        //creamos el usuario en firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //progreso de creacion de cuenta completado, ahora se añade a la base de datos en tiempo real
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //proceso de creacion de cuenta fallido
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Guardando información de usuario");

        //marca de tiempo
        long timestamp = System.currentTimeMillis();

        //obtener id del usuario
        String uid = firebaseAuth.getUid();

        //guardamos los datos en la base de datos
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); // de momento lo dejamos vacio
        hashMap.put("userType", "user");
        hashMap.put("timestamp", timestamp);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //DATOS AGREGADOS A LA BASE DE DATOS
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                        // Cuando la cuenta del usuario es creada finalmente lo enviamos al panel de usuarios
                        startActivity(new Intent(RegisterActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //datos fallidos agregados a la base de datos
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}