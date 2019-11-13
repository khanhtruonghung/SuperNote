package com.truongkhanh.supernote.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "Evaluate_List", indices = [Index("Evaluate_Todo_ID")], primaryKeys = ["Evaluate_ID", "Evaluate_Todo_ID"],
    foreignKeys = arrayOf(
        ForeignKey(
            entity = EvaluateTodo::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Evaluate_Todo_ID"]
        ),
        ForeignKey(
            entity = Evaluate::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = ["id"],
            childColumns = ["Evaluate_ID"]
        )
    )
)
data class EvaluateList(
    @ColumnInfo(name = "Evaluate_ID")
    var evaluateID: Int,
    @ColumnInfo(name = "Evaluate_Todo_ID")
    var evaluateTodoID: Int
)