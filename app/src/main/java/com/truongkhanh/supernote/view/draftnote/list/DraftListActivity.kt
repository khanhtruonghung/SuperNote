package com.truongkhanh.supernote.view.draftnote.list

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.utils.DRAFT_NOTE_BUNDLE
import com.truongkhanh.supernote.view.draftnote.create.CreateNoteActivity
import org.jetbrains.anko.intentFor

class DraftListActivity : BaseNoAppBarActivity(), DraftListFragment.NavigationListener {
    private lateinit var draftListFragment: DraftListFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            draftListFragment = DraftListFragment.getInstance()
            if (intent != null)
                draftListFragment.arguments = intent.extras
            setFragment(draftListFragment)
        }
    }

    private fun setFragment(draftListFragment: DraftListFragment) {
        replaceFragment(R.id.fragmentContainer, draftListFragment)
    }

    override fun navigateToCreateNote(draftNote: DraftNote?) {
        startActivity(intentFor<CreateNoteActivity>(DRAFT_NOTE_BUNDLE to draftNote))
    }
}