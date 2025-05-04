package com.example.mynotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynotes.databinding.FragmentNotesListBinding;
import com.example.mynotes.databinding.ItemNoteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotesListFragment extends Fragment {

    private FragmentNotesListBinding binding;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private NotesAdapter adapter;
    private List<Note> notesList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesListBinding.inflate(inflater, container, false);
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
                    .navigate(R.id.action_NotesListFragment_to_LoginFragment);
            return;
        }

        // Setup RecyclerView
        binding.recyclerViewNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotesAdapter();
        binding.recyclerViewNotes.setAdapter(adapter);

        // Load notes from Firestore
        loadNotes();
    }

    private void loadNotes() {
        db.collection("notes")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error loading notes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notesList.clear();
                    if (value != null && !value.isEmpty()) {
                        for (QueryDocumentSnapshot document : value) {
                            Note note = document.toObject(Note.class);
                            notesList.add(note);
                        }
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                    } else {
                        updateEmptyView();
                    }
                });
    }

    private void updateEmptyView() {
        if (notesList.isEmpty()) {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
            binding.recyclerViewNotes.setVisibility(View.GONE);
        } else {
            binding.textViewEmpty.setVisibility(View.GONE);
            binding.recyclerViewNotes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemNoteBinding itemBinding = ItemNoteBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new NoteViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Note note = notesList.get(position);
            holder.bind(note);
        }

        @Override
        public int getItemCount() {
            return notesList.size();
        }

        class NoteViewHolder extends RecyclerView.ViewHolder {
            private final ItemNoteBinding binding;

            NoteViewHolder(ItemNoteBinding binding) {
                super(binding.getRoot());
                this.binding = binding;

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Note note = notesList.get(position);
                        Bundle bundle = new Bundle();
                        bundle.putString("noteId", note.getId());
                        NavHostFragment.findNavController(NotesListFragment.this)
                                .navigate(R.id.action_NotesListFragment_to_NoteEditorFragment, bundle);
                    }
                });
            }

            void bind(Note note) {
                binding.textViewTitle.setText(note.getTitle());
                binding.textViewContent.setText(note.getContent());
                binding.textViewTimestamp.setText(dateFormat.format(note.getDate()));
            }
        }
    }
}