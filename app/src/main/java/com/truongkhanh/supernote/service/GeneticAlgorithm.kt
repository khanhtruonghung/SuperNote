package com.truongkhanh.supernote.service

import android.util.Log
import com.google.gson.Gson
import com.truongkhanh.supernote.model.*
import com.truongkhanh.supernote.utils.NULL_STRING
import com.truongkhanh.supernote.utils.clone
import com.truongkhanh.supernote.utils.scheduleItemsFromString
import com.truongkhanh.supernote.utils.scheduleItemsToString
import com.truongkhanh.supernote.view.dialog.bottomsheet.AlertPickerDialogFragment
import com.truongkhanh.supernote.view.dialog.bottomsheet.LOW_PRIORITY
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.*
import kotlin.collections.HashMap
import kotlin.random.Random

class GeneticAlgorithm {
    companion object {
        private var INSTANCE: GeneticAlgorithm? = null
        fun getInstance() = if (INSTANCE != null) {
            INSTANCE
        } else {
            INSTANCE = GeneticAlgorithm()
            INSTANCE
        }
    }

    private lateinit var observable: ObservableEmitter<MutableList<Todo>>

    private var criticalError: String? = null
    private var listNoteError: MutableList<DraftNote> = mutableListOf()

    private val random = Random(Calendar.getInstance(Locale.getDefault()).timeInMillis)
    private var spaceTimes: HashMap<Long, Todo?> = hashMapOf()
    private var population: HashMap<HashMap<Long, Todo?>, Int> = hashMapOf()

    private var startDate: Long = 0L
    private var endDate: Long = 0L
    private var startTime: MyCalendar = MyCalendar(0, 0, 0, 0, 0)
    private var endTime: MyCalendar = MyCalendar(0, 0, 0, 0, 0)
    private lateinit var listDraftNote: MutableList<DraftNote>

    // Variable for hybrid and mutation
    private var totalDay = 0

    private fun showError() {
        if (!observable.isDisposed) {
            val error = Error(criticalError, listNoteError)
            val throwable = Throwable(Gson().toJson(error))
            observable.onError(throwable)
        }
    }

    fun startAlgorithm(
        listDraftNote: MutableList<DraftNote>,
        startDate: Long,
        endDate: Long,
        startTime: MyCalendar,
        endTime: MyCalendar,
        todoData: MutableList<Todo>?
    ): Observable<MutableList<Todo>> {
        return Observable.create {
            this.observable = it
            this.startDate = startDate
            this.endDate = endDate
            this.startTime = startTime
            this.endTime = endTime
            this.listDraftNote = listDraftNote

            // Tạo ra một biến map<Long, DraftNote?> để lưu khoảng thời gian
            // mà khoảng đó được dùng để chèn công việc vào.
            spaceTimes = createSpaceTimes(startDate, endDate, startTime, endTime, todoData)

            // Sắp xếp danh sách công việc theo mức độ ưu tiên cao đến thấp
            listDraftNote.sortByDescending { draftNote ->
                draftNote.priority
            }

            // Dùng map spaceTimes và fill vào đó công việc theo ngày,
            // với mỗi công việc 1 ngày làm 1 lần.
            // Mỗi spaceTimes là 1 nhiễm sắc thể.
            for (index in 1..DEFAULT_NUMBER_OF_NST) {
                fillSpaceTimeWithTodo(listDraftNote, spaceTimes)?.let { spaceTimeFilled ->
                    population[spaceTimeFilled] = 0
                } ?: showError()
            }

            if (!population.isNullOrEmpty()) {
                // Tính điểm thích nghi của từng cá thể trong quần thể.
                for (spaceTime in population) {
                    val point = random.nextInt(100)
                    spaceTime.setValue(point)
                }

                // Sắp xếp danh sách theo giá trị giảm dần điểm thích nghi.
                val sortedMap = population.toList().sortedByDescending { (_, point) ->
                    point
                }.toMap()
                population = HashMap(sortedMap)

                // Ta thực hiện lai ghép cho 4 nhiễm sắc thể đầu tiên có độ thích nghi cao nhất.
                // Nếu xác suất lai ghép thất bại ta chuyển sang đột biến.
                val randomNumber = random.nextInt(100)
                val listPopulation = population.toList()
                if (randomNumber <= DEFAULT_HYBRID_PROBABILITY) {
                    listPopulation.getOrNull(0)?.first?.let { dad ->
                        listPopulation.getOrNull(1)?.first?.let { mom ->
                            startHybrid(dad, mom)
                        }
                    }
                    listPopulation.getOrNull(2)?.first?.let { dad ->
                        listPopulation.getOrNull(3)?.first?.let { mom ->
                            startHybrid(dad, mom)
                        }
                    }
                } else {
                    listPopulation.getOrNull(0)?.first?.let{base ->
                        startMutation2(base)
                    }
                    listPopulation.getOrNull(1)?.first?.let{base ->
                        startMutation2(base)
                    }
                    listPopulation.getOrNull(2)?.first?.let{base ->
                        startMutation2(base)
                    }
                    listPopulation.getOrNull(3)?.first?.let{base ->
                        startMutation2(base)
                    }
                }

                // Sắp xếp lại danh sách
                // Lấy ra lịch có điểm cao nhất
                val bestIndividual = HashMap(population.toList().sortedByDescending { pair ->
                    pair.second
                }.toMap()).keys.toList()
                val individualNullable = bestIndividual[0].values.toMutableList()
                val individualNonNull = individualNullable.filterNotNull()
                val resultList: MutableList<Todo> = mutableListOf()
                individualNonNull.forEach { todo ->
                    if (!resultList.containTodo(todo.id))
                        resultList.add(todo)
                }
                resultList.forEach { todo ->
                    val scheduleItems = todo.schedule?.scheduleItemsFromString()
                    if (!scheduleItems.isNullOrEmpty()) {
                        var start: Long = scheduleItems[0].date
                        var end: Long = scheduleItems[0].date
                        scheduleItems.forEach { item ->
                            if (item.date > end) {
                                end = item.date
                            }
                            if (item.date < start) {
                                start = item.date
                            }
                        }
                        todo.startDate = start
                        todo.endDate = end
                    }
                }
                it.onNext(resultList)
            } else {
                if (!it.isDisposed) {
                    it.onError(Throwable("Population cant create and was empty"))
                }
            }
        }
    }

