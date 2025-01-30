package edu.northeastern.musicmilesgroup17;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Map<String, String> friendsMap;
    private List<String> usernames;
    private List<String> filteredUsernames;

    public UserAdapter(Map<String, String> friendsMap) {
        this.friendsMap = friendsMap;
        this.usernames = new ArrayList<>(friendsMap.keySet());
        this.filteredUsernames = new ArrayList<>(usernames);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String username = filteredUsernames.get(position);
        String fullName = friendsMap.get(username);

        holder.fullNameText.setText(fullName);
        holder.usernameText.setText("@" + username);
    }

    @Override
    public int getItemCount() {
        return filteredUsernames.size();
    }

    public void filter(String query) {
        filteredUsernames.clear();
        if (query.isEmpty()) {
            filteredUsernames.addAll(usernames);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (String username : usernames) {
                String fullName = friendsMap.get(username);
                if (username.toLowerCase().contains(lowerCaseQuery) ||
                        (fullName != null && fullName.toLowerCase().contains(lowerCaseQuery))) {
                    filteredUsernames.add(username);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateData(Map<String, String> newFriendsMap) {
        this.friendsMap = newFriendsMap;
        this.usernames = new ArrayList<>(newFriendsMap.keySet());
        this.filteredUsernames = new ArrayList<>(usernames);
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameText;
        TextView usernameText;

        UserViewHolder(View itemView) {
            super(itemView);
            fullNameText = itemView.findViewById(R.id.fullNameText);
            usernameText = itemView.findViewById(R.id.usernameText);
        }
    }
}