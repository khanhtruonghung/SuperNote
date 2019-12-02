package com.truongkhanh.supernote.view.createtodo

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity

class CreateTodoActivity : BaseNoAppBarActivity() {
    private lateinit var createTodoFragment: CreateTodoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            createTodoFragment = CreateTodoFragment.getInstance()
            if(intent != null)
                createTodoFragment.arguments = intent.extras
            replaceFragment(R.id.fragmentContainer, createTodoFragment)
        }
    }
}