    private fun MutableList<Todo>.containTodo(id: Int): Boolean {
        this.forEach {
            if (it.id == id)
                return true
        }
        return false
    }

    private fun HashMap<Long, Todo?>.calculateItem(): Int {
        var point = 0
        val temp = hashMapOf<Int, Todo?>()
        this.forEach {
            if (it.value != null) {
                if (!temp.containsKey(it.value!!.id)) {
                    temp.put(it.value!!.id, it.value)
                    point+=2
                }
                if (!(isWorkDisrupt(this, it.key, 1)))
                    point++
            }
        }
        return point
    }

//    private fun startMutation(mutation: HashMap<Long, Todo?>) {
//        val noteID = listDraftNote[random.nextInt(listDraftNote.size)].id
//        val mutationClone = mutation.clone() as HashMap<Long, Todo?>
//
//        mutationClone.filterValues { todo ->
//            todo?.id == noteID
//        }
//        var child = mutation.clone() as HashMap<Long, Todo?>
//        mutationClone.forEach {
//            if (it.value != null)
//                child.remove(it.key)
//        }
//        mutationLogic(child, noteID)?.let {
//            child = it
//        } ?: showError()
//
//        val childPoint = random.nextInt(100)
//        population.plus(Pair(child, childPoint))
//    }
//
//    private fun mutationLogic(child: HashMap<Long, Todo?>, noteID: Int): HashMap<Long, Todo?>? {
//        val childClone = child.clone() as HashMap<Long, Todo?>
//        val freeSpaceTimes = getFreeSpaceTimes(childClone)
//        val scheduleItems: MutableList<ScheduleItem> = mutableListOf()
//
//        var dailyEstimate = 0
//        var dailyCost = 0
//        var totalEstimate = 0F
//        var mNote: DraftNote? = null
//        listDraftNote.forEach { note ->
//            if (note.id == noteID) {
//                mNote = note
//                dailyEstimate = note.estimateDaily
//                dailyCost = note.estimateDaily / DEFAULT_SPACE_TIME_MINUTE
//                totalEstimate = note.estimateTotal * SIXTY_MINUTE
//                return@forEach
//            }
//        }
//
//        do {
//            // Nếu thời gian rãnh bị null hoặc empty thì báo lỗi không đủ thời gian sắp xếp
//            if (!freeSpaceTimes.isNullOrEmpty()) {
//                // Nếu không còn ngày nào làm việc được thì break
//                if (scheduleItems.isNotEnoughDayToWork(mNote!!.startDate, mNote!!.deadline)) {
//                    criticalError = "Don't have enough day to work when doing mutation logic"
//                    return null
//                }
//
//                // Chọn ngẫu nhiên 1 space time để làm việc
//                val spaceTime =
//                    freeSpaceTimes.entries.elementAt(random.nextInt(freeSpaceTimes.size))
//                val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
//                spaceTimeStart.timeInMillis = spaceTime.key
//                val dayOfSpaceTime = spaceTimeStart.get(Calendar.DAY_OF_MONTH)
//                val monthOfSpaceTime = spaceTimeStart.get(Calendar.MONTH)
//
//                // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
//                if (scheduleItems.containDate(dayOfSpaceTime, monthOfSpaceTime)) {
//                    freeSpaceTimes.remove(spaceTime.key)
//                    continue
//                }
//
//                // Nếu công việc bị gián đoạn, tức không liên tục thì bắt đầu lại từ đầu và xóa spaceTime ở đó đi
//                if (!isWorkDisrupt(childClone, spaceTime.key, dailyCost)) {
//                    val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
//                    scheduleItem.timeStart = MyCalendar(
//                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
//                        spaceTimeStart.get(Calendar.MONTH) + 1,
//                        0,
//                        spaceTimeStart.get(Calendar.MINUTE),
//                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
//                    )
//                    spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
//                    scheduleItem.timeEnd = MyCalendar(
//                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
//                        spaceTimeStart.get(Calendar.MONTH) + 1,
//                        0,
//                        spaceTimeStart.get(Calendar.MINUTE),
//                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
//                    )
//                    scheduleItems.add(scheduleItem)
//
//                    // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
//                    val calendar = Calendar.getInstance(Locale.getDefault())
//                    calendar.timeInMillis = spaceTime.key
//                    for (i in 1..dailyCost) {
//                        calendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
//                        childClone[calendar.timeInMillis] = Todo(
//                            id = mNote!!.id,
//                            title = mNote?.title,
//                            description = mNote?.description,
//                            checkList = NULL_STRING,
//                            priority = mNote?.priority,
//                            startDate = 0L,
//                            endDate = 0L,
//                            alertType = AlertPickerDialogFragment.NO_ALERT,
//                            checkDoneDate = 0L,
//                            isDone = false,
//                            notificationRequestID = NULL_STRING,
//                            schedule = NULL_STRING
//                        )
//                    }
//                } else {
//                    freeSpaceTimes.remove(spaceTime.key)
//                    continue
//                }
//            } else {
//                // Nếu draftNote có mức độ ưu tiên thấp thì không vi phạm ràng buộc cứng.
//                // Do đó có thể dừng ở đây.
//                if (mNote?.priority == LOW_PRIORITY) {
//                    break
//                } else {
//                    criticalError =
//                        "Don't have enough space times to optimize when doing mutation logic"
//                    return null
//                }
//            }
//
//            // Giảm tổng thời gian sau mỗi vòng lập 1
//            totalEstimate -= dailyEstimate
//        } while (totalEstimate > 0)
//        childClone.forEach {
//            if (it.value?.id == mNote?.id) {
//                it.value?.schedule = scheduleItems.scheduleItemsToString()
//            }
//        }
//        return childClone
//    }

