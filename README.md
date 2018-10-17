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
*

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
