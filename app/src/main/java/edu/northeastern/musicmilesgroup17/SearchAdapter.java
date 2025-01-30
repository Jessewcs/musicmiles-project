package edu.northeastern.musicmilesgroup17;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private List<SearchResult> searchResults = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(SearchResult result);
    }

    public SearchAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_item, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        SearchResult result = searchResults.get(position);
        holder.titleText.setText(result.getTitle());
        holder.subtitleText.setText(result.getSubtitle());

        // Set icon based on type
        if (result.getType() == SearchResult.Type.SONG) {
            holder.typeIcon.setImageResource(R.drawable.ic_music_note);
        } else {
            holder.typeIcon.setImageResource(R.drawable.ic_profile);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(result));
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public void updateResults(List<SearchResult> newResults) {
        this.searchResults = newResults;
        notifyDataSetChanged();
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView typeIcon;
        TextView titleText;
        TextView subtitleText;

        SearchViewHolder(View itemView) {
            super(itemView);
            typeIcon = itemView.findViewById(R.id.typeIcon);
            titleText = itemView.findViewById(R.id.titleText);
            subtitleText = itemView.findViewById(R.id.subtitleText);
        }
    }
}