package saffih.streamtools;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static saffih.streamtools.StreamUtilForJava8.*;


public class StreamUtilForJava8Test {

    private static boolean lessThen3(Integer i) {
        return i < 3;
    }

    @Test
    public void testGenerateTillNull() {
        checkStreamOf3(generateTillNull((i) -> i < 3 ? i : null));
    }

    @Test
    public void testTakeWhileWitjPredicate() {
        Stream<Integer> generateForever = generateTillNull((i) -> i);
        checkStreamOf3(takeWhile(generateForever, StreamUtilForJava8Test::lessThen3));

    }

    @Test
    public void testTakeWhileNullTerminated() {
        checkStreamOf3(takeWhile(Stream.of(0, 1, 2, null, 4, 5)));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testAppendStreamActionOnEmptyStream() {
        int cnt[] = {0};
        Stream<Object> withAction = appendStreamAction(Stream.empty(), (Void) -> cnt[0]++);
        Assert.assertEquals(cnt[0], 0);
        withAction.findFirst(); // not fully read
        Assert.assertEquals(cnt[0], 1);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testAppendStreamActionNotFullyRead() {
        int cnt[] = {0};
        Stream<Object> withAction = appendStreamAction(Stream.of(0, 1, 2), (Void) -> cnt[0]++);
        Assert.assertEquals(cnt[0], 0);
        withAction.findFirst(); // not fully read
        Assert.assertEquals(cnt[0], 0);
    }

    @Test
    public void testAppendStreamActionOnStream() {
        int cnt[] = {0};
        Stream<Integer> withAction = appendStreamAction(Stream.of(0, 1, 2), (Void) -> cnt[0]++);
        Assert.assertEquals(cnt[0], 0);
        checkStreamOf3(withAction);
        Assert.assertEquals(cnt[0], 1);
    }

    @Test
    public void testCreateStreamAction() {
        int cnt[] = {0};
        Stream<Integer> withAction = createStreamAction((Void) -> cnt[0]++);
        Assert.assertEquals(cnt[0], 0);
        withAction.forEach((i) ->
                Assert.assertEquals(cnt[0], 0));
        Assert.assertEquals(cnt[0], 1);

    }

    @Test
    public void testJoinStreams() {
        Assert.assertEquals(joinStreams(Stream.empty(), Stream.empty()).collect(Collectors.toList()).size(), 0);
        checkStreamOf3(joinStreams(Stream.of(0, 1, 2), Stream.empty()));
        checkStreamOf3(joinStreams(Stream.empty(), Stream.of(0, 1, 2)));
        checkStreamOf3(joinStreams(Stream.of(0), Stream.of(1, 2)));
        checkStreamOf3(joinStreams(Stream.of(0, 1), Stream.of(2)));
    }

    private void checkStreamOf3(Stream<Integer> checked) {
        List<Integer> lst = checked.collect(Collectors.toList());
        Assert.assertEquals(lst.size(), 3);
        Assert.assertEquals(lst.get(0).intValue(), 0);
        Assert.assertEquals(lst.get(2).intValue(), 2);
    }

    @Test
    public void testAutoclosable() {
        int cnt[] = {0};
        Stream<Integer> aStream = Stream.of(0, 1, 2).onClose(() -> cnt[0]++);
        Stream<Integer> withAction = autoclosable(aStream);
        Assert.assertEquals(cnt[0], 0);
        checkStreamOf3(withAction);
        Assert.assertEquals(cnt[0], 1);
    }
}