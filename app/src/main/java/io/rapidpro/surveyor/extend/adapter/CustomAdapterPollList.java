package io.rapidpro.surveyor.extend.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.extend.StaticMethods;
import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

public class CustomAdapterPollList extends RecyclerView.Adapter<CustomAdapterPollList.ViewHolder> {

    private List<String> mData, mData2, mData3;
    private boolean isCategory = false;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int ColorPosition = -1;
    int delay = 200;
    Context context;
    SurveyorApplication surveyorApplication;

    public CustomAdapterPollList(Context context, List<String> data, List<String> data2, List<String> data3, boolean isCategory, int ColorPos) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mData2 = data2;
        this.mData3 = data3;
        this.isCategory = isCategory;
        this.ColorPosition = ColorPos;
        this.context = context;
    }

    public CustomAdapterPollList(SurveyorApplication surveyor, Context context, List<String> data, List<String> data2, List<String> data3, boolean isCategory, int ColorPos) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mData2 = data2;
        this.mData3 = data3;
        this.isCategory = isCategory;
        this.ColorPosition = ColorPos;
        this.context = context;
        this.surveyorApplication = surveyor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(isCategory){
            View view = mInflater.inflate(R.layout.v1_recycler_item_opinions_category, parent, false);
            return new ViewHolder(view);
        }else{
            View view = mInflater.inflate(R.layout.v1_recycler_item_opinions, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        String custom_data = mData.get(position);
        String lang_code = SurveyorApplication.get().getPreferences().getString(SurveyorPreferences.LANG_CODE, "en");
        if(lang_code.equals("my") && !MDetect.INSTANCE.isUnicode() && !StaticMethods.disableZawgyi()) {
            // Place Zawgyi
            custom_data = Rabbit.uni2zg(custom_data);
        }
        holder.myTextView.setText(custom_data);

        if(position == 0){
            String myDate = StaticMethods.getLocalUpdateDate(surveyorApplication,"ureport_offline_last_updated_local");
            String myText = "";

            if(myDate.equals("")){
                myText = "Pull down to refresh";
            }else{
                myText = "Last updated: " + myDate + "\n" + "Pull down to refresh";
            }
            holder.ureportLastUpdate.setText(myText);
            holder.ureportLastUpdate.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView ureportLastUpdate;
        TextView myTextView;
        View left_color;

        View color_dot;
        ImageView color_arrow;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.topic_title);
            ureportLastUpdate = itemView.findViewById(R.id.ureport_last_update);


            if(ColorPosition != -1){
                color_dot = itemView.findViewById(R.id.colorDot);
                color_arrow = itemView.findViewById(R.id.colorArrow);
            }else{
                left_color = itemView.findViewById(R.id.left_color);
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public String getItem(int id) {
        return mData.get(id);
    }
    public String getId(int id) {
        return mData2.get(id);
    }

    public String getDate(int id) {
        return mData3.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}