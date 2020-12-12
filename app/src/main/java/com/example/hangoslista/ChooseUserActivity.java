package com.example.hangoslista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseUserActivity extends AppCompatActivity {

    private TextView helpText, nameText, existingUserNameText;
    private EditText userNameText;

    //SharedPreferences
    public static String STATE_KEY_USERSLIST = BuildConfig.APPLICATION_ID + ".shlRefUserList";
    private SharedPreferences sharedPref;
    private static String sharedPrefFile = "shrPrefFile";
    private String actualUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_user);

        helpText = findViewById(R.id.helpText);
        nameText = findViewById(R.id.nameText);
        userNameText = findViewById(R.id.userNameText);
        existingUserNameText = findViewById(R.id.existingUserNameText);

        //SharedPreferences
        actualUserName = getString(R.string.defaultUser);
        sharedPref = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        existingUserNameText.setText(sharedPref.getString(STATE_KEY_USERSLIST, actualUserName + "\n"));

        saveSharedPref();
    }

    //How to save
    protected void saveSharedPref(){
        SharedPreferences.Editor pEdit = sharedPref.edit();
        pEdit.putString(STATE_KEY_USERSLIST, existingUserNameText.getText().toString());
        pEdit.apply();
    }

    //Save
    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPref();
    }

    //Login - Intent return
    public final static String INTENTRESULT = "ABC_USERNAME";

    public void loginUser(View view) {
        String name = userNameText.getText().toString();

        if(name.isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.empty_userName), Toast.LENGTH_SHORT).show();
            return;
        }

        //Save the new user
        if(!existingUserNameText.getText().toString().contains(name + "\n")){
            existingUserNameText.append(name + "\n");
            saveSharedPref();
        }

        Intent replyIntent = new Intent();
        replyIntent.putExtra(INTENTRESULT, name);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    public void clearUsers(View view) {
        existingUserNameText.setText(getString(R.string.defaultUser) + "\n");
        actualUserName = getString(R.string.defaultUser);
        //userNameText.setText(actualUserName);
        saveSharedPref();
    }
}