package com.sunzn.banner.sample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sunzn.banner.library.Banner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Banner<Bean> banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        String s = "R2008060360000003";
//
//        String c = s.substring(10);
//
//        int x = Integer.parseInt("R2008060360000003".substring(10));

        Log.e("BBB",String.format("%07d", Integer.parseInt("R2008060360000109".substring(10)) + 1));

        banner = findViewById(R.id.banner);
        getLifecycle().addObserver(banner);

        banner.setDefaultGainColor(Color.RED);
        banner.setIndicatorGravity(GravityCompat.END);
        banner.setIndicatorMargin(15);

        final List<Bean> packs = new ArrayList<>();
        packs.add(new Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0D/01/ChMkJ1gq00WIXw_GAA47r_8gjqgAAXxJAH8qOMADjvH566.jpg"));
        packs.add(new Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0B/0D/ChMkJ1e9jHqIWT4CAA2dKPU9Js8AAUsZgMf8mkADZ1A116.jpg"));
        packs.add(new Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0B/0D/ChMkJle9jIGIMgtdAAYnBOEz3LAAAUsZwPgFgYABicc437.jpg"));
        packs.add(new Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0F/0A/ChMkJleZ8-iIBbFBAAVrdxItOlQAAT76QAFx7oABWuP846.jpg"));
        packs.add(new Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0B/04/ChMkJ1bG5kyIcwkXAAsM0s9DJzoAAKsAwJB9ncACwzq207.jpg"));

        banner.setBannerData(packs);
        banner.setOnItemClickListener(new Banner.OnItemClickListener<Bean>() {
            @Override
            public void onItemClick(int position, Bean item) {
                Toast.makeText(MainActivity.this, "position = " + position, Toast.LENGTH_SHORT).show();
            }
        });
        banner.setOnItemBindListener(new Banner.OnItemBindListener<Bean>() {
            @Override
            public void onItemBind(int position, Bean item, ImageView view) {
                Glide.with(getApplicationContext()).load(item.getUrl()).into(view);
            }
        });
    }

    private class Bean {

        String url;

        Bean(String url) {
            this.url = url;
        }

        String getUrl() {
            return url;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        banner.setPlaying(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        banner.setPlaying(false);
    }

}
