Stream Tools
======

## Working with streams has it's benefits:
* Just in time use - lower memory footprint.
* Common interface and ease of manipulation - filter / collect etc.

## And challenges:
* **Deferred operations and close**: The deferred nature has it's merits until you accidentally close the underline resource.
** On the other hand one leave the stream as is and the last one consuming it must close it.
* **No break**: Once a stream is used it is read and consumed all the way (unlike generators/iterators)


## Some extra magic for Java
* Adding autoclosable(theStream) which would would call thestream.close() when the stream is consumed
* Parsing huge Json (long list of objects) one by one.

## Better magic with Kotlin
* Doing it in Kotlin - enjoy the syntax, theStream.autoclose() [Extension function](https://kotlinlang.org/docs/reference/extensions.html)
```kotlin
@Test
fun testStreamTillNullWithInfiniteStream() {
    val endless = streamUtil.generatorTillNull { i -> i }.map { i-> if (i!=3) i else null }
    checkStreamOf3(endless.tillNull())
}
```
* Kotlin needs attention with regard to [lazyness (sequence)](https://dzone.com/articles/kotlin-beware-of-java-stream-api-habits)


## Leveraging Jackson to read large json having large array field as stream of objects
* The example shows a large array of a json object read as stream,
* while the other "lower footprint" are processed as well when the stream is done.
* The processing of the rest was done using onConsume method (similar to the autoclose that was described above)
```kotlin
@Test
fun readResourceObjectArrayLargeKT() {
    val file = File(classLoader.getResource(objectFileName)!!.file)
    assertTrue(file.exists())

    val input = BufferedReader(InputStreamReader(classLoader.getResourceAsStream(objectFileName)!!))
    val parseState = KJsonStreamParser().parse(input)
    val typeReference = object : TypeReference<FooBarBaz>() {}
    val callback = { e: Exception? -> if (e != null) throw  RuntimeException("Failed", e) }
    val result = parseState.parseObject(typeReference, "tostream", callback)
            .onConsume({  parseState.parseRest(callback)})
    val streamIt = result.asSequence().iterator()
    Assert.assertEquals(parseState.fields["beforeStr"].toString(), "\"str\"")
    Assert.assertEquals(parseState.fields["beforeInt"].toString(), "-7")
    Assert.assertFalse(parseState.fields.containsKey("afterInt"))
    var cnt = 0
    streamIt.forEach {
        cnt++;
        print("read $it")
        Assert.assertFalse(parseState.fields.containsKey("afterInt"))
    }
    Assert.assertTrue(parseState.fields.containsKey("afterInt"))
    Assert.assertEquals(cnt, 100)
    Assert.assertEquals(parseState.fields["afterStr"].toString(), "\"str\"")
    Assert.assertEquals(parseState.fields["afterInt"].toString(), "7")
}
```
