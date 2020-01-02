package com.truongkhanh.supernote.utils

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.truongkhanh.supernote.view.createtodo.CreateTodoViewModel
import com.truongkhanh.supernote.view.dialog.bottomsheet.DateTimeDialogViewModel
import com.truongkhanh.supernote.view.dialog.bottomsheet.DetailTodoDialogViewModel
import com.truongkhanh.supernote.view.dialog.bottomsheet.TagPickerDialogViewModel
import com.truongkhanh.supernote.view.draftnote.create.CreateNoteViewModel
import com.truongkhanh.supernote.view.draftnote.list.DraftListViewModel
import com.truongkhanh.supernote.view.evaluate.createevaluate.UpdateEvaluateViewModel
import com.truongkhanh.supernote.view.evaluate.evaluatelist.EvaluateListViewModel
import com.truongkhanh.supernote.view.mainhome.HomeViewModel

fun getDateTimeDialogViewModelFactory(): ViewModelProvider.Factory {
    return DateTimeDialogViewModel.Factory()
}

fun getCreateTodoViewModelFactory(context: Context): ViewModelProvider.Factory {
    return CreateTodoViewModel.Factory(context)
}

fun getTagPickerViewModelFactory(context: Context): ViewModelProvider.Factory {
    return TagPickerDialogViewModel.Factory(context)
}

fun getHomeViewModelFactory(context: Context): ViewModelProvider.Factory {
    return HomeViewModel.Factory(context)
}

fun getDetailTodoViewModelFactory(context: Context): ViewModelProvider.Factory {
    return DetailTodoDialogViewModel.Factory(context)
}

fun getEvaluateViewModelFactory(context: Context): ViewModelProvider.Factory {
    return EvaluateListViewModel.Factory(context)
}

fun getEvaluateDetailViewModelFactory(context: Context): ViewModelProvider.Factory {
    return UpdateEvaluateViewModel.Factory(context)
}

fun getDraftNoteListViewModelFactory(context: Context): ViewModelProvider.Factory {
    return DraftListViewModel.Factory(context)
}

fun getCreateNoteViewModelFactory(context: Context): ViewModelProvider.Factory {
    return CreateNoteViewModel.Factory(context)
}