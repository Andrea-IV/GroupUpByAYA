package com.andrea.groupup

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.andrea.groupup.Adapters.PagerViewAdapter

class DetailsActivity : AppCompatActivity(){

    private lateinit var groupButton: ImageButton
    private lateinit var chatMapButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var exploreButton: ImageButton
    private lateinit var backButton: ImageButton

    private lateinit var mViewPager: ViewPager
    private lateinit var mPagerAdapter: PagerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        mViewPager = findViewById(R.id.mViewPager)

        groupButton = findViewById(R.id.groupButton)
        chatMapButton = findViewById(R.id.chatButton)
        calendarButton = findViewById(R.id.dateButton)
        exploreButton = findViewById(R.id.pinButton)
        backButton = findViewById(R.id.backButton)

        mPagerAdapter = PagerViewAdapter(supportFragmentManager)
        mViewPager.adapter = mPagerAdapter
        mViewPager.offscreenPageLimit = 5

        groupButton.setOnClickListener {
            mViewPager.currentItem = 0
        }

        chatMapButton.setOnClickListener {
            mViewPager.currentItem = 1
        }

        calendarButton.setOnClickListener {
            mViewPager.currentItem = 2
        }

        exploreButton.setOnClickListener {
            mViewPager.currentItem = 3
        }

        backButton.setOnClickListener {
            finish()
        }

        mViewPager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                changingTabs(position)
            }
        })

        mViewPager.currentItem = 0
        groupButton.setImageResource(R.drawable.ic_group_blue)
    }

    private fun changingTabs(position: Int) {
        if(position == 0){
            groupButton.setImageResource(R.drawable.ic_group_blue)
            chatMapButton.setImageResource(R.drawable.ic_chat_black)
            calendarButton.setImageResource(R.drawable.ic_today_black)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_black)
        }
        if(position == 1){
            groupButton.setImageResource(R.drawable.ic_group_black)
            chatMapButton.setImageResource(R.drawable.ic_chat_blue)
            calendarButton.setImageResource(R.drawable.ic_today_black)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_black)
        }
        if(position == 2){
            groupButton.setImageResource(R.drawable.ic_group_black)
            chatMapButton.setImageResource(R.drawable.ic_chat_black)
            calendarButton.setImageResource(R.drawable.ic_today_blue)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_black)
        }
        if(position == 3){
            groupButton.setImageResource(R.drawable.ic_group_black)
            chatMapButton.setImageResource(R.drawable.ic_chat_black)
            calendarButton.setImageResource(R.drawable.ic_today_black)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_blue)
        }
    }
}
