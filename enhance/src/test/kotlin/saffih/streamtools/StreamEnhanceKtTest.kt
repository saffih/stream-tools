package saffih.streamtools


import org.testng.Assert
import org.testng.annotations.Test
import saffih.streamtools.streamUtil.takeWhile
import java.util.function.Predicate
import java.util.stream.Stream
import kotlin.streams.toList


class StreamEnhanceKtTest {
    @Test
    fun testGenerateTillNull() {
        checkStreamOf3(streamUtil.generatorTillNull { i -> if (i < 3) i else null })
    }

    val lessThen3 = { i: Int? -> i == null || i < 3 }

    @Test
    fun testTakeWhileWithPredicate() {
        val generateForever: Stream<Int?> = streamUtil.generatorTillNull { i -> i }
        val predicate = Predicate<Int?> { lessThen3(it) }
        val result = takeWhile(generateForever, predicate)
        checkStreamOf3(result)
    }

    @Test
    fun testStreamTakeWhileWithPredicate() {
        val generateForever: Stream<Int?> = streamUtil.generatorTillNull { i -> i }
        val predicate = Predicate<Int?> { lessThen3(it) }
        val result = generateForever.takeWhile(predicate)
        checkStreamOf3(result)
    }

    @Test
    fun testTakeWhileNullTerminated() {
        checkStreamOf3(takeWhile(Stream.of<Int>(0, 1, 2, null, 4, 5)))
    }

    @Test
    fun testStreamTillNull() {
        checkStreamOf3(Stream.of<Int>(0, 1, 2, null, 4, 5).tillNull())
    }

    @Test
    fun testAutoclose() {
        val cnt = intArrayOf(0)
        val aStream = Stream.of(0, 1, 2).onClose { cnt[0]++ }.autoclose()
        Assert.assertEquals(cnt[0], 0)
        checkStreamOf3(aStream)
        Assert.assertEquals(cnt[0], 1)
    }

    @Test
    fun testJoinStreams() {
        Assert.assertEquals(Stream.empty<Any>().join(Stream.empty<Any>()).toList().size, 0)
        checkStreamOf3(Stream.of(0, 1, 2).join(Stream.empty<Int>()))
        checkStreamOf3((Stream.empty<Int>().join(Stream.of(0, 1, 2))))
        checkStreamOf3((Stream.of(0).join(Stream.of(1, 2))))
        checkStreamOf3((Stream.of(0, 1).join(Stream.of(2))))
    }

    private fun checkStreamOf3(checked: Stream<Int?>) {
        val lst = checked.toList()
        Assert.assertEquals(lst.size, 3)
        Assert.assertEquals(lst[0], 0)
        Assert.assertEquals(lst[2], 2)
    }

}