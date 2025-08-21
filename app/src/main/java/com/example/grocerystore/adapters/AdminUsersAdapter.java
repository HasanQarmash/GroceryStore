package com.example.grocerystore.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerystore.R;
import com.example.grocerystore.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDeleteUser(User user);
        void onEditUser(User user);
        void onViewUser(User user);
    }

    public AdminUsersAdapter(OnUserActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateUsers(List<User> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView emailText;
        private TextView roleText;
        private ImageButton viewButton;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.user_name_text);
            emailText = itemView.findViewById(R.id.user_email_text);
            roleText = itemView.findViewById(R.id.user_role_text);
            viewButton = itemView.findViewById(R.id.view_user_button);
            editButton = itemView.findViewById(R.id.edit_user_button);
            deleteButton = itemView.findViewById(R.id.delete_user_button);
        }

        public void bind(User user) {
            nameText.setText(user.getFirstName() + " " + user.getLastName());
            emailText.setText(user.getEmail());
            roleText.setText(user.getRole().toUpperCase());

            // Set role text color
            if ("admin".equals(user.getRole())) {
                roleText.setTextColor(itemView.getContext().getColor(R.color.primary));
            } else {
                roleText.setTextColor(itemView.getContext().getColor(R.color.secondary));
            }

            viewButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewUser(user);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUser(user);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUser(user);
                }
            });

            // Don't allow deleting admin users for safety
            if ("admin".equals(user.getRole())) {
                deleteButton.setVisibility(View.GONE);
            } else {
                deleteButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
