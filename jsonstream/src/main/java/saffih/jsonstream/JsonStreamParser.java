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

package saffih.jsonstream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import saffih.streamtools.StreamUtilForJava8;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static saffih.streamtools.StreamUtilForJava8.appendStreamAction;


public class JsonStreamParser {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory(objectMapper);

    public JsonStreamParserState parse(InputStreamReader input) throws IOException {
        return new JsonStreamParserState(input);
    }


    class UnexpectedError extends RuntimeException {
        UnexpectedError(String s) {
            super(s);
        }
    }

    class JsonStreamParserState {
        private final JsonParser jp;
        Reader input;

        JsonStreamParserState(Reader input) throws IOException {
            this.input = input;
            this.jp = jsonFactory.createParser(input);
            this.jp.nextToken();
        }

        public <T> Stream<T> parseArray(TypeReference<T> ref, Consumer<Exception> callback) {
            if (jp.currentToken() != JsonToken.START_ARRAY) {
                return appendStreamAction(Stream.empty(),
                        (Void) -> callback.accept(new UnexpectedError("Expected start of array " + this.jp.toString())));
            } else {
                try {
                    jp.nextToken();
                } catch (IOException e) {
                    callback.accept(e);
                    return Stream.empty();
                }
            }
            return StreamUtilForJava8.generateTillNull((i) -> this.parseArrayElement(ref, callback));
        }

        public <T> T parseArrayElement(TypeReference<T> ref, Consumer<Exception> callback) {
            try {
                if (jp.currentToken() == JsonToken.END_ARRAY) {
                    JsonToken advance = jp.nextToken();
                    return null;
                }
                return jp.readValueAs(ref);
            } catch (
                    IOException e) {
                callback.accept(e);
            }
            return null;
        }
    }


}
