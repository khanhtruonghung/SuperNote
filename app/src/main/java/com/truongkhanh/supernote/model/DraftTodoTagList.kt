package com.truongkhanh.supernote.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "Draft_Todo_Tag_List",
    primaryKeys = ["Draft_Todo_ID", "Tag_Type_ID"],
    indices = [Index("Draft_Todo_ID")],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = DraftTodo::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Draft_Todo_ID"]
        ),
        ForeignKey(
            entity = TagType::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Tag_Type_ID"]
        )
    ))
data class DraftTodoTagList(
    @ColumnInfo(name = "Draft_Todo_ID")
    var draftTodoID: Int,
    @ColumnInfo(name = "Tag_Type_ID")
    var tagTypeID: Int
)