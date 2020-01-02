package com.truongkhanh.supernote.view.evaluate.createevaluate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.model.Evaluate
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.model.enumclass.DAY
import com.truongkhanh.supernote.model.enumclass.MONTH
import com.truongkhanh.supernote.model.enumclass.WEEK
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.dialog.bottomsheet.DetailTodoDialogFragment
import com.truongkhanh.supernote.view.evaluate.createevaluate.adapter.EvaluateTodoDetailAdapter
import kotlinx.android.synthetic.main.fragment_create_evaluate.*
import kotlinx.android.synthetic.main.layout_toolbar_light.*
import org.jetbrains.anko.design.snackbar
import java.util.concurrent.TimeUnit

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

    private lateinit var evaluateTodoDetailAdapter: EvaluateTodoDetailAdapter
    private lateinit var updateEvaluateViewModel: UpdateEvaluateViewModel
    private val bag = DisposeBag(this)

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        bindingViewModel()
        getDataFromBundle()
        initRecyclerView()
        initClickEvent()
        initTextChangeEvent()
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        updateEvaluateViewModel = ViewModelProviders
            .of(activity, getEvaluateDetailViewModelFactory(activity))
            .get(UpdateEvaluateViewModel::class.java)

        updateEvaluateViewModel.messageError.observe(this, Observer {
            it.getContentIfNotHandled()?.let { message ->
                view?.snackbar(message)
            }
        })
        updateEvaluateViewModel.notifyDataChanged.observe(this, Observer {
            it.getContentIfNotHandled()?.let { todo ->
                evaluateTodoDetailAdapter.notifyItem(todo)
            }
        })
        updateEvaluateViewModel.detailTodoData.observe(this, Observer {
            val todo = it.first
            val tagList = it.second
            showDetailTodoDialog(todo, tagList)
        })
        updateEvaluateViewModel.todoList.observe(this, Observer {todoList ->
            if(!todoList.isNullOrEmpty())
                evaluateTodoDetailAdapter.submitList(todoList)
            else
                setVisibilityEmptyView(true)
        })
    }

    private fun getDataFromBundle() {
        arguments?.let {
            it.getParcelable<Evaluate>(EVALUATE_BUNDLE)?.let { evaluate ->
                updateEvaluateViewModel.evaluate.postValue(evaluate)
                initViewInformation(evaluate)
                updateEvaluateViewModel.getTodo(evaluate)
            }
        }
    }

    private fun initViewInformation(evaluate: Evaluate) {
        when (evaluate.evaluateType) {
            DAY -> {
                tvCurrentDate.text = context?.getString(R.string.lbl_day_evalute)
                tvEmptyViewText.text = context?.getString(R.string.lbl_empty_todo_day)
                tvTodoListTag.text = context?.getString(R.string.lbl_today_todo)
                tvTime.text = context?.getString(R.string.lbl_evaluate_time)
                    ?.plus(context?.getString(R.string.lbl_evaluate_today))
                    ?.plus("(")
                    ?.plus(getDateString(evaluate.date))
                    ?.plus(")")
            }
            WEEK -> {
                tvCurrentDate.text = context?.getString(R.string.lbl_week_evalute)
                tvEmptyViewText.text = context?.getString(R.string.lbl_empty_todo_week)
                tvTodoListTag.text = context?.getString(R.string.lbl_this_week_todo)
                tvTime.text = context?.getString(R.string.lbl_evaluate_time)
                    ?.plus(context?.getString(R.string.lbl_evaluate_week))
                    ?.plus(evaluate.week)
                    ?.plus(", ")
                    ?.plus(evaluate.month + 1)
            }
            MONTH -> {
                tvCurrentDate.text = context?.getString(R.string.lbl_month_evalute)
                tvEmptyViewText.text = context?.getString(R.string.lbl_empty_todo_month)
                tvTodoListTag.text = context?.getString(R.string.lbl_this_month_todo)
                tvTime.text = context?.getString(R.string.lbl_evaluate_time)
                    ?.plus(context?.getString(R.string.lbl_evaluate_month))
                    ?.plus(evaluate.month)
            }
        }
        etTitle.setText(evaluate.title.toString())
        etNote.setText(evaluate.description.toString())
        btnToday.setImageResource(R.drawable.ic_check_black_24dp)
    }

    private fun initRecyclerView() {
        evaluateTodoDetail()
    }

    private fun initClickEvent() {
        RxView.clicks(btnNavigation)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
        RxView.clicks(btnToday)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                updateEvaluateViewModel.updateEvaluate()
            }.disposedBy(bag)
    }

    private fun initTextChangeEvent() {
        RxTextView.textChanges(etTitle)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                updateEvaluateViewModel.evaluate.value?.title = etTitle.text.toString()
            }.disposedBy(bag)
        RxTextView.textChanges(etNote)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                updateEvaluateViewModel.evaluate.value?.description = etNote.text.toString()
            }.disposedBy(bag)
    }

    private val checkDoneListener: (Todo) -> Unit = { todo ->
        updateEvaluateViewModel.updateTodo(todo)
    }

    private fun evaluateTodoDetail() {
        val context = context ?: return
        val layoutManager = object : LinearLayoutManager(context, VERTICAL, false) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        rvTodos.layoutManager = layoutManager
        evaluateTodoDetailAdapter =
            EvaluateTodoDetailAdapter(context, checkDoneListener) { todo ->
                updateEvaluateViewModel.getTagList(todo)
            }
        rvTodos.adapter = evaluateTodoDetailAdapter
    }

    private fun showDetailTodoDialog(todo: Todo, tagList: MutableList<TagType>?) {
        fragmentManager?.let {
            val detailTodoDialogFragment = DetailTodoDialogFragment.getInstance {

            }
            val bundle = Bundle()
            bundle.putParcelable(TODO_BUNDLE, todo)
            todo.checkList?.checkItemsFromString()?.let { checkList ->
                bundle.putParcelableArrayList(CHECK_LIST_BUNDLE, ArrayList(checkList))
            }
            tagList?.let { it1 ->
                bundle.putParcelableArrayList(TODO_TAG_LIST_BUNDLE, ArrayList(it1))
            }
            detailTodoDialogFragment.arguments = bundle
            detailTodoDialogFragment.show(it, DETAIL_TODO_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun setVisibilityEmptyView(enable: Boolean) {
        clEmptyView.visibility = getEnable(enable)
    }
}
