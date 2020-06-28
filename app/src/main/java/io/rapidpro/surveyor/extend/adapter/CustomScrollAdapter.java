package io.rapidpro.surveyor.extend.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import io.rapidpro.surveyor.R;

public class CustomScrollAdapter extends RecyclerView.Adapter<CustomScrollAdapter.ViewHolder> {

    private static ClickListener clickListener;
    private List<DashboardList_RV> data;

    public CustomScrollAdapter(List<DashboardList_RV> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.v1_item_dashboard_card_new, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Animate Here
        holder.activityName.setText(data.get(position).name);
        holder.cardImage.setImageResource(data.get(position).cardImage);
        holder.bgShadow.setImageResource(data.get(position).bgShadow);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView activityName;
        private ImageView cardImage;
        private ImageView bgShadow;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            activityName = itemView.findViewById(R.id.activityName);
            cardImage = itemView.findViewById(R.id.cardImage);
            bgShadow = itemView.findViewById(R.id.bg_shadow);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        CustomScrollAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }
}