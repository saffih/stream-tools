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

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "foo",
        "bar",
        "baz"
})

public class FooBarBaz {

    @JsonProperty("foo")
    private String foo;
    @JsonProperty("bar")
    private Integer bar;
    @JsonProperty("baz")
    private Boolean baz;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    public FooBarBaz() {
    }

    @JsonProperty("foo")
    public String getFoo() {
        return foo;
    }

    @JsonProperty("foo")
    public void setFoo(String foo) {
        this.foo = foo;
    }

    @JsonProperty("bar")
    public Integer getBar() {
        return bar;
    }

    @JsonProperty("bar")
    public void setBar(Integer bar) {
        this.bar = bar;
    }

    @JsonProperty("baz")
    public Boolean getBaz() {
        return baz;
    }

    @JsonProperty("baz")
    public void setBaz(Boolean baz) {
        this.baz = baz;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }


}
