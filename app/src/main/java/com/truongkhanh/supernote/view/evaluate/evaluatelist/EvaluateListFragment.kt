package com.truongkhanh.supernote.view.evaluate.evaluatelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.musicapplication.base.BaseFragment
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.disposedBy
import com.truongkhanh.supernote.utils.getEvaluateViewModelFactory
import com.truongkhanh.supernote.view.evaluate.evaluatelist.adapter.EvaluateListAdapter
import kotlinx.android.synthetic.main.fragment_evaluate_list.*

class EvaluateListFragment : BaseFragment() {

    companion object {
        fun getInstance() =
            EvaluateListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_evaluate_list, container, false)
    }

    private lateinit var evaluateListViewModel: EvaluateListViewModel
    private val bag = DisposeBag(this)
    private lateinit var evaluateListAdapter: EvaluateListAdapter

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        bindingViewModel()
        initEvaluateListRecyclerView()
        initClickListener()
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        evaluateListViewModel = ViewModelProviders
            .of(activity, getEvaluateViewModelFactory(activity))
            .get(EvaluateListViewModel::class.java)

        evaluateListViewModel.evaluateList.observe(this, Observer { evaluateList ->
            evaluateListAdapter.submitList(evaluateList)
        })
    }

    private fun initEvaluateListRecyclerView() {
        val context = context ?: return
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvEvaluateList.layoutManager = layoutManager
        evaluateListAdapter = EvaluateListAdapter(context) {evaluate ->
            //TODO : Open evaluate detail screen
        }
        rvEvaluateList.adapter = evaluateListAdapter
    }

    private fun initClickListener() {
        RxView.clicks(fbCreateEvaluate)
            .subscribe {
                //TODO : Open create evaluate screen
            }.disposedBy(bag)
    }
}