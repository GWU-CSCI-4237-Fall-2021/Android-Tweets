package edu.gwu.androidtweetsfall2021;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This class is the rough equivalent of the MainActivity, but written in Java so you can see
 * a comparison between Java & Kotlin.
 */
public class MainActivityJava extends AppCompatActivity  {

    private EditText username;

    private EditText password;

    private Button login;

    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);

        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                Intent intent = new Intent(MainActivityJava.this, TweetsActivity.class);
                intent.putExtra("location", "Washington");
                startActivity(intent);
            }
        });
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String inputtedUsername = username.getText().toString().trim();
            String inputtedPassword = password.getText().toString().trim();
            boolean enableButton = inputtedUsername.length() > 0 && inputtedPassword.length() > 0;

            login.setEnabled(enableButton);
        }
    };
}