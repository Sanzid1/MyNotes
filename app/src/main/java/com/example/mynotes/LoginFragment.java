package com.example.mynotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private TextView textViewToggle;
    private boolean isLoginMode = true;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate a temporary layout (we'll create the real one later)
        return inflater.inflate(android.R.layout.simple_list_item_1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to NotesListFragment
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_LoginFragment_to_NotesListFragment);
            return;
        }

        // Since we're using a temporary layout, we'll just create a simple UI programmatically
        ViewGroup parent = (ViewGroup) view;
        parent.removeAllViews();

        // Create a vertical layout
        ViewGroup layout = new android.widget.LinearLayout(requireContext());
        ((android.widget.LinearLayout) layout).setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        parent.addView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Add title
        TextView title = new TextView(requireContext());
        title.setText("My Notes App");
        title.setTextSize(24);
        title.setGravity(android.view.Gravity.CENTER);
        title.setPadding(0, 0, 0, 50);
        layout.addView(title);

        // Add email field
        editTextEmail = new EditText(requireContext());
        editTextEmail.setHint("Email");
        editTextEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(editTextEmail);

        // Add password field
        editTextPassword = new EditText(requireContext());
        editTextPassword.setHint("Password");
        editTextPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        editTextPassword.setTransformationMethod(android.text.method.PasswordTransformationMethod.getInstance());
        layout.addView(editTextPassword);

        // Add login button
        buttonLogin = new Button(requireContext());
        buttonLogin.setText("Login");
        buttonLogin.setOnClickListener(v -> loginUser());
        layout.addView(buttonLogin);

        // Add register button
        buttonRegister = new Button(requireContext());
        buttonRegister.setText("Register");
        buttonRegister.setOnClickListener(v -> registerUser());
        layout.addView(buttonRegister);

        // Add toggle text
        textViewToggle = new TextView(requireContext());
        textViewToggle.setText("Switch to Register");
        textViewToggle.setGravity(android.view.Gravity.CENTER);
        textViewToggle.setPadding(0, 20, 0, 0);
        textViewToggle.setOnClickListener(v -> toggleMode());
        layout.addView(textViewToggle);

        updateUI();
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUI();
    }

    private void updateUI() {
        if (isLoginMode) {
            buttonLogin.setVisibility(View.VISIBLE);
            buttonRegister.setVisibility(View.GONE);
            textViewToggle.setText("Need an account? Register");
        } else {
            buttonLogin.setVisibility(View.GONE);
            buttonRegister.setVisibility(View.VISIBLE);
            textViewToggle.setText("Already have an account? Login");
        }
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_LoginFragment_to_NotesListFragment);
                    } else {
                        // If sign in fails, display a message to the user.
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "";
                        Toast.makeText(requireContext(), "Authentication failed: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Create user document in Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", email);
                            userMap.put("createdAt", com.google.firebase.Timestamp.now());

                            db.collection("users").document(user.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Navigate to notes list
                                        NavHostFragment.findNavController(LoginFragment.this)
                                                .navigate(R.id.action_LoginFragment_to_NotesListFragment);
                                    })
                                    .addOnFailureListener(e -> {
                                        String errorMessage = e.getMessage();
                                        Toast.makeText(requireContext(), "Error creating user profile: " + (errorMessage != null ? errorMessage : ""),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        // If registration fails, display a message to the user.
                        Exception exception = task.getException();
                        String errorMessage = exception != null ? exception.getMessage() : "";
                        Toast.makeText(requireContext(), "Registration failed: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}