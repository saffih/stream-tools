package saffih.streamtools

import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream
import java.util.stream.StreamSupport

object streamUtil {

    /**
     * add Pending action to the stream
     * @param target
     * @param consumer
     * @return Stream with action when consumed
     */
    fun <T> onConsume(target: Stream<T>, consumer: Consumer<Void?>): Stream<T> {
        return joinStreams(target, createStreamAction(consumer))
    }

    /**
     * @param consumer
     * @param <T>
     * @return Empty stream with call the consumer when read.
     */
    fun <T> createStreamAction(consumer: Consumer<Void?>): Stream<T> {
        return Stream.of<T>(null).peek { consumer.accept(null) }.filter { false }
    }

    /**
     * Join two streams.
     * @param first stream
     * @param second second
     * @return
     */
    fun <T> joinStreams(first: Stream<T>, second: Stream<T>): Stream<T> {
        return Stream.of(first, second).flatMap { i -> i }
    }


    /**
     * Null terminated generator
     */
    fun <T> generatorTillNull(generator: (Int) -> T?): Stream<T> {
        return takeWhile(Stream.iterate(0) { i -> i!! + 1 }.map(generator))
    }

    /**
     * Null terminated stream
     */
    fun <T> takeWhile(stream: Stream<T?>): Stream<T> {
        val predicate = Predicate<T?> { Objects.nonNull(it) }
        return takeWhile(stream, predicate).map { it!! }
    }

    /**
     * takeWhile implementation
     */
    fun <T> takeWhile(stream: Stream<T?>, predicate: Predicate<in T?>): Stream<T?> {
        return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false)
    }

    // https://stackoverflow.com/questions/20746429/limit-a-stream-by-a-predicate
    //***  till we use java 1.9 ***/
    private fun <T> takeWhile(splitr: Spliterator<T?>, predicate: Predicate<in T?>): Spliterator<T?> {
        return object : Spliterators.AbstractSpliterator<T?>(splitr.estimateSize(), 0) {
            var stillGoing = true

            override fun tryAdvance(consumer: Consumer<in T?>): Boolean {
                if (stillGoing) {
                    val hadNext = splitr.tryAdvance { elem ->
                        if (predicate.test(elem)) {
                            consumer.accept(elem)
                        } else {
                            stillGoing = false
                        }
                    }
                    return hadNext && stillGoing
                }
                return false
            }
        }
    }


}

fun <T> Stream<T>.autoclose() = this.onConsume(Consumer { close() })
fun <T> Stream<T>.onConsume(consumer: Consumer<Void?>) = streamUtil.onConsume(this, consumer)
fun <T> Stream<T?>.takeWhile(predicate: Predicate<in T?>) = streamUtil.takeWhile(this, predicate)
fun <T> Stream<T?>.tillNull() = this.tillNullSub().map { it!! }
fun <T> Stream<T>.join(other: Stream<T>) = streamUtil.joinStreams(this, other)

private fun <T> Stream<T?>.tillNullSub() = streamUtil.takeWhile(this, Predicate<T?> { it -> it != null })
