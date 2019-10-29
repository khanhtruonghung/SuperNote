package com.truongkhanh.supernote.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.truongkhanh.musicapplication.base.BaseActivity
import com.truongkhanh.supernote.R
import kotlinx.android.synthetic.main.activity_appbar_layout.*

open class BaseAppBarActivity: BaseActivity() {

    private var mToolbar: Toolbar? = null
    private var mActionBar: ActionBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appbar_layout)
        mToolbar = toolbar
        setSupportActionBar(mToolbar)
        mActionBar = supportActionBar
    }

    fun setToolbarTitle(title: String?) {
        supportActionBar!!.title = title
    }

    fun setDisplayHomeAsUpEnabled(enabled: Boolean) {
        supportActionBar!!.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun setHomeButtonEnabled(enabled: Boolean) {
        supportActionBar!!.setHomeButtonEnabled(enabled)
    }

    fun hideActionBar() {
        appbarLayout.visibility = View.GONE
    }
}