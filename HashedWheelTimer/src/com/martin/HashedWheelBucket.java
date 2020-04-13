package com.martin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;


/**
 * 时间格
 */
class HashedWheelBucket {

    /**
     * 一个时间格子包含多个待执行的定时任务，通过队列添加
     **/
    private LinkedList<HashedWheelTimeout> linkedList = new LinkedList<>();

    /**
     * 将定时任务添加到对应格子的队列中
     **/
    void addTimeout(HashedWheelTimeout timeout) {
        linkedList.add(timeout);
    }

    /**
     * 遍历，执行该格子（队列）的定时任务
     **/
    void expireTimeouts(long deadline) {
        Iterator<HashedWheelTimeout> iterator = linkedList.iterator();
        while (iterator.hasNext()) {
            HashedWheelTimeout timeout = iterator.next();
            // 定时任务到期，可执行
            if (timeout.getRemainingRounds() <= 0 && timeout.getDeadline() <= deadline) {
                // 从队列中删除
                iterator.remove();
                // 执行定时任务
                timeout.expire();
                continue;
            }
            // 定时任务没到期，轮数减一
            timeout.setRemainingRounds(timeout.getRemainingRounds() - 1);
        }
    }

    /**
     * 当时间轮停止后，将未执行完的任务添加到 set 中
     * @param set
     */
    void clearTimeouts(Set<HashedWheelTimeout> set) {
        while (!linkedList.isEmpty()) {
            // 从格子中获取定时任务
            HashedWheelTimeout timeout = linkedList.removeFirst();
            // 如果任务已过期或取消，则跳过
            if (timeout.isExpired()) {
                continue;
            }
            // 未过期和未取消的定时任务添加到 set 中
            set.add(timeout);
        }
    }
}

