package io.rapidpro.surveyor.extend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.extend.StaticMethods;
import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;
import me.myatminsoe.mdetect.MDetect;
import me.myatminsoe.mdetect.Rabbit;

import static io.rapidpro.surveyor.extend.StaticMethods.getMD5;


/**
 * Custom Adapter for Story List Display
 * Used in StoryListActivity -> StoriesListFragment
 */
public class CustomAdapterStories extends RecyclerView.Adapter<CustomAdapterStories.ViewHolder> {

    private Context context;
    private List<StoriesLocal> stories;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private String lang_code;

    public CustomAdapterStories(Context context, List<StoriesLocal> stories, String lang_code) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.stories = stories;
        this.lang_code = lang_code;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.v1_recycler_item_stories, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.storyAuthorName.setText(stories.get(position).getAuthor());
        holder.storyDate.setText(stories.get(position).getCreated_at());

        if(lang_code.equals("bn")){
            holder.storyTitle.setText(stories.get(position).getTitle_bn());
            if(stories.get(position).getSubtitle_bn() != null){
                holder.storyBody.setText(stories.get(position).getSubtitle_bn());
            }else{
                String x = stories.get(position).getBody_bn();
                if(x.length() > 200){
                    x = x.substring(0, 200) + "...";
                }
                holder.storyBody.setText(x);
            }
        }else if(lang_code.equals("my")){

            if (!MDetect.INSTANCE.isUnicode() && StaticMethods.displayZawgyi()){
                // Convert to Zawgyi
                holder.storyTitle.setText(Rabbit.uni2zg(stories.get(position).getTitle_my()));
                if(stories.get(position).getSubtitle_my() != null){
                    holder.storyBody.setText(Rabbit.uni2zg(stories.get(position).getSubtitle_my()));
                }else{
                    String x = stories.get(position).getBody_my();
                    if(x.length() > 200){
                        x = x.substring(0, 200) + "...";
                    }
                    holder.storyBody.setText(Rabbit.uni2zg(x));
                }

            } else {
                holder.storyTitle.setText(stories.get(position).getTitle_my());
                if(stories.get(position).getSubtitle_my() != null){
                    holder.storyBody.setText(stories.get(position).getSubtitle_my());
                }else{
                    String x = stories.get(position).getBody_my();
                    if(x.length() > 200){
                        x = x.substring(0, 200) + "...";
                    }
                    holder.storyBody.setText(x);
                }
            }


        } else {
            holder.storyTitle.setText(stories.get(position).getTitle_en());
            if(stories.get(position).getSubtitle_en() != null){
                holder.storyBody.setText(stories.get(position).getSubtitle_en());
            }else{
                String x = stories.get(position).getBody_en();
                if(x.length() > 200){
                    x = x.substring(0, 200) + "...";
                }
                holder.storyBody.setText(x);
            }
        }

        holder.storySeeMore.setText(R.string.v1_story_see_more);

        if(stories.get(position).getContent_image() != null){
            String imageURL = stories.get(position).getContent_image();
            String file_path = "";

            if(!imageURL.equals("")){
                file_path = context.getFilesDir() + "/story_image_" + getMD5(imageURL);
            }else{
                file_path = "file:///android_asset/images/no-image.png";
            }

            Glide.with(context)
                    .load(file_path)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .into(holder.storyImage);
        }

    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView storyCard;
        TextView storyAuthorName;
        CircleImageView storyAuthorImage;
        TextView storyDate;
        ImageView storyImage;
        TextView storyTitle;
        TextView storyBody;
        TextView storySeeMore;

        ViewHolder(View itemView) {
            super(itemView);
            storyCard = itemView.findViewById(R.id.story_card);
            storyAuthorName = itemView.findViewById(R.id.storyAuthorName);
            storyAuthorImage = itemView.findViewById(R.id.storyAuthorImage);
            storyDate = itemView.findViewById(R.id.storyDate);
            storyImage = itemView.findViewById(R.id.storyContentImage);
            storyTitle = itemView.findViewById(R.id.storyTitle);
            storyBody = itemView.findViewById(R.id.storyBody);
            storySeeMore = itemView.findViewById(R.id.storySeeMore);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
