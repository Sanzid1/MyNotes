package com.example.mynotes;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoteEditorFragment extends Fragment {

    private FragmentNoteEditorBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String noteId;
    private boolean isEditMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNoteEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // User not logged in, navigate to login screen
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_NoteEditorFragment_to_LoginFragment);
            return;
        }

        // Check if we're editing an existing note
        if (getArguments() != null && getArguments().containsKey("noteId")) {
            noteId = getArguments().getString("noteId");
            isEditMode = true;
            loadNoteData();
        }

        binding.buttonSave.setOnClickListener(v -> saveNote());
    }

    private void loadNoteData() {
        db.collection("notes").document(noteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Note note = documentSnapshot.toObject(Note.class);
                        if (note != null) {
                            binding.editTextTitle.setText(note.getTitle());
                            binding.editTextContent.setText(note.getContent());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), 
                        "Error loading note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveNote() {
        String title = binding.editTextTitle.getText().toString().trim();
        String content = binding.editTextContent.getText().toString().trim();

        if (title.isEmpty()) {
            binding.editTextTitle.setError("Title cannot be empty");
            return;
        }

        if (isEditMode) {
            // Update existing note
            db.collection("notes").document(noteId)
                    .update(
                            "title", title,
                            "content", content
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Note updated", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_NoteEditorFragment_to_NotesListFragment);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            "Error updating note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Create new note
            Note newNote = new Note(title, content, currentUser.getUid());
            
            db.collection("notes")
                    .add(newNote)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(requireContext(), "Note added", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_NoteEditorFragment_to_NotesListFragment);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            "Error adding note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (isEditMode) {
            inflater.inflate(R.menu.menu_note_editor, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            deleteNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteNote() {
        if (isEditMode && noteId != null) {
            db.collection("notes").document(noteId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_NoteEditorFragment_to_NotesListFragment);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(),
                            "Error deleting note: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}