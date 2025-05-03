package com.example.mynotes;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mynotes.databinding.FragmentNoteEditorBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NoteEditorFragment extends Fragment {

    private FragmentNoteEditorBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private String noteId;
    private boolean isNewNote;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentNoteEditorBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            NavHostFragment.findNavController(NoteEditorFragment.this)
                    .navigate(R.id.action_NoteEditorFragment_to_LoginFragment);
            return;
        }

        userId = mAuth.getCurrentUser().getUid();

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            isNewNote = args.getBoolean("isNewNote", true);
            if (!isNewNote) {
                noteId = args.getString("noteId");
                loadNote(noteId);
            }
        }

        binding.buttonSave.setOnClickListener(v -> saveNote());
    }

    private void loadNote(String noteId) {
        db.collection("notes").document(noteId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            binding.editTextTitle.setText(note.getTitle());
                            binding.editTextContent.setText(note.getContent());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error loading note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNote() {
        String title = binding.editTextTitle.getText().toString().trim();
        String content = binding.editTextContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.editTextTitle.setError("Title is required");
            return;
        }

        Map<String, Object> noteMap = new HashMap<>();
        noteMap.put("title", title);
        noteMap.put("content", content);
        noteMap.put("userId", userId);
        noteMap.put("timestamp", Timestamp.now());

        if (isNewNote) {
            // Create new note
            db.collection("notes").add(noteMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "Note saved", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(NoteEditorFragment.this)
                                .navigate(R.id.action_NoteEditorFragment_to_NotesListFragment);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error saving note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Update existing note
            db.collection("notes").document(noteId).update(noteMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Note updated", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(NoteEditorFragment.this)
                                .navigate(R.id.action_NoteEditorFragment_to_NotesListFragment);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error updating note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_editor, menu);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        if (deleteItem != null) {
            deleteItem.setVisible(!isNewNote);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            deleteNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteNote() {
        if (noteId != null) {
            db.collection("notes").document(noteId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show();