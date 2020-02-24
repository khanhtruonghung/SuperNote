package com.truongkhanh.supernote.view.planning

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.utils.DRAFT_NOTE_BUNDLE
import com.truongkhanh.supernote.view.draftnote.create.CreateNoteActivity
import org.jetbrains.anko.intentFor

class PlanningActivity : BaseNoAppBarActivity(), PlanningFragment.NavigationListener {

    private lateinit var planningFragment: PlanningFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            planningFragment = PlanningFragment.getInstance()
            if (intent != null) {
                planningFragment.arguments = intent.extras
            }
            setFragment(planningFragment)
        }
    }

    private fun setFragment(planningFragment: PlanningFragment) {
        replaceFragment(R.id.fragmentContainer, planningFragment)
    }

    override fun navigateToCreateNote(draftNote: DraftNote?) {
        startActivity(intentFor<CreateNoteActivity>(DRAFT_NOTE_BUNDLE to draftNote))
    }
}