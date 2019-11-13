package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.truongkhanh.supernote.R
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.utils.*
import com.truongkhanh.supernote.view.createtodo.adapter.TagListAdapter
import com.truongkhanh.supernote.view.dialog.popup.PopupDialogHelper
import kotlinx.android.synthetic.main.fragment_add_tag_dialog.*
import java.util.concurrent.TimeUnit

class TagPickerDialogFragment(private val saveClickListener: (TagType) -> Unit) :
    BottomSheetDialogFragment() {

    companion object {
        fun getInstance(saveClickListener: (TagType) -> Unit) =
            TagPickerDialogFragment(
                saveClickListener
            )
    }

    private val bag = DisposeBag(this)
    private lateinit var tagPickerDialogViewModel: TagPickerDialogViewModel
    private lateinit var tagListAdapter: TagListAdapter

    private val itemClickListener : (TagType) -> Unit = {data ->
        saveClickListener(data)
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_tag_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }
        initRecyclerView()
        bindingViewModel()
        initListener()
    }

    private fun initRecyclerView() {
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL)
        rvTagList.layoutManager = layoutManager
        tagListAdapter = TagListAdapter(itemClickListener, null)
        rvTagList.adapter = tagListAdapter
    }

    private fun bindingViewModel() {
        val activity = activity ?: return
        tagPickerDialogViewModel = ViewModelProviders
            .of(activity, getTagPickerViewModelFactory(activity))
            .get(TagPickerDialogViewModel::class.java)

        tagPickerDialogViewModel.tagList.observe(this, Observer {
            tagListAdapter.submitList(it)
        })
        tagPickerDialogViewModel.isChecked.observe(this, Observer {
            btnSave.isEnabled = it
        })
        tagPickerDialogViewModel.dismissDialog.observe(this, Observer {
            it.getContentIfNotHandled()?.let {newTagType ->
                saveClickListener(newTagType)
            }
            dismiss()
        })
    }

    private fun initListener() {
        clickListener()
        textChangeListener()
    }

    private fun clickListener() {
        RxView.clicks(btnSave)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                tagPickerDialogViewModel.createNewTag()
            }.disposedBy(bag)
        RxView.clicks(etTagName)
            .subscribe {
                cbAddTag.isEnabled = true
            }.disposedBy(bag)
        RxView.clicks(colorPicker)
            .throttleFirst(THROTTLE_TIME, TimeUnit.MILLISECONDS)
            .subscribe {
                showColorPickerPopupDialog()
            }.disposedBy(bag)
    }

    private fun textChangeListener() {
        RxTextView.textChanges(etTagName)
            .skipInitialValue()
            .subscribe {
                tagPickerDialogViewModel.newTagName.postValue(etTagName.text.toString())
            }.disposedBy(bag)
    }

    private fun showColorPickerPopupDialog() {
        val context = context ?: return
        PopupDialogHelper.setUpColorPickerPopup(context, colorPicker) {colorString ->
            tagPickerDialogViewModel.newTagColor.postValue(colorString)
            colorPicker.setBackgroundColor(getColor(colorString))
        }
    }
}