    private fun startHybrid(dad: HashMap<Long, Todo?>, mom: HashMap<Long, Todo?>) {
        var numberOfDay = totalDay
        while (numberOfDay > totalDay / 2) {
            numberOfDay = random.nextInt(1, totalDay)
        }
        val additionalDay = random.nextInt(0, totalDay - numberOfDay)

        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = startDate
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        startCalendar.add(Calendar.DAY_OF_MONTH, additionalDay)

        val arrayDay = getArrayDay(numberOfDay, startCalendar.clone() as Calendar)
        val childBaseOnDad: HashMap<Long, Todo?> = hashMapOf()
        val dadGen: HashMap<Long, Todo?> = hashMapOf()

        val childBaseOnMom: HashMap<Long, Todo?> = hashMapOf()
        val momGen: HashMap<Long, Todo?> = hashMapOf()

        dad.forEach { data ->
            if (data.value != null) {
                if (data.key.isThisDayContainIn(arrayDay)) {
                    childBaseOnDad.put(data.key, data.value)
                } else {
                    childBaseOnDad.put(data.key, null)
                    dadGen.put(data.key, data.value)
                }
            } else {
                childBaseOnDad.put(data.key, null)
            }
        }

        mom.forEach { data ->
            if (data.value != null) {
                if (data.key.isThisDayContainIn(arrayDay)) {
                    childBaseOnMom.put(data.key, data.value)
                } else {
                    childBaseOnMom.put(data.key, null)
                    momGen.put(data.key, data.value)
                }
            } else {
                childBaseOnMom.put(data.key, null)
            }
        }
        val a = (mom.clone() as HashMap<Long, Todo?>).filterValues {
            it != null
        }
        val b = (dad.clone() as HashMap<Long, Todo?>).filterValues {
            it != null
        }
        Log.d("DebugggHydrib", "mom $a")
        Log.d("DebugggHydrib", "dad $b")

        val child1 = hybridLogic(dadGen, childBaseOnMom)
        Log.d("DebugggHydrib", "child1 $child1")
        population.plus(Pair(child1, 100))

        val child2 = hybridLogic(momGen, childBaseOnDad)
        Log.d("DebugggHydrib", "child2 $child2")
        population.plus(Pair(child2, 100))
    }

