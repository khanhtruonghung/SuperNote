package com.truongkhanh.supernote.view.evaluate.createevaluate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.truongkhanh.musicapplication.base.BaseFragment
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.view.evaluate.createevaluate.adapter.EvaluateTodoDetailAdapter
import kotlinx.android.synthetic.main.fragment_create_evaluate.*

class CreateEvaluateFragment : BaseFragment() {

    companion object {
        fun getInstance() = CreateEvaluateFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_evaluate, container, false)
    }

    private lateinit var newTodoAdapter: EvaluateTodoDetailAdapter
    private lateinit var previousTodoAdapter: EvaluateTodoDetailAdapter

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        bindingViewModel()
        getDataFromBundle()
        initRecyclerView()
    }

    private fun bindingViewModel() {

    }

    private fun getDataFromBundle() {
        arguments?.let{
            initViewInformation()
        }
    }

    private fun initViewInformation() {

    }

    private fun initRecyclerView() {
        newTodoRecyclerView()
        previousTodoRecyclerView()
    }

    private fun newTodoRecyclerView() {
        val context = context ?: return
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvNewTodos.layoutManager = layoutManager
        newTodoAdapter = EvaluateTodoDetailAdapter(context) { evaluateTodo ->
            //TODO : Item click event
        }
        rvNewTodos.adapter = newTodoAdapter
    }

    private fun previousTodoRecyclerView() {
        val context = context ?: return
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvPreviousTodos.layoutManager = layoutManager
        previousTodoAdapter = EvaluateTodoDetailAdapter(context) {
            //TODO : Item Click event
        }
        rvPreviousTodos.adapter = previousTodoAdapter
    }
}