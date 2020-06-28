package io.rapidpro.surveyor.activity;

import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.skydoves.powermenu.PowerMenuItem;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.extend.adapter.ViewPagerAdapter;
import io.rapidpro.surveyor.fragment.SettingsFragment;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * Activity for modifying app settings
 */
public class SettingsActivity extends BaseActivity {

    public boolean requireLogin() {
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v1_activity_settings);

        //FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //ft.replace(android.R.id.content, new SettingsFragment()).commit();

        ViewPagerAdapter viewPagerAdapter;
        ViewPager viewPager;
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPagerAdapter.addFragment(new SettingsFragment(), "Settings");
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);

        menuItems.add(new PowerMenuItem(getString(R.string.action_bug_report)));
        menuItems.add(new PowerMenuItem(getString(R.string.action_settings)));
        menuItems.add(new PowerMenuItem(getString(R.string.action_logout)));
        initHeaderBar();
    }
}
