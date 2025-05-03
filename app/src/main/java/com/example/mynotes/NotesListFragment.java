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
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Note> notesList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate a temporary layout (we'll create the real one later)
        return inflater.inflate(R.layout.fragment_notes_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Setup menu provider (replacing deprecated setHasOptionsMenu)
        requireActivity().addMenuProvider(new androidx.core.view.MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.action_settings) {
                    // Handle settings action
                    return true;
                } else if (id == R.id.action_logout) {
                    // Sign out user
                    mAuth.signOut();
                    NavHostFragment.findNavController(NotesListFragment.this)
                            .navigate(R.id.action_NotesListFragment_to_LoginFragment);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), androidx.lifecycle.Lifecycle.State.RESUMED);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in
        if (mAuth.getCurrentUser() == null) {
            NavHostFragment.findNavController(NotesListFragment.this)
                    .navigate(R.id.action_NotesListFragment_to_LoginFragment);
            return;
        }

        userId = mAuth.getCurrentUser().getUid();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notesList = new ArrayList<>();

        // Create adapter for RecyclerView
        NoteAdapter adapter = new NoteAdapter(notesList, note -> {
            // Handle note click - navigate to editor with the note ID
            Bundle args = new Bundle();
            args.putBoolean("isNewNote", false);
            args.putString("noteId", note.getId());
            NavHostFragment.findNavController(NotesListFragment.this)
                    .navigate(R.id.action_NotesListFragment_to_NoteEditorFragment, args);
        });

        recyclerView.setAdapter(adapter);

        // Load notes from Firestore
        loadNotes(adapter);
    }

    private void loadNotes(NoteAdapter adapter) {
        db.collection("notes")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notesList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Note note = document.toObject(Note.class);
                        note.setId(document.getId()); // Ensure ID is set
                        notesList.add(note);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    String errorMessage = e.getMessage();
                    Toast.makeText(requireContext(), "Error loading notes: " + (errorMessage != null ? errorMessage : ""), Toast.LENGTH_SHORT).show();
                });
    }

    // Removed deprecated onCreateOptionsMenu and onOptionsItemSelected methods
    // Replaced with MenuProvider in onViewCreated

    // Inner class for RecyclerView adapter
    private static class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

        private final List<Note> notes;
        private final OnNoteClickListener listener;

        public interface OnNoteClickListener {
            void onNoteClick(Note note);
        }

        public NoteAdapter(List<Note> notes, OnNoteClickListener listener) {
            this.notes = notes;
            this.listener = listener;
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Note note = notes.get(position);
            holder.bind(note, listener);
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        static class NoteViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView textViewTitle;
            android.widget.TextView textViewContent;

            public NoteViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewContent = itemView.findViewById(R.id.textViewContent);
            }

            public void bind(Note note, OnNoteClickListener listener) {
                textViewTitle.setText(note.getTitle());
                // Show a preview of the content (first 50 chars)
                String content = note.getContent();
                if (content.length() > 50) {
                    content = content.substring(0, 50) + "...";
                }
                textViewContent.setText(content);

                itemView.setOnClickListener(v -> listener.onNoteClick(note));
            }
        }
    }
}