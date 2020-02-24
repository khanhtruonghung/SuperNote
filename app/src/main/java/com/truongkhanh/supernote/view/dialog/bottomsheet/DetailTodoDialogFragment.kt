package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.CheckItem
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.createtodo.adapter.CheckListAdapter
import com.truongkhanh.supernote.view.createtodo.adapter.TagListAdapter
import kotlinx.android.synthetic.main.fragment_detail_todo_dialog.*
import kotlinx.android.synthetic.main.layout_todo.rvCheckList
import java.util.concurrent.TimeUnit

class DetailTodoDialogFragment(private val deletedListener: (Todo) -> Unit) : BottomSheetDialogFragment() {

    companion object {
        fun getInstance(deletedListener: (Todo) -> Unit) = DetailTodoDialogFragment(deletedListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_todo_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private lateinit var detailTodoDialogViewModel: DetailTodoDialogViewModel
    private val bag = DisposeBag(this)
    private lateinit var checkListAdapter: CheckListAdapter
    private lateinit var tagListAdapter: TagListAdapter
    private val itemCheckedListener : (CheckItem) -> Unit = {checkItem ->
        detailTodoDialogViewModel.updateTodo(checkItem)
    }

    private fun setupView() {
        bindingViewModel()
        getDataFromBundle()
        setupBottomSheetDialog()
        initClickEvent()
    }

    private fun initClickEvent() {
        RxView.clicks(btnDeleteTodo)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                detailTodoDialogViewModel.todo.value?.let {todo ->
                    deletedListener(todo)
                } ?: dismiss()
            }.disposedBy(bag)
    }

    private fun setupBottomSheetDialog() {
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            val coordinatorLayout = bottomSheet?.parent as CoordinatorLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = bottomSheet.height
            bottomSheet.setBackgroundColor(Color.TRANSPARENT)
            coordinatorLayout.parent.requestLayout()
        }
    }

    private fun getDataFromBundle() {
        arguments?.let {
            it.getParcelable<Todo>(TODO_BUNDLE)?.let { todo ->
                detailTodoDialogViewModel.todo.postValue(todo)
            }
            it.getParcelableArrayList<CheckItem>(CHECK_LIST_BUNDLE)?.let{it1 ->
                val checkList = it1.toMutableList()
                if (checkList.isNullOrEmpty()) {
                    hideRecyclerView(rvCheckList)
                } else {
                    checkListRecyclerView(checkList)
                    detailTodoDialogViewModel.checkList.postValue(checkList)
                }
            }
            it.getParcelableArrayList<TagType>(TODO_TAG_LIST_BUNDLE)?.let{it1 ->
                val tagList = it1.toMutableList()
                if (tagList.isNullOrEmpty())
                    hideRecyclerView(rvTagList)
                else
                    tagListRecyclerView(tagList)
            }
        }
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        detailTodoDialogViewModel = ViewModelProviders
            .of(activity, getDetailTodoViewModelFactory(activity))
            .get(DetailTodoDialogViewModel::class.java)

        detailTodoDialogViewModel.notifyEvent.observe(this, Observer {
            it.getContentIfNotHandled()?.let { message ->
                Log.d("Debuggg", message)
            }
        })
        detailTodoDialogViewModel.todo.observe(this, Observer { todo ->
            tvTitle.text = todo.title
            tvDescription.text = todo.description
            tvPriority.text = todo.priority.toString()
//            cbIsAllDay.isChecked = todo.isAllDay
        })
        detailTodoDialogViewModel.deletedEvent.observe(this, Observer {event ->
            event.getContentIfNotHandled()?.let{todo ->
                deletedListener(todo)
                dismiss()
            }
        })
    }

    private fun hideRecyclerView(recyclerView: RecyclerView) {
        recyclerView.visibility = View.GONE
    }

    private fun tagListRecyclerView(tagList: MutableList<TagType>) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvTagList.layoutManager = layoutManager
        tagListAdapter = TagListAdapter(null, null)
        rvTagList.adapter = tagListAdapter
        tagListAdapter.submitList(tagList)
    }

    private fun checkListRecyclerView(checkList: MutableList<CheckItem>) {
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCheckList.layoutManager = layoutManager
        checkListAdapter = CheckListAdapter(bag, itemCheckedListener,null)
        rvCheckList.adapter = checkListAdapter
        checkListAdapter.submitList(checkList)
    }
}