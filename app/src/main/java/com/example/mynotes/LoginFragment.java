package com.example.mynotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView toggleModeTextView;
    private boolean isLoginMode = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create the UI programmatically
        LinearLayout rootLayout = new LinearLayout(requireContext());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(32, 64, 32, 32);
        
        // Title
        TextView titleTextView = new TextView(requireContext());
        titleTextView.setText(isLoginMode ? "Login" : "Register");
        titleTextView.setTextSize(24);
        titleTextView.setPadding(0, 0, 0, 32);
        rootLayout.addView(titleTextView);
        
        // Email field
        emailEditText = new EditText(requireContext());
        emailEditText.setHint("Email");
        emailEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        rootLayout.addView(emailEditText);
        
        // Add some spacing
        View spacer1 = new View(requireContext());
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        rootLayout.addView(spacer1);
        
        // Password field
        passwordEditText = new EditText(requireContext());
        passwordEditText.setHint("Password");
        passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        rootLayout.addView(passwordEditText);
        
        // Add some spacing
        View spacer2 = new View(requireContext());
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 32));
        rootLayout.addView(spacer2);
        
        // Login/Register button
        loginButton = new Button(requireContext());
        loginButton.setText(isLoginMode ? "Login" : "Register");
        rootLayout.addView(loginButton);
        
        // Toggle mode text
        toggleModeTextView = new TextView(requireContext());
        toggleModeTextView.setText(isLoginMode ? "New user? Create an account" : "Already have an account? Login");
        toggleModeTextView.setPadding(0, 32, 0, 0);
        rootLayout.addView(toggleModeTextView);
        
        return rootLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (isLoginMode) {
                loginUser(email, password);
            } else {
                registerUser(email, password);
            }
        });
        
        toggleModeTextView.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            loginButton.setText(isLoginMode ? "Login" : "Register");
            toggleModeTextView.setText(isLoginMode ? "New user? Create an account" : "Already have an account? Login");
        });
    }
    
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_LoginFragment_to_NotesListFragment);
                    } else {
                        Toast.makeText(requireContext(), "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_LoginFragment_to_NotesListFragment);
                    } else {
                        Toast.makeText(requireContext(), "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}