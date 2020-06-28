package io.rapidpro.surveyor.extend;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.activity.BaseActivity;

import static io.rapidpro.surveyor.extend.StaticMethods.playNotification;

public class StoriesListActivity extends BaseActivity {

    ViewGroup headerLayout;
    CardView storyList;
    View bgColor;
    ImageView backButton;
    boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_stories_new);
        getWindow().setEnterTransition(null);

        isOpen = true;

        headerLayout = findViewById(R.id.headerLayout);
        storyList = findViewById(R.id.storyList);
        bgColor = findViewById(R.id.bgColor);
        backButton = findViewById(R.id.backButton);

        ((TextView) findViewById(R.id.activityName)).setText(R.string.v1_stories);

        ObjectAnimator.ofFloat(storyList, "alpha",  0, 1f).setDuration(500).start();
        ObjectAnimator.ofFloat(bgColor, "translationY", -500, 0).setDuration(1000).start();
        ObjectAnimator.ofFloat(storyList, "translationY", 1000, 0).setDuration(1000).start();
        ObjectAnimator.ofFloat(backButton, "translationX", -200, 0).setDuration(1000).start();

        backButton.setOnClickListener(view -> onBackPressed());

        isOpen = true;
    }

    @Override
    public void onBackPressed() {
        if(isOpen){ isOpen = false; }else{ return; }
        playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_no, findViewById(R.id.backButton));
        headerLayout.setBackgroundColor(Color.parseColor("#00000000"));
        ObjectAnimator.ofFloat(storyList, "alpha",  1f, 0).setDuration(750).start();
        ObjectAnimator.ofFloat(bgColor, "translationY", 0, -500).setDuration(500).start();
        ObjectAnimator.ofFloat(storyList, "translationY", 0, 1000).setDuration(750).start();
        ObjectAnimator.ofFloat(backButton, "translationX", 0, -200).setDuration(1000).start();
        super.onBackPressed();
    }

    public void beginTransition(View view, int story_id){

        playNotification(getSurveyor(), getApplicationContext(), R.raw.button_click_yes, view);

        Intent intent = new Intent(this, StoriesActivity.class);
        intent.putExtra("STORY_ID", story_id);
        startActivity(intent);
    }

    @Override
    public boolean requireLogin() {
        return false;
    }
}
