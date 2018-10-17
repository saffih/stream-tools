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

package saffih.jsonstream


import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import saffih.streamtools.KJsonStreamParser
import java.io.*
import kotlin.streams.toList


@Test(groups = ["smoke"])
class JsonStreamKTTest {
    private val classLoader = ClassLoader.getSystemClassLoader()
    private val arrayFileName = "example-list.json"
    private val objectFileName = "example-object.json"

    @Test
    fun readResourceArrayKT() {
        val file = File(classLoader.getResource(arrayFileName)!!.file)
        assertTrue(file.exists())

        //create ObjectMapper instance
        val objectMapper = ObjectMapper()

        //read JSON like DOM Parser
        val rootNode = objectMapper.readTree(FileReader(file))
        Assert.assertTrue(rootNode.isArray)
        Assert.assertEquals(rootNode.size().toLong(), 100)
        val parseState = KJsonStreamParser().JsonStreamParserState(BufferedReader(
                InputStreamReader(classLoader.getResourceAsStream(arrayFileName)!!)))
        val typeReference = object : TypeReference<FooBarBaz>() {}
        val callback = { e: Exception? -> if (e != null) throw  RuntimeException("Failed", e) }
        val result = parseState.parseArray(typeReference, callback)
        val lst = result.toList()
        Assert.assertEquals(lst.size, 100)
    }

    @Test
    fun readResourceObjectArraySmallKT() {
        val file = File(classLoader.getResource(objectFileName)!!.file)
        assertTrue(file.exists())
        val st = """
            {"a" : 1,
                "tostream": [
                    {
                      "foo": "cba3cb55-62f2-48bd-bdf9-ef381dc5652b",
                      "bar": 528,
                      "baz": true
                    }
                ],
                "b":2
                } """
        val input: StringReader = st.reader()
        val parseState = KJsonStreamParser().parse(input)
        val typeReference = object : TypeReference<FooBarBaz>() {}
        val callback = { e: Exception? -> if (e != null) throw  RuntimeException("Failed", e) }
        val result = parseState.parseObject(typeReference, "tostream", callback)
        val lst = result.toList()
        Assert.assertEquals(lst.size, 1)
        parseState.parseRest(callback)
        Assert.assertEquals(parseState.fields["b"].toString(), "2")
    }

    @Test
    fun readResourceObjectArrayLargeKT() {
        val file = File(classLoader.getResource(objectFileName)!!.file)
        assertTrue(file.exists())

        val input = BufferedReader(InputStreamReader(classLoader.getResourceAsStream(objectFileName)!!))
        val parseState = KJsonStreamParser().parse(input)
        val typeReference = object : TypeReference<FooBarBaz>() {}
        val callback = { e: Exception? -> if (e != null) throw  RuntimeException("Failed", e) }
        val result = parseState.parseObject(typeReference, "tostream", callback)
        val lst = result.toList()
        Assert.assertEquals(lst.size, 100)
        parseState.parseRest(callback)
        Assert.assertEquals(parseState.fields["beforeStr"].toString(), "\"str\"")
        Assert.assertEquals(parseState.fields["beforeInt"].toString(), "-7")
        Assert.assertEquals(parseState.fields["afterStr"].toString(), "\"str\"")
        Assert.assertEquals(parseState.fields["afterInt"].toString(), "7")
    }
}
