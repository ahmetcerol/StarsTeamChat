package com.example.starsteamchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.starsteamchat.databinding.ActivitySignInBinding;
import com.example.starsteamchat.utilities.Constants;
import com.example.starsteamchat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding signInBinding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        signInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(signInBinding.getRoot());
        setListeners();
    }

    private void setListeners(){
        signInBinding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)) );
        signInBinding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDeatils()){
                signIn();
            }
        });
    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,signInBinding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,signInBinding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult()!=null
                            && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }else {
                        loading(false);
                        showToast("Giriş Yapılamıyor");
                    }
                });

    }

    private void loading(Boolean isLoading){
        if (isLoading){
            signInBinding.buttonSignIn.setVisibility(View.INVISIBLE);
            signInBinding.proggresBar.setVisibility(View.VISIBLE);
        }else {
            signInBinding.proggresBar.setVisibility(View.INVISIBLE);
            signInBinding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDeatils(){
        if (signInBinding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Emailinizi Girin");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(signInBinding.inputEmail.getText().toString()).matches()){
            showToast("Doğru bir mail girin");
            return false;
        } else if(signInBinding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Şifrenizi Girin");
            return  false;
        }else{return true;}
    }
}