    private fun hybridLogic(
        parentGen: HashMap<Long, Todo?>,
        childBase: HashMap<Long, Todo?>
    ): HashMap<Long, Todo?>? {
        var childResult = childBase.clone() as HashMap<Long, Todo?>
        val missingDraftNote: MutableList<DraftNote> = mutableListOf()
        listDraftNote.forEach { draftNote ->
            if (childBase.containID(draftNote.id)) {
                childBase.getTodoByID(draftNote.id)?.let { todo ->
                    todo.schedule?.scheduleItemsFromString()?.let { schedule ->
                        val newDraft = draftNote.clone()
                        val estimateTotal = draftNote.estimateTotal * SIXTY_MINUTE
                        val a = (schedule.count().toFloat() * draftNote.estimateDaily)
                        when {
                            a < estimateTotal -> {
                                newDraft.estimateTotal =
                                    draftNote.estimateTotal - (schedule.count() * draftNote.estimateDaily)
                                missingDraftNote.add(newDraft)
                            }
                            (schedule.count().toFloat() * draftNote.estimateDaily) == estimateTotal -> {
                                Log.d("Debuggg", "This draft was fully done, id: " + draftNote.id)
                            }
                            (schedule.count().toFloat() * draftNote.estimateDaily) > estimateTotal -> {
                                Log.d(
                                    "Debuggg",
                                    "Cant calculate total estimate: " + (schedule.count() * draftNote.estimateDaily)
                                )
                                criticalError = "Cant calculate total estimate"
                                return null
                            }
                        }
                        true
                    }
                }
            } else {
                missingDraftNote.add(draftNote)
            }
        }

        missingDraftNote.forEach { draftNote ->
            val parentGenFiltered = (parentGen.clone() as HashMap<Long, Todo?>).filterValues {
                it?.id == draftNote.id
            }

            val freeSpaceTimes = getFreeSpaceTimes(childBase)
            val totalEstimate = (draftNote.estimateTotal * SIXTY_MINUTE)
            val dailyEstimate = draftNote.estimateDaily
            val dailyCost = draftNote.estimateDaily / DEFAULT_SPACE_TIME_MINUTE

            if (parentGenFiltered.isEmpty()) {
                fillNoteRandomly(
                    freeSpaceTimes,
                    draftNote,
                    dailyCost,
                    dailyEstimate,
                    totalEstimate
                )?.let { data ->
                    childResult = data
                } ?: showError()
            } else {
                var scheduleItems: MutableList<ScheduleItem> = mutableListOf()
                parentGenFiltered.forEach { data ->
                    if (freeSpaceTimes.containsKey(data.key) && !isWorkDisrupt(
                            childResult,
                            data.key,
                            dailyCost
                        )
                    ) {
                        val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                        spaceTimeStart.timeInMillis = data.key
                        val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                        scheduleItem.timeStart = MyCalendar(
                            spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                            spaceTimeStart.get(Calendar.MONTH) + 1,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                        scheduleItem.timeEnd = MyCalendar(
                            spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                            spaceTimeStart.get(Calendar.MONTH) + 1,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        scheduleItems.add(scheduleItem)

                        // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                        for (i in 1..dailyCost) {
                            childResult[spaceTimeStart.timeInMillis] = Todo(
                                id = draftNote.id,
                                title = draftNote.title,
                                description = draftNote.description,
                                checkList = NULL_STRING,
                                priority = draftNote.priority,
                                startDate = 0L,
                                endDate = 0L,
                                alertType = AlertPickerDialogFragment.NO_ALERT,
                                checkDoneDate = 0L,
                                isDone = false,
                                notificationRequestID = NULL_STRING,
                                schedule = NULL_STRING
                            )
                            freeSpaceTimes.remove(spaceTimeStart.timeInMillis)
                            spaceTimeStart.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                        }
                    } else {
                        fillItemRandomly(
                            freeSpaceTimes,
                            draftNote,
                            scheduleItems,
                            dailyCost,
                            dailyEstimate
                        )?.let { pairData ->
                            scheduleItems = pairData.first
                            childResult = pairData.second
                        } ?: showError()
                    }
                }
                childResult.forEach {
                    if (it.value?.id == draftNote.id) {
                        it.value?.schedule = scheduleItems.scheduleItemsToString()
                    }
                }
            }
        }
        Log.d("Debugggg", childResult.toString())
        return childResult
    }

    private fun startMutation2(mutation: HashMap<Long, Todo?>) {
        var numberOfDay = totalDay
        while (numberOfDay > totalDay / 2) {
            numberOfDay = random.nextInt(1, totalDay)
        }

        val additionalDay = random.nextInt(0, totalDay - numberOfDay)
        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = startDate
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        startCalendar.add(Calendar.DAY_OF_MONTH, additionalDay)
        val arrayDay = getArrayDay(numberOfDay, startCalendar.clone() as Calendar)

        val result = hashMapOf<Long, Todo?>()
        val listMutation = hashMapOf<Long, Todo?>()

        mutation.forEach { map ->
            if (map.value != null) {
                if (map.key.isThisDayContainIn(arrayDay)) {
                    listMutation.put(map.key, map.value)
                    result.put(map.key, null)
                } else {
                    result.put(map.key, map.value)
                }
            } else {
                result.put(map.key, null)
            }
        }

        listMutation.forEach { map ->
            val scheduleItems = map.value!!.schedule!!.scheduleItemsFromString()
            val freeSpaceTimes = getFreeSpaceTimes(result.clone() as HashMap<Long, Todo?>)
            while (true) {
                if (!freeSpaceTimes.isNullOrEmpty()) {
                    val listFreeTimes = freeSpaceTimes.toList()
                    val rng = random.nextInt(listFreeTimes.size)
                    val spaceTime = listFreeTimes[rng]

                    if (!(spaceTime.first.isThisDayContainIn(arrayDay) || spaceTime.first.isTheSameDay(map.key))) {
                        freeSpaceTimes.remove(spaceTime.first)
                        continue
                    }

                    val dailyCost = getDailyCost(map.value!!)
                    val dailyEstimate = getDailyEstimate(map.value!!)
                    if (!(isWorkDisrupt(result, spaceTime.first, dailyCost))) {
                        var index = 0
                        scheduleItems.forEachIndexed { i, scheduleItem ->
                            if (scheduleItem.date.isTheSameDay(spaceTime.first)) {
                                index = i
                            }
                        }
                        val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                        spaceTimeStart.timeInMillis = spaceTime.first
                        scheduleItems[index] = ScheduleItem(
                            spaceTime.first,
                            MyCalendar(
                                spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                                spaceTimeStart.get(Calendar.MONTH) + 1,
                                0,
                                spaceTimeStart.get(Calendar.MINUTE),
                                spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                            ),
                            null
                        )
                        spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                        scheduleItems[index].timeEnd = MyCalendar(
                            spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                            spaceTimeStart.get(Calendar.MONTH) + 1,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        for (i in 1..dailyCost) {
                            result[spaceTimeStart.timeInMillis] = map.value!!
                            spaceTimeStart.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                        }
                        break
                    } else {
                        freeSpaceTimes.remove(spaceTime.first)
                        continue
                    }
                } else {
                    criticalError =
                        "Don't have enough space times to optimize when mutation"
                }
            }
            result.forEach {
                if (it.value?.id == map.value!!.id) {
                    it.value?.schedule = scheduleItems.scheduleItemsToString()
                }
            }
        }

        population.put(result, 100)
    }

    private fun fillItemRandomly(
        freeSpaceTimes: HashMap<Long, Todo?>,
        draftNote: DraftNote,
        scheduleItems: MutableList<ScheduleItem>,
        dailyCost: Int,
        dailyEstimate: Int
    ): Pair<MutableList<ScheduleItem>, HashMap<Long, Todo?>>? {
        val freeTimesClone: HashMap<Long, Todo?> = freeSpaceTimes.clone() as HashMap<Long, Todo?>
        val result = freeTimesClone.clone() as HashMap<Long, Todo?>
        val mScheduleItems = scheduleItems
        while (true) {
            if (!freeTimesClone.isNullOrEmpty()) {
                if (mScheduleItems.isNotEnoughDayToWork(draftNote.startDate, draftNote.deadline)) {
                    criticalError =
                        "Don't have enough day to work when fill item randomly"
                    return null
                }

                val listFreeTimes = freeTimesClone.toList()
                val rng = random.nextInt(listFreeTimes.size)
                val spaceTime = listFreeTimes[rng]

                if (spaceTime.first.isOutTimeBound(draftNote.startDate, draftNote.deadline)) {
                    freeTimesClone.remove(spaceTime.first)
                    continue
                }

                val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                spaceTimeStart.timeInMillis = spaceTime.first

                // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
                if (mScheduleItems.containDate(
                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                        spaceTimeStart.get(Calendar.MONTH)
                    )
                ) {
                    freeTimesClone.remove(spaceTime.first)
                    continue
                }

                if (!isWorkDisrupt(result, spaceTimeStart.timeInMillis, dailyCost)) {
                    val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                    scheduleItem.timeStart = MyCalendar(
                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                        spaceTimeStart.get(Calendar.MONTH) + 1,
                        0,
                        spaceTimeStart.get(Calendar.MINUTE),
                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                    )
                    spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                    scheduleItem.timeEnd = MyCalendar(
                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                        spaceTimeStart.get(Calendar.MONTH) + 1,
                        0,
                        spaceTimeStart.get(Calendar.MINUTE),
                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                    )
                    mScheduleItems.add(scheduleItem)

                    // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                    for (i in 1..dailyCost) {
                        result[spaceTimeStart.timeInMillis] = Todo(
                            id = draftNote.id,
                            title = draftNote.title,
                            description = draftNote.description,
                            checkList = NULL_STRING,
                            priority = draftNote.priority,
                            startDate = 0L,
                            endDate = 0L,
                            alertType = AlertPickerDialogFragment.NO_ALERT,
                            checkDoneDate = 0L,
                            isDone = false,
                            notificationRequestID = NULL_STRING,
                            schedule = NULL_STRING
                        )
                        freeTimesClone.remove(spaceTimeStart.timeInMillis)
                        spaceTimeStart.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                    }
                    // Hoàn tất việc sắp xếp công việc trong 1 ngày một cách ngẫu nhiên
                    break
                } else {
                    freeTimesClone.remove(spaceTime.first)
                    continue
                }
            } else {
                criticalError =
                    "Don't have enough space times to optimize when fill item randomly"
                return null
            }
        }
        return Pair(mScheduleItems, result)
    }

    private fun fillNoteRandomly(
        freeSpaceTimes: HashMap<Long, Todo?>,
        draftNote: DraftNote,
        dailyCost: Int,
        dailyEstimate: Int,
        totalEstimate: Float
    ): HashMap<Long, Todo?>? {
        val scheduleItems: MutableList<ScheduleItem> = mutableListOf()
        val freeTimesClone: HashMap<Long, Todo?> = freeSpaceTimes.clone() as HashMap<Long, Todo?>
        val result = freeTimesClone.clone() as HashMap<Long, Todo?>
        var totalEstimateResult = totalEstimate
        do {
            // Nếu thời gian rãnh bị null hoặc empty thì báo lỗi không đủ thời gian sắp xếp
            if (!freeTimesClone.isNullOrEmpty()) {
                // Nếu không còn ngày nào làm việc được thì break
                if (scheduleItems.isNotEnoughDayToWork(draftNote.startDate, draftNote.deadline)) {
                    criticalError =
                        "Don't have enough day to work when fill note randomly"
                    return null
                }

                // Chọn ngẫu nhiên 1 space time để làm việc
                val listFreeTimes = freeTimesClone.toList()
                val rng = random.nextInt(listFreeTimes.size)
                val spaceTime = listFreeTimes[rng]

                // Nếu công việc nằm ngoài thời gian làm việc của draft note là phạm ràng buộc
                if (spaceTime.first.isOutTimeBound(draftNote.startDate, draftNote.deadline)) {
                    freeTimesClone.remove(spaceTime.first)
                    continue
                }

                val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                spaceTimeStart.timeInMillis = spaceTime.first

                // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
                if (scheduleItems.containDate(
                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                        spaceTimeStart.get(Calendar.MONTH)
                    )
                ) {
                    freeTimesClone.remove(spaceTime.first)
                    continue
                }

                // Nếu công việc bị gián đoạn, tức không liên tục thì bắt đầu lại từ đầu và xóa spaceTime ở đó đi
                if (!isWorkDisrupt(result, spaceTimeStart.timeInMillis, dailyCost)) {
                    val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                    scheduleItem.timeStart = MyCalendar(
                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                        spaceTimeStart.get(Calendar.MONTH) + 1,
                        0,
                        spaceTimeStart.get(Calendar.MINUTE),
                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                    )
                    spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                    scheduleItem.timeEnd = MyCalendar(
                        spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                        spaceTimeStart.get(Calendar.MONTH) + 1,
                        0,
                        spaceTimeStart.get(Calendar.MINUTE),
                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                    )
                    scheduleItems.add(scheduleItem)

                    // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                    for (i in 1..dailyCost) {
                        result[spaceTimeStart.timeInMillis] = Todo(
                            id = draftNote.id,
                            title = draftNote.title,
                            description = draftNote.description,
                            checkList = NULL_STRING,
                            priority = draftNote.priority,
                            startDate = 0L,
                            endDate = 0L,
                            alertType = AlertPickerDialogFragment.NO_ALERT,
                            checkDoneDate = 0L,
                            isDone = false,
                            notificationRequestID = NULL_STRING,
                            schedule = NULL_STRING
                        )
                        freeTimesClone.remove(spaceTimeStart.timeInMillis)
                        spaceTimeStart.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                    }
                    // Giảm tổng thời gian sau mỗi vòng lập
                    totalEstimateResult -= dailyEstimate
                } else {
                    freeTimesClone.remove(spaceTime.first)
                    continue
                }
            } else {
                // Không còn thời gian trống nào để sắp xếp công việc
                criticalError =
                    "Don't have enough space times to optimize when fill note randomly"
                return null
            }
        } while (totalEstimateResult > 0)
        result.forEach {
            if (it.value?.id == draftNote.id) {
                it.value?.schedule = scheduleItems.scheduleItemsToString()
            }
        }
        return result
    }

