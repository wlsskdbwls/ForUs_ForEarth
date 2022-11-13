package com.example.forusforearth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.forusforearth.adapter.ImageViewPagerAdapter
import com.example.forusforearth.databinding.ActivityImageListBinding

class ImageListActivity : AppCompatActivity() {

    companion object{
        private const val URI_LIST_KEY = "uriList"

        fun newIntent(activity: Activity, uriList:List<Uri>)=
            Intent(activity,ImageListActivity::class.java).apply {
                putExtra(URI_LIST_KEY,ArrayList<Uri>().apply { uriList.forEach{add(it)} })
            }
    }

    private lateinit  var binding: ActivityImageListBinding
    private lateinit var imageViewPagerAdapter: ImageViewPagerAdapter

    private val uriList by lazy <List<Uri>>{intent.getParcelableArrayListExtra(URI_LIST_KEY)!!  }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityImageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews(){
        setSupportActionBar(binding.toolbar)
        setupImageList()
    }

    private fun setupImageList()=with(binding){
        if (::imageViewPagerAdapter.isInitialized.not()) {
            imageViewPagerAdapter = ImageViewPagerAdapter(uriList)
        }
        imageViewPager.adapter=imageViewPagerAdapter
        indicator.setViewPager(imageViewPager)
        imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                toolbar.title=getString(R.string.image_page,position + 1,imageViewPagerAdapter.uriList.size)
            }

        })

    }

}
