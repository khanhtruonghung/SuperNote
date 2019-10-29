package com.truongkhanh.supernote.view.mainhome

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity

class HomeActivity : BaseNoAppBarActivity() {

    private lateinit var homeFragment: HomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            homeFragment = HomeFragment.getInstance()
            if (intent != null)
                homeFragment.arguments = intent.extras
            setFragment(homeFragment)
        }
    }

    private fun setFragment(homeFragment: HomeFragment) {
        replaceFragment(R.id.fragmentContainer, homeFragment)
    }
}
