package com.example.diariopersonal;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText correoTxt, contraseñaTxt;
    private TextView errorLbl;
    private Button ingresarBtn, registrarseBtn;
    private ImageButton googleBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Autentificacion Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Conectar las vistas
        correoTxt = findViewById(R.id.lblCorreo);
        contraseñaTxt = findViewById(R.id.lblContraseña);
        ingresarBtn = findViewById(R.id.btnIngresar);
        registrarseBtn = findViewById(R.id.btnRegistrarse);
        googleBtn = findViewById(R.id.btnGoogle);
        errorLbl = findViewById(R.id.lblError);

        // Ocultar el mensaje de error
        errorLbl.setVisibility(View.GONE);

        // Configurar los listeners de los botones

        ingresarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = correoTxt.getText().toString();
                String contraseña = contraseñaTxt.getText().toString();
                if (correo.isEmpty() || contraseña.isEmpty()) {
                    errorLbl.setText("Por favor, llena todos los campos");
                    errorLbl.setVisibility(View.VISIBLE);
                } else if (!correo.contains("@") || !correo.contains(".")){
                    errorLbl.setText("Correo inválido");
                    errorLbl.setVisibility(View.VISIBLE);
                } else {
                    // Lógica para iniciar sesión con correo y contraseña
                    signIn(correo, contraseña);
                    errorLbl.setVisibility(View.GONE);
                }
            }
        });

        registrarseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de registro
                Intent intent = new Intent(MainActivity.this, newUser.class);
                startActivity(intent);
            }
        });

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            errorLbl.setText("Correo o contraseña incorrectos");
                            errorLbl.setVisibility(View.VISIBLE);
                            updateUI(null);
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado devuelto al iniciar Intent desde GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In fue exitoso, autenticar con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In falló, actualizar la UI adecuadamente
                errorLbl.setText("Error al iniciar sesión con Google");
                errorLbl.setVisibility(View.VISIBLE);
                updateUI(null);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        /// Verificar si la tarea fue exitosa
                        if (task.isSuccessful()) {
                            // Inicio de sesión exitoso, actualizar la UI con la información del usuario
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // Si falla, mostrar un mensaje al usuario
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Navegar a la actividad principal del diario
            Intent intent = new Intent(MainActivity.this, Menu.class);
            startActivity(intent);
            finish();
        } else {
        }
    }
}