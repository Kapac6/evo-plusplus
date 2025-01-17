package ru.dargen.evoplus.scheduler

import ru.dargen.evoplus.scheduler.task.Task
import ru.dargen.evoplus.scheduler.task.TaskOrder
import java.util.concurrent.TimeUnit

fun async(block: () -> Unit) = Scheduler.Executor.execute(block)

fun every(
    delay: Int = 1, period: Int = 1,
    repeats: Int = -1, unit: TimeUnit? = null,
    order: TaskOrder = TaskOrder.TICK_PRE,
    action: (Task) -> Unit
) = Scheduler.runTicking(delay, period, repeats, unit, true, order, action)

fun everyAsync(
    delay: Int = 1, period: Int = 1,
    repeats: Int = -1, unit: TimeUnit? = null,
    order: TaskOrder = TaskOrder.TICK_PRE,
    action: (Task) -> Unit
) = Scheduler.runTicking(delay, period, repeats, unit, false, order, action)

fun after(
    delay: Int = 1, unit: TimeUnit? = null,
    order: TaskOrder = TaskOrder.TICK_PRE,
    action: (Task) -> Unit
) = Scheduler.runTicking(delay, delay, 1, unit, true, order, action)

fun afterAsync(
    delay: Int = 1, unit: TimeUnit? = null,
    order: TaskOrder = TaskOrder.TICK_PRE,
    action: (Task) -> Unit
) = Scheduler.runTicking(delay, delay, 1, unit, false, order, action)

fun scheduleEvery(
    delay: Int = 1, period: Int = 1,
    repeats: Int = -1, unit: TimeUnit? = null,
    action: (Task) -> Unit
) = Scheduler.runAsync(delay, period, repeats, unit, action)

fun schedule(
    delay: Int = 1, unit: TimeUnit? = null,
    action: (Task) -> Unit
) = Scheduler.runAsync(delay, delay, 1, unit, action)