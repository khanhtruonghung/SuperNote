package com.truongkhanh.supernote.view.evaluate.evaluatelist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.model.enumclass.DAY
import com.truongkhanh.supernote.model.enumclass.MONTH
import com.truongkhanh.supernote.model.enumclass.WEEK
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.evaluate.evaluatelist.adapter.EvaluateListAdapter
import kotlinx.android.synthetic.main.fragment_evaluate_list.*
import kotlinx.android.synthetic.main.layout_toolbar_light.*
import org.jetbrains.anko.design.snackbar
import java.util.concurrent.TimeUnit

class EvaluateListFragment : BaseFragment() {

    companion object {
        fun getInstance() =
            EvaluateListFragment()
    }

    interface NavigationListener {
        fun navigateToCreateEvaluate(evaluate: Evaluate)
    }

    private lateinit var evaluateListViewModel: EvaluateListViewModel
    private val bag = DisposeBag(this)
    private lateinit var evaluateListAdapter: EvaluateListAdapter
    private lateinit var navigationListener: NavigationListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_evaluate_list, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationListener)
            navigationListener = context
    }

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        initViewInformation()
        initClickListener()
        initEvaluateListRecyclerView()
        bindingViewModel()
    }

    private fun initViewInformation() {
        tvCurrentDate.text = context?.getString(R.string.lbl_evaluate_list)
        btnToday.visibility = View.GONE
    }

    private fun bindingViewModel() {

        val activity = activity ?: return
        evaluateListViewModel = ViewModelProviders
            .of(activity, getEvaluateViewModelFactory(activity))
            .get(EvaluateListViewModel::class.java)

        evaluateListViewModel.evaluateList.observe(this, Observer { evaluateList ->
            if (!evaluateList.isNullOrEmpty()) {
                evaluateListAdapter.submitList(evaluateList)
                setVisibilityEmptyView(false)
            } else {
                setVisibilityEmptyView(true)
            }
        })
        evaluateListViewModel.showMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let{message ->
                view?.snackbar(message)
            }
        })
        evaluateListViewModel.navigateToCreateEvaluate.observe(this, Observer {event ->
            event.getContentIfNotHandled()?.let { evaluate ->
                navigationListener.navigateToCreateEvaluate(evaluate)
            }
        })
        evaluateListViewModel.evaluateType.observe(this, Observer {type ->
            tvEvaluateType.text = getEvaluateTypeText(type)
        })
    }

    private fun getEvaluateTypeText(type: Int): String? {
        return when (type) {
            DAY -> context?.getString(R.string.lbl_full_day)
            WEEK -> context?.getString(R.string.lbl_full_week)
            MONTH -> context?.getString(R.string.lbl_full_month)
            else -> NULL_STRING
        }
    }

    private fun setVisibilityEmptyView(enable: Boolean) {
        rlEmptyView.visibility = getEnable(enable)
    }

    private fun initEvaluateListRecyclerView() {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvEvaluateList.layoutManager = layoutManager
        val context = context ?: return
        evaluateListAdapter = EvaluateListAdapter(context) {evaluate ->
            navigationListener.navigateToCreateEvaluate(evaluate)
        }
        rvEvaluateList.adapter = evaluateListAdapter
    }

    private fun initClickListener() {
        RxView.clicks(fbCreateTodo)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                evaluateListViewModel.getEvaluate()
            }.disposedBy(bag)
        RxView.clicks(btnNavigation)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
        RxView.clicks(evaluateType)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                evaluateListViewModel.changeEvaluateType()
            }.disposedBy(bag)
    }
}