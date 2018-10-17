/*
 * Copyright (c) 2018. Saffi Hartal.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package saffih.streamtools;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings("WeakerAccess")
public class StreamUtilForJava8 {
    /**
     * Generate stream, from generator method till it returns null.
     *
     * @param generator Function<Integer,T>
     * @return <T>
     */
    public static <T> Stream<T> generateTillNull(Function<Integer, T> generator) {
        return takeWhile(Stream.iterate(0, i -> i + 1).map(generator));
    }

    public static <T> Stream<T> takeWhile(Stream<T> stream) {
        return takeWhile(stream, Objects::nonNull);
    }

    public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<? super T> predicate) {
        return StreamSupport.stream(takeWhile(stream.spliterator(), predicate), false);
    }

    // https://stackoverflow.com/questions/20746429/limit-a-stream-by-a-predicate
    //***  till we use java 1.9 ***/
    public static <T> Spliterator<T> takeWhile(
            Spliterator<T> splitr, Predicate<? super T> predicate) {
        return new Spliterators.AbstractSpliterator<T>(splitr.estimateSize(), 0) {
            boolean stillGoing = true;

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (stillGoing) {
                    boolean hadNext = splitr.tryAdvance(elem -> {
                        if (predicate.test(elem)) {
                            consumer.accept(elem);
                        } else {
                            stillGoing = false;
                        }
                    });
                    return hadNext && stillGoing;
                }
                return false;
            }
        };
    }

    /**
     * add Pending action to the stream
     *
     * @return stream with bound consumer
     */
    @NotNull
    public static <T> Stream<T> appendStreamAction(Stream<T> target, Consumer<Void> consumer) {
        return joinStreams(target, createStreamAction(consumer));
    }

    /**
     * Empty stream with call the consumer when read.
     */
    @NotNull
    public static <T> Stream<T> createStreamAction(Consumer<Void> consumer) {
        return Stream.of((T) null).peek(n -> consumer.accept(null)).filter(ignore -> false);
    }

    /**
     * Join two streams.
     */
    @NotNull
    public static <T> Stream<T> joinStreams(Stream<T> first, Stream<T> second) {
        return Stream.of(first, second).flatMap(i -> i);
    }

    /**
     *
     */
    public static <T> Stream<T> autoclosable(Stream<T> aStream) {
        Stream<T> onConsume = createStreamAction((Void) -> aStream.close());
        return Stream.of(aStream, onConsume).flatMap(i -> i);
    }

}