    private fun fillSpaceTimeWithTodo(
        listDraftNote: MutableList<DraftNote>,
        spaceTimes: HashMap<Long, Todo?>
    ): HashMap<Long, Todo?>? {

        val spaceTimesClone = spaceTimes.clone() as HashMap<Long, Todo?>
        listDraftNote.forEach { draftNote ->
            // Lấy toàn bộ thời gian rãnh còn lại
            val freeSpaceTimes = getFreeSpaceTimes(spaceTimesClone)

            var totalEstimate = (draftNote.estimateTotal * SIXTY_MINUTE)
            val dailyEstimate = draftNote.estimateDaily
            val dailyCost = draftNote.estimateDaily / DEFAULT_SPACE_TIME_MINUTE
            val scheduleItems: MutableList<ScheduleItem> = mutableListOf()

            do {
                // Nếu thời gian rãnh bị null hoặc empty thì báo lỗi không đủ thời gian sắp xếp
                if (!freeSpaceTimes.isNullOrEmpty()) {
                    // Nếu không còn ngày nào làm việc được thì break
                    if (scheduleItems.isNotEnoughDayToWork(
                            draftNote.startDate,
                            draftNote.deadline
                        )
                    ) {
                        criticalError =
                            "Don't have enough day to work when fill space time with todo name: " + draftNote.title
                        return@fillSpaceTimeWithTodo null
                    }

                    // Chọn ngẫu nhiên 1 space time để làm việc
                    val listFreeTimes = freeSpaceTimes.toList()
                    val rng = random.nextInt(listFreeTimes.size)
                    val spaceTime = listFreeTimes[rng]
                    val a = spaceTime.first.isOutTimeBound(draftNote.startDate, draftNote.deadline)
                    if (a) {
                        freeSpaceTimes.remove(spaceTime.first)
                        continue
                    }

                    val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                    spaceTimeStart.timeInMillis = spaceTime.first

                    // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
                    if (scheduleItems.containDate(
                            spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                            spaceTimeStart.get(Calendar.MONTH)
                        )
                    ) {
                        freeSpaceTimes.remove(spaceTime.first)
                        continue
                    }

                    // Nếu công việc bị gián đoạn, tức không liên tục thì bắt đầu lại từ đầu và xóa spaceTime ở đó đi
                    if (!isWorkDisrupt(spaceTimesClone, spaceTimeStart.timeInMillis, dailyCost)) {
                        val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                        scheduleItem.timeStart = MyCalendar(
                            spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                            spaceTimeStart.get(Calendar.MONTH) + 1,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                        scheduleItem.timeEnd = MyCalendar(
                            spaceTimeStart.get(Calendar.DAY_OF_MONTH),
                            spaceTimeStart.get(Calendar.MONTH) + 1,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        scheduleItems.add(scheduleItem)

                        // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                        for (i in 1..dailyCost) {
                            spaceTimesClone[spaceTimeStart.timeInMillis] = Todo(
                                id = draftNote.id,
                                title = draftNote.title,
                                description = draftNote.description,
                                checkList = NULL_STRING,
                                priority = draftNote.priority,
                                startDate = 0L,
                                endDate = 0L,
                                alertType = AlertPickerDialogFragment.NO_ALERT,
                                checkDoneDate = 0L,
                                isDone = false,
                                notificationRequestID = NULL_STRING,
                                schedule = NULL_STRING
                            )
                            freeSpaceTimes.remove(spaceTimeStart.timeInMillis)
                            spaceTimeStart.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                        }
                        // Giảm tổng thời gian sau mỗi vòng lập 1
                        totalEstimate -= dailyEstimate
                    } else {
                        freeSpaceTimes.remove(spaceTime.first)
                        continue
                    }
                } else {
                    // Nếu draftNote có mức độ ưu tiên thấp thì không vi phạm ràng buộc cứng.
                    // Do đó có thể dừng ở đây.
                    if (draftNote.priority == LOW_PRIORITY) {
                        break
                    } else {
                        criticalError =
                            "Don't have enough space times to optimize when fill space time with todo"
                        return null
                    }
                }
            } while (totalEstimate > 0)
            spaceTimesClone.forEach {
                if (it.value?.id == draftNote.id) {
                    it.value?.schedule = scheduleItems.scheduleItemsToString()
                }
            }
        }
        return spaceTimesClone
    }

    private fun Long.isOutTimeBound(startDate: Long, deadline: Long): Boolean {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = this

        val deadlineCalendar = Calendar.getInstance()
        deadlineCalendar.timeInMillis = deadline
        val startCalendar = Calendar.getInstance()
        startCalendar.timeInMillis = startDate

        val startMonth = startCalendar.get(Calendar.MONTH)
        val endMonth = deadlineCalendar.get(Calendar.MONTH)
        val currentMonth = currentCalendar.get(Calendar.MONTH)
        val startDay = startCalendar.get(Calendar.DAY_OF_MONTH)
        val endDay = deadlineCalendar.get(Calendar.DAY_OF_MONTH)
        val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)

        if (startMonth == currentMonth || endMonth == currentMonth) {
            if (currentDay < startDay)
                return true
            if (currentDay > endDay)
                return true
            return false
        } else return currentMonth !in startMonth..endMonth

//        return when {
//            (startCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) -> {
//                currentCalendar.get(Calendar.DAY_OF_MONTH) < startCalendar.get(Calendar.DAY_OF_MONTH)
//            }
//            (deadlineCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) -> {
//                currentCalendar.get(Calendar.DAY_OF_MONTH) > deadlineCalendar.get(Calendar.DAY_OF_MONTH)
//            }
//            (deadlineCalendar.get(Calendar.MONTH) > currentCalendar.get(Calendar.MONTH) &&
//                    startCalendar.get(Calendar.MONTH) < currentCalendar.get(Calendar.MONTH)) -> false
//            else -> true
//        }
    }

