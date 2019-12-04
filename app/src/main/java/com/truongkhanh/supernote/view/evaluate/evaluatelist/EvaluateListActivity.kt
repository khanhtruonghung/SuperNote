package com.truongkhanh.supernote.view.evaluate.evaluatelist

import android.os.Bundle
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseNoAppBarActivity
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.utils.EVALUATE_BUNDLE
import com.truongkhanh.supernote.view.evaluate.createevaluate.CreateEvaluateActivity
import org.jetbrains.anko.intentFor

class EvaluateListActivity : BaseNoAppBarActivity(), EvaluateListFragment.NavigationListener {

    private lateinit var evaluateListFragment: EvaluateListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            evaluateListFragment =
                EvaluateListFragment.getInstance()
            if (intent != null)
                evaluateListFragment.arguments = intent.extras
            setFragment(evaluateListFragment)
        }
    }

    private fun setFragment(evaluateListFragment: EvaluateListFragment) {
        replaceFragment(R.id.fragmentContainer, evaluateListFragment)
    }

    override fun navigateToCreateEvaluate(evaluate: Evaluate) {
        startActivity(intentFor<CreateEvaluateActivity>(EVALUATE_BUNDLE to evaluate))
    }
}