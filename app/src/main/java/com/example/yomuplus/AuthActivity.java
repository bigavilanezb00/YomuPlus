package com.example.yomuplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    Button btn_login, btn_register;
    EditText email, password;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        btn_login = findViewById(R.id.logInButton);
        btn_register = findViewById(R.id.signUpButton);


        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                if (emailUser.isEmpty() && passUser.isEmpty()) {
                    Toast.makeText(AuthActivity.this, "INTRODUCE TUS DATOS", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(emailUser, passUser);
                }

            }

            private void loginUser(String emailUser, String passUser) {
                mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            finish();
                            startActivity(new Intent(AuthActivity.this, HomeActivity.class));
                            Toast.makeText(AuthActivity.this
                            ,"BIENVENIDO", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AuthActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AuthActivity.this, "ERROR AL INICIAR SESIÓN", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    public void onClick(View view) {
        Intent miIntent = new Intent(AuthActivity.this, RegisterActivity.class);
        startActivity(miIntent);
    }

}