    private fun isWorkDisrupt(
        freeSpaceTimes: HashMap<Long, Todo?>,
        start: Long,
        dailyCost: Int
    ): Boolean {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = start
        for (i in 1..dailyCost) {
            if (!freeSpaceTimes.containsKey(calendar.timeInMillis) || freeSpaceTimes[calendar.timeInMillis] != null) {
                return true
            }
            calendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
        }
        return false
    }

    private fun MutableList<ScheduleItem>.containDate(day: Int, month: Int): Boolean {
        this.forEach {
            if (it.timeStart?.day == day && it.timeStart?.month == month + 1)
                return true
        }
        return false
    }

    private fun MutableList<ScheduleItem>.isNotEnoughDayToWork(
        startDate: Long,
        deadline: Long
    ): Boolean {
        if (this.isNullOrEmpty()) {
            return false
        }

        val start = Calendar.getInstance(Locale.getDefault())
        start.timeInMillis = startDate
        var dayStart = start.get(Calendar.DAY_OF_MONTH)
        var monthStart = start.get(Calendar.MONTH)

        val end = Calendar.getInstance(Locale.getDefault())
        end.timeInMillis = deadline
        val dayEnd = end.get(Calendar.DAY_OF_MONTH)

        var isEnough = true
        while (dayStart != (dayEnd + 1)) {
            if (!this.containDate(dayStart, monthStart))
                isEnough = false
            start.add(Calendar.DAY_OF_MONTH, 1)
            dayStart = start.get(Calendar.DAY_OF_MONTH)
            monthStart = start.get(Calendar.MONTH)
        }
        return isEnough
    }

