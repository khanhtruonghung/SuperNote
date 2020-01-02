package com.truongkhanh.supernote.service

import com.truongkhanh.supernote.model.DraftNote
import com.truongkhanh.supernote.model.MyCalendar
import com.truongkhanh.supernote.model.ScheduleItem
import com.truongkhanh.supernote.model.Todo
import com.truongkhanh.supernote.utils.NULL_STRING
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
    private val random = Random(Calendar.getInstance(Locale.getDefault()).timeInMillis)
    private var spaceTimes: HashMap<Long, Todo?> = hashMapOf()
    private var population: HashMap<HashMap<Long, Todo?>, Int> = hashMapOf()

    private var listNoteCantOptimize: HashMap<DraftNote, String> = hashMapOf()
    private var criticalError: String? = null

    private var startDate: Long = 0L
    private var endDate: Long = 0L
    private var startTime: MyCalendar = MyCalendar(0, 0, 0, 0, 0)
    private var endTime: MyCalendar = MyCalendar(0, 0, 0, 0, 0)
    private lateinit var listDraftNote: MutableList<DraftNote>

    private fun showError() {
        val throwable = Throwable(criticalError)
        observable.onError(throwable)
    }

    fun startAlgorithm(
        listDraftNote: MutableList<DraftNote>,
        startDate: Long,
        endDate: Long,
        startTime: MyCalendar,
        endTime: MyCalendar
    ): Observable<MutableList<Todo>> {
//        observable = Observable.fromCallable<MutableList<Todo>> {
        return Observable.create {
            this.observable = it
            this.startDate = startDate
            this.endDate = endDate
            this.startTime = startTime
            this.endTime = endTime
            this.listDraftNote = listDraftNote

            // Tạo ra một biến map<Long, DraftNote?> để lưu khoảng thời gian
            // mà khoảng đó được dùng để chèn công việc vào.
            spaceTimes = createSpaceTimes(startDate, endDate, startTime, endTime)

            // Sắp xếp danh sách công việc theo mức độ ưu tiên cao đến thấp
            listDraftNote.sortByDescending {draftNote ->
                draftNote.priority
            }

            // Dùng map spaceTimes và fill vào đó công việc theo ngày,
            // với mỗi công việc 1 ngày làm 1 lần.
            // Mỗi spaceTimes là 1 nhiễm sắc thể.
            for (index in 1..DEFAULT_NUMBER_OF_NST) {
                val spaceTimesClone = HashMap(spaceTimes)
                fillSpaceTimeWithTodo(listDraftNote, spaceTimesClone)?.let { spaceTimeFilled ->
                    population.plus(Pair(spaceTimeFilled, 0))
                } ?: showError()
            }

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
                startHybrid(listPopulation[0].first, listPopulation[1].first)
                startHybrid(listPopulation[2].first, listPopulation[3].first)
            } else {
                startMutation(listPopulation[0].first)
                startMutation(listPopulation[1].first)
                startMutation(listPopulation[2].first)
                startMutation(listPopulation[3].first)
            }

            // Sắp xếp lại danh sách
            // Lấy ra lịch có điểm cao nhất
            val sortedMap2 = population.toList().sortedByDescending { (_, point) ->
                point
            }.toMap()
            population = HashMap(sortedMap2)

            val bestIndividual = population.keys.toList()
            val individualNullable = bestIndividual[0].values.toMutableList()
            val individualNonNull = individualNullable.filterNotNull()

            it.onNext(individualNonNull.toMutableList())
            it.onComplete()
        }
    }

    private fun startMutation(mutation: HashMap<Long, Todo?>) {
        val noteID = listDraftNote[random.nextInt(listDraftNote.size)].id
        val mutationClone = mutation.clone() as HashMap<Long, Todo?>

        mutationClone.filterValues { todo ->
            todo!!.id == noteID
        }
        var child = mutation.clone() as HashMap<Long, Todo?>
        mutationClone.forEach {
            child.remove(it.key)
        }
        mutationLogic(child, noteID)?.let{
            child = it
        }?:showError()

        val childPoint = random.nextInt(100)
        population.plus(Pair(child, childPoint))
    }

    private fun mutationLogic(child: HashMap<Long, Todo?>, noteID: Int): HashMap<Long, Todo?>? {
        val childClone = HashMap(child)
        val freeSpaceTimes = getFreeSpaceTimes(childClone)
        val scheduleItems: MutableList<ScheduleItem> = mutableListOf()

        var dailyEstimate = 0
        var dailyCost = 0
        var totalEstimate = 0
        var mNote: DraftNote? = null
        listDraftNote.forEach { note ->
            if (note.id == noteID) {
                mNote = note
                dailyEstimate = note.estimateDaily
                dailyCost = note.estimateDaily / DEFAULT_SPACE_TIME_MINUTE
                totalEstimate = note.estimateTotal
                return@forEach
            }
        }

        do {
            // Nếu thời gian rãnh bị null hoặc empty thì báo lỗi không đủ thời gian sắp xếp
            if (!freeSpaceTimes.isNullOrEmpty()) {
                // Nếu không còn ngày nào làm việc được thì break
                if (scheduleItems.isNotEnoughDayToWork()) {
                    criticalError = "Don't have enough day to work"
                    return null
                }

                // Chọn ngẫu nhiên 1 space time để làm việc
                val spaceTime =
                    freeSpaceTimes.entries.elementAt(random.nextInt(freeSpaceTimes.size))
                val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                spaceTimeStart.timeInMillis = spaceTime.key
                val dayOfSpaceTime = spaceTimeStart.get(Calendar.DAY_OF_MONTH)
                val monthOfSpaceTime = spaceTimeStart.get(Calendar.MONTH)

                // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
                if (scheduleItems.containDate(dayOfSpaceTime, monthOfSpaceTime)) {
                    freeSpaceTimes.remove(spaceTime.key)
                    continue
                }

                // Nếu công việc bị gián đoạn, tức không liên tục thì bắt đầu lại từ đầu và xóa spaceTime ở đó đi
                if (!isWorkDisrupt(freeSpaceTimes, spaceTime.key, dailyCost)) {
                    val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                    scheduleItem.timeStart = MyCalendar(
                        0,
                        0,
                        0,
                        spaceTimeStart.get(Calendar.MINUTE),
                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                    )
                    spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                    scheduleItem.timeEnd = MyCalendar(
                        0,
                        0,
                        0,
                        spaceTimeStart.get(Calendar.MINUTE),
                        spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                    )
                    scheduleItems.add(scheduleItem)

                    // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                    val calendar = Calendar.getInstance(Locale.getDefault())
                    calendar.timeInMillis = spaceTime.key
                    for (i in 1..dailyCost) {
                        calendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                        childClone[calendar.timeInMillis] = Todo(
                            id = mNote!!.id,
                            title = mNote?.title,
                            description = mNote?.description,
                            checkList = NULL_STRING,
                            priority = mNote?.priority,
                            startDate = 0L,
                            endDate = 0L,
                            isAllDay = false,
                            alertType = AlertPickerDialogFragment.NO_ALERT,
                            checkDoneDate = 0L,
                            isDone = false,
                            notificationRequestID = NULL_STRING,
                            schedule = NULL_STRING
                        )
                    }
                } else {
                    freeSpaceTimes.remove(spaceTime.key)
                    continue
                }
            } else {
                // Nếu draftNote có mức độ ưu tiên thấp thì không vi phạm ràng buộc cứng.
                // Do đó có thể dừng ở đây.
                if (mNote?.priority == LOW_PRIORITY) {
                    break
                } else {
                    criticalError = "Don't have enough space times to optimize"
                    return null
                }
            }

            // Giảm tổng thời gian sau mỗi vòng lập 1
            totalEstimate -= dailyEstimate
        } while (totalEstimate > 0)
        childClone.forEach {
            if (it.value?.id == mNote?.id) {
                it.value?.schedule = scheduleItems.scheduleItemsToString()
            }
        }
        return childClone
    }

    private fun startHybrid(dad: HashMap<Long, Todo?>, mom: HashMap<Long, Todo?>) {
        val noteID = listDraftNote[random.nextInt(listDraftNote.size)].id
        val dadTodo = dad.clone() as HashMap<Long, Todo?>
        val momTodo = mom.clone() as HashMap<Long, Todo?>

        dadTodo.filterValues {todo ->
            todo!!.id == noteID
        }
        momTodo.filterValues { todo ->
            todo!!.id == noteID
        }

        var child1 = dad.clone() as HashMap<Long, Todo?>
        var child2 = mom.clone() as HashMap<Long, Todo?>

        dadTodo.forEach {
            child1.remove(it.key)
        }
        momTodo.forEach {
            child2.remove(it.key)
        }

        hybridLogic(child1, momTodo)?.let{
            child1 = it
        } ?: showError()
        hybridLogic(child2, dadTodo)?.let{
            child2 = it
        } ?: showError()

        val child1Point = random.nextInt(100)
        population.plus(Pair(child1, child1Point))

        val child2Point = random.nextInt(100)
        population.plus(Pair(child2, child2Point))
    }

    private fun hybridLogic(child: HashMap<Long, Todo?>, parent: HashMap<Long, Todo?>): HashMap<Long, Todo?>? {
        val childClone = HashMap(child)
        val freeSpaceTimes = getFreeSpaceTimes(childClone)
        val scheduleItems: MutableList<ScheduleItem> = mutableListOf()

        var dailyEstimate = 0
        var dailyCost = 0
        var totalEstimate = 0
        listDraftNote.forEach { note ->
            if (note.id == parent.entries.elementAt(0).value!!.id) {
                dailyEstimate = note.estimateDaily
                dailyCost = note.estimateDaily / DEFAULT_SPACE_TIME_MINUTE
                totalEstimate = note.estimateTotal
                return@forEach
            }
        }

        parent.forEach {
            if (freeSpaceTimes.containsKey(it.key)) {
                if (scheduleItems.isNotEnoughDayToWork()) {
                    criticalError = "Don't have enough day to work"
                    return@hybridLogic null
                }

                val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                spaceTimeStart.timeInMillis = it.key
                val dayOfSpaceTime = spaceTimeStart.get(Calendar.DAY_OF_MONTH)
                val monthOfSpaceTime = spaceTimeStart.get(Calendar.MONTH)

                if (scheduleItems.containDate(dayOfSpaceTime, monthOfSpaceTime)) {
                    freeSpaceTimes.remove(it.key)
                } else {
                    if (!isWorkDisrupt(freeSpaceTimes, it.key, dailyCost)) {
                        val calendar = Calendar.getInstance(Locale.getDefault())
                        calendar.timeInMillis = it.key
                        val scheduleItem = ScheduleItem(it.key, null, null)
                        scheduleItem.timeStart = MyCalendar(
                            0,
                            0,
                            0,
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.HOUR_OF_DAY)
                        )
                        calendar.add(Calendar.MINUTE, dailyEstimate)
                        scheduleItem.timeEnd = MyCalendar(
                            0,
                            0,
                            0,
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.HOUR_OF_DAY)
                        )
                        scheduleItems.add(scheduleItem)

                        val calendar2 = Calendar.getInstance(Locale.getDefault())
                        calendar2.timeInMillis = it.key
                        for (i in 1..dailyCost) {
                            calendar2.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                            childClone[calendar2.timeInMillis] = it.value
                        }
                    }
                }
            } else {
                // Đây là trường hợp không thể thêm công việc của parent vào đúng chỗ của nó được
                // Vì vậy phải tìm một chỗ khác để sắp xếp công việc đó vào
                do {
                    // Nếu thời gian rãnh bị null hoặc empty thì báo lỗi không đủ thời gian sắp xếp
                    if (!freeSpaceTimes.isNullOrEmpty()) {
                        // Nếu không còn ngày nào làm việc được thì break
                        if (scheduleItems.isNotEnoughDayToWork()) {
                            criticalError = "Don't have enough day to work"
                            return@hybridLogic null
                        }

                        // Chọn ngẫu nhiên 1 space time để làm việc
                        val spaceTime =
                            freeSpaceTimes.entries.elementAt(random.nextInt(freeSpaceTimes.size))
                        val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                        spaceTimeStart.timeInMillis = spaceTime.key
                        val dayOfSpaceTime = spaceTimeStart.get(Calendar.DAY_OF_MONTH)
                        val monthOfSpaceTime = spaceTimeStart.get(Calendar.MONTH)

                        // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
                        if (scheduleItems.containDate(dayOfSpaceTime, monthOfSpaceTime)) {
                            freeSpaceTimes.remove(spaceTime.key)
                            continue
                        }

                        // Nếu công việc bị gián đoạn, tức không liên tục thì bắt đầu lại từ đầu và xóa spaceTime ở đó đi
                        if (!isWorkDisrupt(freeSpaceTimes, spaceTime.key, dailyCost)) {
                            val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                            scheduleItem.timeStart = MyCalendar(
                                0,
                                0,
                                0,
                                spaceTimeStart.get(Calendar.MINUTE),
                                spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                            )
                            spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                            scheduleItem.timeEnd = MyCalendar(
                                0,
                                0,
                                0,
                                spaceTimeStart.get(Calendar.MINUTE),
                                spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                            )
                            scheduleItems.add(scheduleItem)

                            // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                            val calendar2 = Calendar.getInstance(Locale.getDefault())
                            calendar2.timeInMillis = spaceTime.key
                            for (i in 1..dailyCost) {
                                calendar2.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                                childClone[calendar2.timeInMillis] = it.value
                            }
                        } else {
                            freeSpaceTimes.remove(spaceTime.key)
                            continue
                        }
                    } else {
                        // Nếu draftNote có mức độ ưu tiên thấp thì không vi phạm ràng buộc cứng.
                        // Do đó có thể dừng ở đây.
                        if (it.value!!.priority == LOW_PRIORITY) {
                            break
                        } else {
                            criticalError = "Don't have enough space times to optimize"
                            return null
                        }
                    }

                    // Giảm tổng thời gian sau mỗi vòng lập 1 khoang bang daily estimate
                    totalEstimate -= dailyEstimate
                } while (totalEstimate > 0)
                childClone.forEach {childTodo ->
                    if (childTodo.value!!.id == it.value!!.id) {
                        childTodo.value!!.schedule = scheduleItems.scheduleItemsToString()
                    }
                }
            }
        }
        return childClone
    }

    private fun fillSpaceTimeWithTodo(
        listDraftNote: MutableList<DraftNote>,
        spaceTimes: HashMap<Long, Todo?>
    ): HashMap<Long, Todo?>? {
        val spaceTimesClone = HashMap(spaceTimes)

        // Lấy toàn bộ thời gian rãnh còn lại
        val freeSpaceTimes = getFreeSpaceTimes(spaceTimesClone)

        listDraftNote.forEach { draftNote ->

            var totalEstimate = (draftNote.estimateTotal * SIXTY_MINUTE)
            val dailyEstimate = draftNote.estimateDaily
            val dailyCost = draftNote.estimateDaily / DEFAULT_SPACE_TIME_MINUTE
            val scheduleItems: MutableList<ScheduleItem> = mutableListOf()

            do {
                // Nếu thời gian rãnh bị null hoặc empty thì báo lỗi không đủ thời gian sắp xếp
                if (!freeSpaceTimes.isNullOrEmpty()) {
                    // Nếu không còn ngày nào làm việc được thì break
                    if (scheduleItems.isNotEnoughDayToWork()) {
                        criticalError = "Don't have enough day to work"
                        return@fillSpaceTimeWithTodo null
                    }

                    // Chọn ngẫu nhiên 1 space time để làm việc
                    val spaceTime =
                        freeSpaceTimes.entries.elementAt(random.nextInt(freeSpaceTimes.size))
                    val spaceTimeStart = Calendar.getInstance(Locale.getDefault())
                    spaceTimeStart.timeInMillis = spaceTime.key
                    val dayOfSpaceTime = spaceTimeStart.get(Calendar.DAY_OF_MONTH)
                    val monthOfSpaceTime = spaceTimeStart.get(Calendar.MONTH)

                    // Nếu công việc đã làm ở ngày này thì bắt đầu lại từ đầu và xóa spaceTime ở đấy đi
                    if (scheduleItems.containDate(dayOfSpaceTime, monthOfSpaceTime)) {
                        freeSpaceTimes.remove(spaceTime.key)
                        continue
                    }

                    // Nếu công việc bị gián đoạn, tức không liên tục thì bắt đầu lại từ đầu và xóa spaceTime ở đó đi
                    if (!isWorkDisrupt(freeSpaceTimes, spaceTime.key, dailyCost)) {
                        val scheduleItem = ScheduleItem(spaceTimeStart.timeInMillis, null, null)
                        scheduleItem.timeStart = MyCalendar(
                            0,
                            0,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        spaceTimeStart.add(Calendar.MINUTE, dailyEstimate)
                        scheduleItem.timeEnd = MyCalendar(
                            0,
                            0,
                            0,
                            spaceTimeStart.get(Calendar.MINUTE),
                            spaceTimeStart.get(Calendar.HOUR_OF_DAY)
                        )
                        scheduleItems.add(scheduleItem)

                        // Thêm vào spaceTimesClone công việc cần làm trong khoảng từ A -> B thời gian
                        val calendar = Calendar.getInstance(Locale.getDefault())
                        calendar.timeInMillis = spaceTime.key
                        for (i in 1..dailyCost) {
                            calendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                            spaceTimesClone[calendar.timeInMillis] = Todo(
                                id = draftNote.id,
                                title = draftNote.title,
                                description = draftNote.description,
                                checkList = NULL_STRING,
                                priority = draftNote.priority,
                                startDate = 0L,
                                endDate = 0L,
                                isAllDay = false,
                                alertType = AlertPickerDialogFragment.NO_ALERT,
                                checkDoneDate = 0L,
                                isDone = false,
                                notificationRequestID = NULL_STRING,
                                schedule = NULL_STRING
                            )
                        }
                    } else {
                        freeSpaceTimes.remove(spaceTime.key)
                        continue
                    }
                } else {
                    // Nếu draftNote có mức độ ưu tiên thấp thì không vi phạm ràng buộc cứng.
                    // Do đó có thể dừng ở đây.
                    if (draftNote.priority == LOW_PRIORITY) {
                        break
                    } else {
                        criticalError = "Don't have enough space times to optimize"
                        return null
                    }
                }

                // Giảm tổng thời gian sau mỗi vòng lập 1
                totalEstimate -= dailyEstimate
            } while (totalEstimate > 0)
            spaceTimesClone.forEach {
                if (it.value!!.id == draftNote.id) {
                    it.value!!.schedule = scheduleItems.scheduleItemsToString()
                }
            }
        }
        return spaceTimesClone
    }

    private fun isWorkDisrupt(
        freeSpaceTimes: HashMap<Long, Todo?>,
        start: Long,
        dailyCost: Int
    ): Boolean {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = start
        for (i in 1..dailyCost) {
            if (freeSpaceTimes.containsKey(calendar.timeInMillis)) {
                calendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
            } else {
                return true
            }
        }
        return false
    }

    private fun MutableList<ScheduleItem>.containDate(day: Int, month: Int): Boolean {
        this.forEach {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = it.date
            if (calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.MONTH) == month)
                return true
        }
        return false
    }

    private fun MutableList<ScheduleItem>.isNotEnoughDayToWork(): Boolean {
        if (this.isNullOrEmpty())
            return false

        val start = Calendar.getInstance(Locale.getDefault())
        start.timeInMillis = startDate
        var dayStart = start.get(Calendar.DAY_OF_MONTH)
        var monthStart = start.get(Calendar.MONTH)

        val end = Calendar.getInstance(Locale.getDefault())
        end.timeInMillis = endDate
        val dayEnd = start.get(Calendar.DAY_OF_MONTH)

        do {
            if (!this.containDate(dayStart, monthStart))
                return true
            start.add(Calendar.DAY_OF_MONTH, 1)
            dayStart = start.get(Calendar.DAY_OF_MONTH)
            monthStart = start.get(Calendar.MONTH)
        } while (dayStart <= dayEnd)
        return false
    }

    @Suppress("UNUSED_VALUE")
    private fun createSpaceTimes(
        startDate: Long,
        endDate: Long,
        startTime: MyCalendar,
        endTime: MyCalendar
    ): HashMap<Long, Todo?> {
        val startCalendar = Calendar.getInstance(Locale.getDefault())
        startCalendar.timeInMillis = startDate
        startCalendar.set(Calendar.HOUR_OF_DAY, startTime.hour)
        startCalendar.set(Calendar.MINUTE, startTime.minute)

        val endCalendar = Calendar.getInstance(Locale.getDefault())
        endCalendar.timeInMillis = endDate
        endCalendar.set(Calendar.HOUR_OF_DAY, endTime.hour)
        endCalendar.set(Calendar.MINUTE, endTime.minute)

        var currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
        val endDay = endCalendar.get(Calendar.DAY_OF_MONTH)
        val spaceTimes: HashMap<Long, Todo?> = hashMapOf()

        while (currentDay <= endDay) {
            val startHour = startCalendar.get(Calendar.HOUR_OF_DAY)
            val endHour = endCalendar.get(Calendar.HOUR_OF_DAY)

            val startMinute = startCalendar.get(Calendar.MINUTE)
            val endMinute = endCalendar.get(Calendar.MINUTE)

            spaceTimes.plus(Pair(startCalendar.timeInMillis, null))

            when {
                (startHour > endHour) -> {
                    startCalendar.add(Calendar.DAY_OF_MONTH, 1)
                    currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
                }
                (startHour == endHour) -> {
                    if ((endMinute - startMinute) > 0) {
                        startCalendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                    } else {
                        startCalendar.add(Calendar.DAY_OF_MONTH, 1)
                        currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
                    }
                }
                (startHour < endHour) -> {
                    startCalendar.add(Calendar.MINUTE, DEFAULT_SPACE_TIME_MINUTE)
                }
            }

            currentDay = startCalendar.get(Calendar.DAY_OF_MONTH)
        }
        return spaceTimes
    }

    private fun getFreeSpaceTimes(spaceTimesInput: HashMap<Long, Todo?>): HashMap<Long, Todo?> {
        return spaceTimesInput.filterValues {
            it == null
        } as HashMap<Long, Todo?>
    }
}

const val DEFAULT_NUMBER_OF_NST = 10
const val DEFAULT_HYBRID_PROBABILITY = 95
const val DEFAULT_MUTATION_PROBABILTY = 5
const val DEFAULT_SPACE_TIME_MINUTE = 30
const val SIXTY_MINUTE = 60