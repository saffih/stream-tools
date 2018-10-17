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

package saffih.streamtools

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import java.io.Reader
import java.lang.reflect.ParameterizedType
import java.util.stream.Stream


class KJsonStreamParser {
    private val objectMapper = ObjectMapper()
    private val jsonFactory = JsonFactory(objectMapper)

    @Throws(IOException::class)
    fun parse(input: Reader): JsonStreamParserState {
        return JsonStreamParserState(input)
    }

    internal inner class UnexpectedError(s: String) : Exception(s)

    inner class JsonStreamParserState @Throws(IOException::class)
    constructor(input: Reader) {
        private val jp: JsonParser = jsonFactory.createParser(input)
        val fields = HashMap<String, TreeNode>()
        val streamFields = HashMap<String, Stream<TreeNode>>()

        init {
            this.jp.nextToken()
        }

        fun <T> parseObject(ref: TypeReference<T>, fieldname: String, callback: (java.lang.Exception) -> Unit): Stream<T> {
            val clazz = (if (ref.type is ParameterizedType) (ref.type as ParameterizedType).rawType else ref.type) as Class<T>
            return parseObject(fieldname, callback).map { it -> objectMapper.treeToValue(it, clazz) }
        }

        fun parseObject(fieldname: String, callback: (java.lang.Exception) -> Unit): Stream<TreeNode> {
            if (readStart(JsonToken.START_OBJECT, callback)) {
                readTillField(fieldname)
                val result = readStreamField(fieldname, callback)
                this.streamFields[fieldname] = result
                return result
            } else {
                return Stream.empty()
            }
        }

        private fun readStreamField(fieldname: String, callback: (java.lang.Exception) -> Unit): Stream<TreeNode> {
            if (jp.currentName == null) {
                callback(UnexpectedError("Expected missing field $fieldname"))
                return Stream.empty()
            }
            if (jp.currentName != fieldname) {
                callback(UnexpectedError("Bug wrong field $fieldname"))
            }
            jp.nextToken() // done fieldname
            return if (readStart(JsonToken.START_ARRAY, callback)) {
                streamUtil.generatorTillNull { this.parseTreeElement(JsonToken.END_ARRAY, callback) }
            } else Stream.empty()
        }

        private fun readTillField(fieldname: String) {
            while (JsonToken.FIELD_NAME == jp.currentToken() && jp.currentName != fieldname) {
                jp.nextToken()
                this.fields[jp.currentName] = jp.readValueAsTree()
                jp.nextToken()
            }
        }

        fun parseRest(callback: (java.lang.Exception) -> Unit) {
            while (JsonToken.FIELD_NAME == jp.currentToken()) {
                jp.nextToken()
                this.fields[jp.currentName] = jp.readValueAsTree()
                jp.nextToken()
            }
        }

        fun <T> parseArray(ref: TypeReference<T>, callback: (java.lang.Exception) -> Unit): Stream<T> {
            return if (readStart(JsonToken.START_ARRAY, callback)) streamUtil.generatorTillNull { this.parseElement(JsonToken.END_ARRAY, ref, callback) }
            else Stream.empty()
        }

        fun <T> parseObject(ref: TypeReference<T>, callback: (java.lang.Exception) -> Unit): Stream<T> {
            return if (readStart(JsonToken.START_ARRAY, callback)) streamUtil.generatorTillNull { this.parseElement(JsonToken.END_OBJECT, ref, callback) }
            else Stream.empty()
        }

        private fun readStart(start: JsonToken, callback: (java.lang.Exception) -> Unit): Boolean {
            val currentToken = jp.currentToken()
            if (currentToken != start) {
                callback(UnexpectedError("Expected start $start found $currentToken"))
            } else {
                try {
                    jp.nextToken()
                } catch (e: IOException) {
                    callback(e)
                    return false
                }
            }
            return true
        }


        private fun parseTreeElement(end: JsonToken, callback: (Exception) -> Unit): TreeNode? {
            try {
                if (jp.currentToken() == end) {
                    return null
                }
                return jp.readValueAsTree<TreeNode>()
            } catch (e: IOException) {
                callback(e)
                return null
            } finally {
                jp.nextToken()
            }
        }

        private fun <T> parseElement(end: JsonToken, ref: TypeReference<T>, callback: (Exception) -> Unit): T? {
            try {
                if (jp.currentToken() == end) {
                    return null
                }
                return jp.readValueAs<T>(ref)
            } catch (e: IOException) {
                callback(e)
                return null
            } finally {
                jp.nextToken()
            }
        }
    }
}