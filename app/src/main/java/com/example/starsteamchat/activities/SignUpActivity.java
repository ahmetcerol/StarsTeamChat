package com.example.starsteamchat.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;


import com.example.starsteamchat.databinding.ActivitySignUpBinding;
import com.example.starsteamchat.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.starsteamchat.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding signUpBinding;
    private PreferenceManager preferenceManager;
    private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(signUpBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    private void setListeners(){

        signUpBinding.textSignIn.setOnClickListener(v-> onBackPressed());
        signUpBinding.buttonSignUp.setOnClickListener(v ->{
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        signUpBinding.layoutImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String,Object> user= new HashMap<>();
        user.put(Constants.KEY_NAME, signUpBinding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, signUpBinding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, signUpBinding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,signUpBinding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception ->{
                    loading(false);
                    showToast(exception.getMessage());

                } );

    }

    private String getEncodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight= bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()==RESULT_OK) {
                    if (result.getData()!=null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            signUpBinding.imageProfile.setImageBitmap(bitmap);
                            signUpBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = getEncodedImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }

            }
    );

    private Boolean isValidSignUpDetails(){

        if (encodedImage==null){
            showToast("Lütfen profil fotoğrafı seçin");
            return false;
        }else if(signUpBinding.inputName.getText().toString().trim().isEmpty()){
            showToast("Adınızı Girin");
            return false;
        }else if(signUpBinding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Emailinizi Girin");
            return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(signUpBinding.inputEmail.getText().toString()).matches()){
            showToast("Geçerli bir email girin");
            return false;
        }else if(signUpBinding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Şifrenizi Girin");
            return false;
        }else if(signUpBinding.inputConfirmPassword.getText().toString().trim().isEmpty()){
            showToast(" Şifrenizi Doğrulayın");
            return false;
        }else if(!signUpBinding.inputPassword.getText().toString().equals(signUpBinding.inputConfirmPassword.getText().toString())){
            showToast("Şifreler uyuşmuyor");
            return false;
        }
        else {
            return true;}

    }
    private void loading(Boolean isLoading){
        if (isLoading){
            signUpBinding.buttonSignUp.setVisibility(View.INVISIBLE);
            signUpBinding.progressBar.setVisibility(View.VISIBLE);
        }else {
            signUpBinding.progressBar.setVisibility(View.INVISIBLE);
            signUpBinding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}