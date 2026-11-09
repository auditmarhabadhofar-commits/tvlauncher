package com.ahmed.tvlauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Stage 1: minimal launcher to prove this Fire TV accepts a third-party HOME app. */
public class MainActivity extends Activity {

    static class App {
        String label;
        Drawable icon;
        Intent intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(48), dp(32), dp(48), dp(16));

        TextView title = new TextView(this);
        title.setText("Launcher test");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setPadding(0, 0, 0, dp(16));
        root.addView(title);

        RecyclerView rv = new RecyclerView(this);
        rv.setLayoutManager(new GridLayoutManager(this, 4));
        rv.setAdapter(new Adapter(loadApps()));
        root.addView(rv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root);
    }

    @Override
    public void onBackPressed() {
        // A home screen has nowhere to go back to.
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    private List<App> loadApps() {
        PackageManager pm = getPackageManager();
        Intent q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> found = pm.queryIntentActivities(q, 0);
        if (found.isEmpty()) {
            q = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            found = pm.queryIntentActivities(q, 0);
        }
        List<App> apps = new ArrayList<>();
        for (ResolveInfo ri : found) {
            if (ri.activityInfo.packageName.equals(getPackageName())) continue;
            App a = new App();
            a.label = String.valueOf(ri.loadLabel(pm));
            Drawable banner = ri.activityInfo.loadBanner(pm);
            a.icon = banner != null ? banner : ri.loadIcon(pm);
            a.intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(ri.activityInfo.packageName, ri.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            apps.add(a);
        }
        Collections.sort(apps, new Comparator<App>() {
            @Override
            public int compare(App x, App y) {
                return x.label.compareToIgnoreCase(y.label);
            }
        });
        return apps;
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<App> apps;

        Adapter(List<App> apps) {
            this.apps = apps;
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView label;

            VH(LinearLayout tile, ImageView icon, TextView label) {
                super(tile);
                this.icon = icon;
                this.label = label;
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout tile = new LinearLayout(parent.getContext());
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER_HORIZONTAL);
            tile.setFocusable(true);
            tile.setClickable(true);
            tile.setPadding(dp(8), dp(8), dp(8), dp(8));
            tile.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            ImageView icon = new ImageView(parent.getContext());
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tile.addView(icon, new LinearLayout.LayoutParams(dp(160), dp(90)));

            TextView label = new TextView(parent.getContext());
            label.setTextColor(Color.WHITE);
            label.setTextSize(14);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            tile.addView(label, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            tile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    v.setBackgroundColor(hasFocus ? 0x44FFFFFF : Color.TRANSPARENT);
                    v.animate().scaleX(hasFocus ? 1.08f : 1f).scaleY(hasFocus ? 1.08f : 1f)
                            .setDuration(120).start();
                }
            });
            return new VH(tile, icon, label);
        }

        @Override
        public void onBindViewHolder(VH h, int position) {
            final App a = apps.get(position);
            h.icon.setImageDrawable(a.icon);
            h.label.setText(a.label);
            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(a.intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return apps.size();
        }
    }
}
