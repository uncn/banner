package com.sunzn.banner.sample

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.sunzn.banner.library.Banner
import java.util.*

class MainActivity : AppCompatActivity() {

    private var banner: Banner<Bean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        banner = findViewById(R.id.banner)
        lifecycle.addObserver(banner!!)

        banner!!.setDefaultGainColor(Color.RED)
        banner!!.setIndicatorGravity(GravityCompat.END)
        banner!!.setIndicatorMargin(15)

        val packs = ArrayList<Bean>()
        packs.add(Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0D/01/ChMkJ1gq00WIXw_GAA47r_8gjqgAAXxJAH8qOMADjvH566.jpg"))
        packs.add(Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0B/0D/ChMkJ1e9jHqIWT4CAA2dKPU9Js8AAUsZgMf8mkADZ1A116.jpg"))
        packs.add(Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0B/0D/ChMkJle9jIGIMgtdAAYnBOEz3LAAAUsZwPgFgYABicc437.jpg"))
        packs.add(Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0F/0A/ChMkJleZ8-iIBbFBAAVrdxItOlQAAT76QAFx7oABWuP846.jpg"))
        packs.add(Bean("http://desk.fd.zol-img.com.cn/t_s1024x768c5/g5/M00/0B/04/ChMkJ1bG5kyIcwkXAAsM0s9DJzoAAKsAwJB9ncACwzq207.jpg"))

        banner!!.setBannerData(packs)
        banner!!.setOnItemClickListener(object : Banner.OnItemClickListener<Bean> {
            override fun onItemClick(position: Int, item: Bean) {
                Toast.makeText(this@MainActivity, "position = $position", Toast.LENGTH_SHORT).show()
            }
        })
        banner!!.setOnItemBindListener(object : Banner.OnItemBindListener<Bean> {
            override fun onItemBind(position: Int, item: Bean, view: ImageView) {
                Glide.with(applicationContext).load(item.url).into(view)
            }
        })
    }

    private inner class Bean internal constructor(internal var url: String)

    override fun onResume() {
        super.onResume()
        banner!!.setPlaying(true)
    }

    override fun onPause() {
        super.onPause()
        banner!!.setPlaying(false)
    }

}
