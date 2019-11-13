package com.truongkhanh.supernote.view.dialog.bottomsheet

import android.content.Context
import androidx.lifecycle.*
import com.truongkhanh.supernote.model.TagType
import com.truongkhanh.supernote.repository.TagTypeRepository
import com.truongkhanh.supernote.service.ApplicationDatabase
import com.truongkhanh.supernote.utils.DisposeBag
import com.truongkhanh.supernote.utils.Event
import com.truongkhanh.supernote.utils.disposedBy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class TagPickerDialogViewModel(context: Context) : ViewModel() {

    val tagList: LiveData<MutableList<TagType>>

    val isChecked = MutableLiveData<Boolean>()
    val newTagName = MutableLiveData<String>()
    val newTagColor = MutableLiveData<String>()

    private val tagTypeRepository: TagTypeRepository
    private val bag = DisposeBag(context as LifecycleOwner)
    val dismissDialog: LiveData<Event<TagType>> get() = _dismissDialog
    private val _dismissDialog = MutableLiveData<Event<TagType>>()

    init {
        val dbInstance = ApplicationDatabase.getInstance(context)
        tagTypeRepository = TagTypeRepository(dbInstance.tagTypeDao())

        tagList = tagTypeRepository.listTagType
    }

    fun createNewTag() {
        val tagType = TagType(0, newTagName.value, newTagColor.value)
        insertTag(tagType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
            _dismissDialog.value = Event(tagType)
        }.disposedBy(bag)
    }

    private fun insertTag(tagType: TagType)
        = tagTypeRepository.insert(tagType)

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TagPickerDialogViewModel(
                context
            ) as T
        }
    }
}