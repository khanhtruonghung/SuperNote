package com.truongkhanh.supernote.view.evaluate.createevaluate

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity

class CreateEvaluateActivity : BaseNoAppBarActivity() {

    private lateinit var createEvaluateFragment: CreateEvaluateFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            createEvaluateFragment = CreateEvaluateFragment.getInstance()
            if (intent != null) {
                createEvaluateFragment.arguments = intent.extras
            }
            setFragment(createEvaluateFragment)
        }
    }

    private fun setFragment(createEvaluateFragment: CreateEvaluateFragment) {
        replaceFragment(R.id.fragmentContainer, createEvaluateFragment)
    }
}