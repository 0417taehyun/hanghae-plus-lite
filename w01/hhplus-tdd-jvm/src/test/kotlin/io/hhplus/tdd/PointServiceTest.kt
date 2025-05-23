package io.hhplus.tdd

import io.hhplus.tdd.common.UserIdReentrantLock
import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.database.UserPointTable
import io.hhplus.tdd.point.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class PointServiceTest {
    @Test
    @DisplayName("Given 10,000 existing points, When charging 90,000 points, Then total becoming 100,000 points successfully.")
    fun givenExistingPoints_whenChargingPoints_ThenAddingPointsSuccessfully() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 90_000L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val fakeLockManager = FakeLockManager()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTableMock,
            timeUtil = fakeTimeUtil,
            lockManager = fakeLockManager,
        )

        `when`(userPointTable.selectById(id = userId))
            .thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))
        `when`(userPointTable.insertOrUpdate(id = userId, amount = existingPoint + amount))
            .thenReturn(UserPoint(id = userId, point = existingPoint + amount, updateMillis = fakeUpdateMilliseconds))
        `when`(
            pointHistoryTableMock.insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.CHARGE,
                updateMillis = fakeUpdateMilliseconds
            )
        ).thenReturn(
            PointHistory(
                id = 1L,
                userId = userId,
                amount = amount,
                type = TransactionType.CHARGE,
                timeMillis = fakeUpdateMilliseconds
            )
        )

        // When
        val result = pointService.charge(userId = userId, amount = amount)

        // Then
        assertThat(result)
            .isEqualTo(UserPoint(id = userId, point = existingPoint + amount, updateMillis = fakeUpdateMilliseconds))

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, times(1)).insertOrUpdate(id = userId, amount = existingPoint + amount)
        verify(pointHistoryTableMock, times(1))
            .insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.CHARGE,
                updateMillis = fakeUpdateMilliseconds
            )
    }

    @Test
    @DisplayName("Given 10,000 existing points, When charging 90,001 points, Then throw IllegalAmountChargeException.")
    fun givenExistingPoints_whenChargingPointsOverMaximum_ThenThrowException() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 90_001L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val fakeLockManager = FakeLockManager()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTableMock,
            timeUtil = fakeTimeUtil,
            lockManager = fakeLockManager,
        )

        `when`(userPointTable.selectById(id = userId)).
        thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))

        // When
        // Then
        assertThrows<PointException.IllegalAmountChargeException> {
            pointService.charge(userId = userId, amount = amount)
        }

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, never()).insertOrUpdate(id = userId, amount = existingPoint + amount)
        verify(pointHistoryTableMock, never()).insert(id = userId, amount = amount, transactionType = TransactionType.CHARGE, updateMillis = fakeUpdateMilliseconds)
    }

    @Test
    @DisplayName("Given 10,000 existing points, When using 10,000 points, Then left point becoming 0 successfully.")
    fun givenExistingPoints_whenUsingPoints_ThenBalancingPointsSuccessfully() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 10_000L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val fakeLockManager = FakeLockManager()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTableMock,
            timeUtil = fakeTimeUtil,
            lockManager = fakeLockManager,
        )

        `when`(userPointTable.selectById(id = userId))
            .thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))
        `when`(userPointTable.insertOrUpdate(id = userId, amount = existingPoint - amount))
            .thenReturn(UserPoint(id = userId, point = existingPoint - amount, updateMillis = fakeUpdateMilliseconds))
        `when`(
            pointHistoryTableMock.insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.USE,
                updateMillis = fakeUpdateMilliseconds
            )
        ).thenReturn(
            PointHistory(
                id = 1L,
                userId = userId,
                amount = amount,
                type = TransactionType.USE,
                timeMillis = fakeUpdateMilliseconds
            )
        )

        // When
        val result = pointService.use(userId = userId, amount = amount)

        // Then
        assertThat(result)
            .isEqualTo(UserPoint(id = userId, point = existingPoint - amount, updateMillis = fakeUpdateMilliseconds))

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, times(1)).insertOrUpdate(id = userId, amount = existingPoint - amount)
        verify(pointHistoryTableMock, times(1))
            .insert(
                id = userId,
                amount = amount,
                transactionType = TransactionType.USE,
                updateMillis = fakeUpdateMilliseconds
            )
    }

    @Test
    @DisplayName("Given 10,000 existing points, When using 10,001 points, Then throw IllegalAmountUseException.")
    fun givenExistingPoints_whenUsingPointsOverExisting_ThenThrowException() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val amount = 10_001L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val fakeLockManager = FakeLockManager()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTableMock,
            timeUtil = fakeTimeUtil,
            lockManager = fakeLockManager,
        )

        `when`(userPointTable.selectById(id = userId))
            .thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))

        // When
        // Then
        assertThrows<PointException.IllegalAmountUseException> {
            pointService.use(userId = userId, amount = amount)
        }

        verify(userPointTable, times(1)).selectById(id = userId)
        verify(userPointTable, never()).insertOrUpdate(id = userId, amount = existingPoint - amount)
        verify(pointHistoryTableMock, never()).insert(id = userId, amount = amount, transactionType = TransactionType.USE, updateMillis = fakeUpdateMilliseconds)
    }

    @Test
    @DisplayName("Given existing user point, When passing matched user id, Then returning the matched user point successfully.")
    fun givenExistingUserPoint_whenPassingMatchedUserId_ThenReturnUserPointSuccessfully() {
        // Given
        val userId = 1L
        val existingPoint = 10_000L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val fakeLockManager = FakeLockManager()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTableMock,
            timeUtil = fakeTimeUtil,
            lockManager = fakeLockManager,
        )

        `when`(userPointTable.selectById(id = userId))
            .thenReturn(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))

        // When
        val result = pointService.get(userId = userId)

        // Then
        assertThat(result).isEqualTo(UserPoint(id = userId, point = existingPoint, updateMillis = fakeUpdateMilliseconds))
    }

    @Test
    @DisplayName("Given two transactions, When passing matched user id, Then returning a history of two transactions successfully.")
    fun givenExistingPointHistory_whenPassingMatchedUserId_ThenReturningListOfPointHistorySuccessfully() {
        // Given
        val userId = 1L
        val amount = 10_000L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = mock(UserPointTable::class.java)
        val pointHistoryTableMock = mock(PointHistoryTable::class.java)
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val fakeLockManager = FakeLockManager()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTableMock,
            timeUtil = fakeTimeUtil,
            lockManager = fakeLockManager,
        )

        val chargeTransaction = PointHistory(id = 1L, userId = userId, type = TransactionType.CHARGE, amount = amount, timeMillis = fakeUpdateMilliseconds)
        val useTransaction = PointHistory(id = 2L, userId = userId, type = TransactionType.USE, amount = amount, timeMillis = fakeUpdateMilliseconds)

        `when`(pointHistoryTableMock.selectAllByUserId(userId = userId)).thenReturn(listOf(chargeTransaction, useTransaction))

        // When
        val result = pointService.getHistories(userId = userId)

        // Then
        assertThat(result).isEqualTo(listOf(chargeTransaction, useTransaction))
    }

    @Test
    @DisplayName("")
    fun givenTwoChargeTransactions_whenExecutingConcurrently_ThenApplyingAllTransactionsSuccessfully() {
        // Given
        val userId = 1L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = UserPointTable()
        val pointHistoryTable = PointHistoryTable()
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val lockManager = UserIdReentrantLock()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTable,
            timeUtil = fakeTimeUtil,
            lockManager = lockManager,
        )

        val readyLatch = CountDownLatch(2)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        // When
        repeat(2) {
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                pointService.charge(userId = userId, amount = 50L)
            }
        }

        readyLatch.await()
        startLatch.countDown()

        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // Then
        assertThat(userPointTable.selectById(id = userId).point).isEqualTo(100L)
    }

    @Test
    @DisplayName("")
    fun givenTwoUseTransactions_whenExecutingConcurrently_ThenApplyingAllTransactionsSuccessfully() {
        // Given
        val userId = 1L
        val amount = 100L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = UserPointTable()
        val pointHistoryTable = PointHistoryTable()
        val fakeTimeUtil = FakeTimeUtil(fixedTime = fakeUpdateMilliseconds)
        val lockManager = UserIdReentrantLock()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTable,
            timeUtil = fakeTimeUtil,
            lockManager = lockManager,
        )

        userPointTable.insertOrUpdate(id = userId, amount = amount)

        val readyLatch = CountDownLatch(2)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        // When
        repeat(2) {
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                pointService.use(userId = userId, amount = 50L)
            }
        }

        readyLatch.await()
        startLatch.countDown()

        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // Then
        assertThat(userPointTable.selectById(id = userId).point).isEqualTo(0L)
    }

    @Test
    @DisplayName("")
    fun givenBothChargeAndUseTransactions_whenChargeShouldBeExecutedFirst_ThenApplyAllTransactionsSuccessfully() {
        // Given
        val userId = 1L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = UserPointTable()
        val pointHistoryTable = PointHistoryTable()
        val fakeTimeUtil = FakeTimeUtil(fakeUpdateMilliseconds)
        val lockManager = UserIdReentrantLock()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTable,
            timeUtil = fakeTimeUtil,
            lockManager = lockManager,
        )

        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        val exceptions = AtomicReference<Throwable?>(null)

        // When
        executor.submit {
            ready.countDown()
            start.await()
            try {
                pointService.charge(userId = userId, amount = 100L)
            }
            catch (throwable: Throwable) {
                exceptions.compareAndSet(null, throwable)
            }
        }
        executor.submit {
            ready.countDown()
            start.await()
            try {
                pointService.use(userId = userId, amount = 50L)
            }
            catch (throwable: Throwable) {
                exceptions.compareAndSet(null, throwable)
            }
        }

        ready.await()
        start.countDown()

        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        // Then
        assertThat(exceptions.get()).isNull()
        assertThat(userPointTable.selectById(userId).point).isEqualTo(50L)
    }

    @Test
    @DisplayName("")
    fun givenBothChargeAndGetTransactions_whenChargeShouldBeExecutedFirst_ThenApplyAllTransactionsSuccessfully() {
        // Given
        val userId = 1L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = UserPointTable()
        val pointHistoryTable = PointHistoryTable()
        val fakeTimeUtil = FakeTimeUtil(fakeUpdateMilliseconds)
        val lockManager = UserIdReentrantLock()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTable,
            timeUtil = fakeTimeUtil,
            lockManager = lockManager,
        )

        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        // When
        executor.submit {
            ready.countDown()
            start.await()
            pointService.charge(userId = userId, amount = 100L)
        }
        val getResult = executor.submit<UserPoint> {
            ready.countDown()
            start.await()
            pointService.get(userId = userId)
        }

        ready.await()
        start.countDown()

        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        val result = getResult.get()

        // Then
        assertThat(result.point).isEqualTo(100L)
    }

    @Test
    @DisplayName("")
    fun givenBothChargeAndHistoryTransactions_whenChargeShouldBeExecutedFirst_ThenApplyAllTransactionsSuccessfully() {
        // Given
        val userId = 1L
        val fakeUpdateMilliseconds = 1_000L

        val userPointTable = UserPointTable()
        val pointHistoryTable = PointHistoryTable()
        val fakeTimeUtil = FakeTimeUtil(fakeUpdateMilliseconds)
        val lockManager = UserIdReentrantLock()

        val pointService = PointService(
            userPointTable = userPointTable,
            pointHistoryTable = pointHistoryTable,
            timeUtil = fakeTimeUtil,
            lockManager = lockManager,
        )

        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)

        // When
        executor.submit {
            ready.countDown()
            start.await()
            pointService.charge(userId = userId, amount = 100L)
        }
        val getHistoriesResult = executor.submit<List<PointHistory>> {
            ready.countDown()
            start.await()
            pointService.getHistories(userId = userId)
        }

        ready.await()
        start.countDown()

        executor.shutdown()
        executor.awaitTermination(1, TimeUnit.MINUTES)

        val result = getHistoriesResult.get()[0]

        // Then
        assertThat(result.amount).isEqualTo(100L)
        assertThat(result.type).isEqualTo(TransactionType.CHARGE)
    }
}