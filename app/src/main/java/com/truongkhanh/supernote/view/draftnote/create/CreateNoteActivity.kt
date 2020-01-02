package com.truongkhanh.supernote.view.draftnote.create

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity

class CreateNoteActivity : BaseNoAppBarActivity() {
    private lateinit var createNoteFragment: CreateNoteFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            createNoteFragment = CreateNoteFragment.getInstance()
            if (intent != null)
                createNoteFragment.arguments = intent.extras
            setFragment(createNoteFragment)
        }
    }

    private fun setFragment(createNoteFragment: CreateNoteFragment) {
        replaceFragment(R.id.fragmentContainer, createNoteFragment)
    }
}