    @Suppress("UNUSED_VALUE")
    private fun createSpaceTimes(
        startDate: Long,
        endDate: Long,
        startTime: MyCalendar,
        endTime: MyCalendar,
        todoData: MutableList<Todo>?
    ): HashMap<Long, Todo?> {
        val startCalendar = Calendar.getInstance(Locale.getDefault())
        startCalendar.timeInMillis = startDate
        startCalendar.set(Calendar.HOUR_OF_DAY, startTime.hour)
        startCalendar.set(Calendar.MINUTE, startTime.minute)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        this.startDate = startCalendar.timeInMillis

        val endCalendar = Calendar.getInstance(Locale.getDefault())
        endCalendar.timeInMillis = endDate
        endCalendar.set(Calendar.HOUR_OF_DAY, endTime.hour)
        endCalendar.set(Calendar.MINUTE, endTime.minute)
        endCalendar.set(Calendar.SECOND, 0)
        endCalendar.set(Calendar.MILLISECOND, 0)
        this.endDate = endCalendar.timeInMillis

        val endHour = endCalendar.get(Calendar.HOUR_OF_DAY)
        val endMinute = endCalendar.get(Calendar.MINUTE)
        val endDay = endCalendar.get(Calendar.DAY_OF_MONTH)
        var currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
        this.totalDay = endDay - currentDay

        val spaceTimes: HashMap<Long, Todo?> = hashMapOf()
        val unavailableTimeList = getScheduleArray(todoData)
        val listSpaceTimes = spaceTimes.toList().toMutableList()
        if (!unavailableTimeList.isContain(startCalendar.timeInMillis)) {
            listSpaceTimes.add(Pair(startCalendar.timeInMillis, null))
        }

        while (currentDay != (endDay + 1)) {
            val startHour = startCalendar.get(Calendar.HOUR_OF_DAY)
            val startMinute = startCalendar.get(Calendar.MINUTE)
            when {
                (startHour > endHour) -> {
                    startCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
                    startCalendar.set(Calendar.HOUR_OF_DAY, startTime.hour)
                    startCalendar.set(Calendar.MINUTE, startTime.minute)
                }
                (startHour == endHour) -> {
                    if ((endMinute - startMinute) > 0) {
                        startCalendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                    } else {
                        startCalendar.add(Calendar.DAY_OF_MONTH, 1)
                        currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
                        startCalendar.set(Calendar.HOUR_OF_DAY, startTime.hour)
                        startCalendar.set(Calendar.MINUTE, startTime.minute)
                    }
                }
                (startHour < endHour) -> {
                    startCalendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                }
            }
            if (!unavailableTimeList.isContain(startCalendar.timeInMillis)) {
                listSpaceTimes.add(Pair(startCalendar.timeInMillis, null))
            }
        }
        return HashMap(listSpaceTimes.toMap())
    }

