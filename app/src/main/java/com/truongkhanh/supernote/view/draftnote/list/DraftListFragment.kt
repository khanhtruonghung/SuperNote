package com.truongkhanh.supernote.view.draftnote.list

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.base.BaseFragment
import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.draftnote.list.adapter.DraftListAdapter
import kotlinx.android.synthetic.main.fragment_list_note.*
import kotlinx.android.synthetic.main.layout_toolbar_light.*
import org.jetbrains.anko.design.snackbar
import java.util.concurrent.TimeUnit

class DraftListFragment : BaseFragment() {

    companion object {
        fun getInstance() = DraftListFragment()
    }

    interface NavigationListener {
        fun navigateToCreateNote(draftNote: DraftNote?)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_note, container, false)
    }

    private lateinit var draftListViewModel: DraftListViewModel
    private lateinit var draftListAdapter: DraftListAdapter
    private lateinit var listener: NavigationListener
    private val bag = DisposeBag(this)

    private val longClickListener: (Pair<View, DraftNote>) -> Unit = {
        showMenu(it)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationListener)
            listener = context
    }

    override fun onResume() {
        super.onResume()
        draftListViewModel.getListNote()
    }

    override fun setUpView(view: View, savedInstanceState: Bundle?) {
        bindingViewModel()
        initViewInformation()
        initRecyclerView()
        initClickEvent()
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        draftListViewModel = ViewModelProviders
            .of(activity, getDraftNoteListViewModelFactory(activity))
            .get(DraftListViewModel::class.java)

        draftListViewModel.showMessage.observe(this, Observer { event ->
            event.getContentIfNotHandled()?.let { message ->
                view?.snackbar(message)
            }
        })
        draftListViewModel.listNote.observe(this, Observer { listNote ->
            val nullOrEmpty = listNote.isNullOrEmpty()
            if (!nullOrEmpty) {
                draftListAdapter.submitList(listNote)
            }
            setVisibilityEmptyView(nullOrEmpty)
        })
    }

    private fun initViewInformation() {
        tvCurrentDate.text = context?.getString(R.string.lbl_list_note)
        draftListViewModel.getListNote()
        btnToday.setImageResource(R.drawable.ic_add_black)
    }

    private fun initRecyclerView() {
        val context = context ?: return
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvNoteList.layoutManager = layoutManager
        draftListAdapter = DraftListAdapter(longClickListener) { draftNote ->
            listener.navigateToCreateNote(draftNote)
        }
        rvNoteList.adapter = draftListAdapter
        draftListViewModel.getListNote()
    }

    private fun initClickEvent() {
        RxView.clicks(btnTakeNote)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToCreateNote(null)
            }.disposedBy(bag)
        RxView.clicks(btnToday)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                listener.navigateToCreateNote(null)
            }.disposedBy(bag)
        RxView.clicks(btnNavigation)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                activity?.finish()
            }.disposedBy(bag)
    }

    private fun setVisibilityEmptyView(enable: Boolean) {
        rlEmptyView.visibility = getEnable(enable)
    }

    private fun showMenu(pair: Pair<View, DraftNote>) {
        val context = context ?: return
        val popup = PopupMenu(context, pair.first, Gravity.END)
        popup.menuInflater.inflate(R.menu.menu_delete_note, popup.menu)
        popup.setOnMenuItemClickListener {
            draftListViewModel.delete(pair.second)
            true
        }
        popup.show()
    }
}