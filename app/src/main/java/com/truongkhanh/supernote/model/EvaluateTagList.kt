package com.truongkhanh.supernote.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "Evaluate_Tag_List",
    primaryKeys = ["Evaluate_Todo_ID", "Tag_Type_ID"],
    indices = [Index("Evaluate_Todo_ID")],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EvaluateTodo::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Evaluate_Todo_ID"]
        ),
        ForeignKey(
            entity = TagType::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Tag_Type_ID"]
        )
    ))
data class EvaluateTagList(
    @ColumnInfo(name = "Evaluate_Todo_ID")
    var evaluateTodoID: Int,
    @ColumnInfo(name = "Tag_Type_ID")
    var tagTypeID: Int
)