    private fun getScheduleArray(todoData: MutableList<Todo>?): MutableList<Long> {
        val result: MutableList<Long> = mutableListOf()
        return if (todoData.isNullOrEmpty())
            result
        else {
            todoData.forEach { todo ->
                todo.schedule?.scheduleItemsFromString()?.forEach { scheduleItem ->
                    result.add(scheduleItem.date)
                }
            }
            result
        }
    }

    private fun MutableList<Long>.isContain(date: Long): Boolean {
        this.forEach {
            val start = Calendar.getInstance(Locale.getDefault())
            start.timeInMillis = date

            val end = Calendar.getInstance(Locale.getDefault())
            end.timeInMillis = date
            end.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)

            if (it in start.timeInMillis..end.timeInMillis)
                return true
        }
        return false
    }

    private fun getFreeSpaceTimes(spaceTimesInput: HashMap<Long, Todo?>): HashMap<Long, Todo?> {
        return HashMap(spaceTimesInput.toList().filterNot {
            it.second != null
        }.toMap())
    }

    private fun MutableList<DraftNote>.containID(id: Int): Boolean {
        this.forEach {
            if (it.id == id)
                return true
        }
        return false
    }

    private fun HashMap<Long, Todo?>.containID(id: Int): Boolean {
        this.forEach { data ->
            data.value?.id?.let {
                if (it == id)
                    return true
            }
        }
        return false
    }

    private fun HashMap<Long, Todo?>.getTodoByID(id: Int): Todo? {
        this.forEach { data ->
            data.value?.id?.let {
                if (it == id)
                    return data.value
            }
        }
        return null
    }

    private fun HashMap<Long, Todo?>.getDateByID(id: Int): Long? {
        this.forEach { data ->
            data.value?.id?.let {
                if (it == id)
                    return data.key
            }
        }
        return null
    }

    private fun Long.isThisDayContainIn(arrayListDay: ArrayList<Long>): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.timeInMillis = this
        arrayListDay.forEach { dayLong ->
            val calendar2 = Calendar.getInstance()
            calendar2.timeInMillis = dayLong
            if (calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH) &&
                calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
            )
                return true
        }
        return false
    }

    private fun getArrayDay(numberOfDay: Int, startCalendar: Calendar): ArrayList<Long> {
        val result: ArrayList<Long> = arrayListOf()
        for (i in 1..numberOfDay) {
            result.add(startCalendar.timeInMillis)
            startCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }

    private fun Long.isTheSameDay(dateCompare: Long): Boolean {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = this
        val calendarCompare = Calendar.getInstance(Locale.getDefault())
        calendarCompare.timeInMillis = dateCompare
        return calendar.get(Calendar.DAY_OF_YEAR) == calendarCompare.get(Calendar.DAY_OF_YEAR)
    }

    private fun getDailyCost(todo: Todo): Int {
        val draftNote = getDraftNote(todo.id)
        return draftNote!!.estimateDaily / DEFAULT_SPACE_TIME_MINUTE
    }

    private fun getDailyEstimate(todo: Todo): Int {
        val draftNote = getDraftNote(todo.id)
        return draftNote!!.estimateDaily
    }

    private fun getDraftNote(id: Int?): DraftNote? {
        id?.let {it1 ->
            listDraftNote.forEach {it2 ->
                if (it2.id == it1)
                    return it2
            }
        }
        return null
    }
}

const val DEFAULT_NUMBER_OF_NST = 10
const val DEFAULT_HYBRID_PROBABILITY = 95
const val DEFAULT_MUTATION_PROBABILTY = 5
const val DEFAULT_SPACE_TIME_MINUTE = 30
const val SIXTY_MINUTE = 60