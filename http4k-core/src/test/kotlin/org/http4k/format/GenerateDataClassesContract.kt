package org.http4k.format

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.GenerateDataClasses
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal

abstract class GenerateDataClassesContract<NODE : Any>(val j: Json<NODE>) {

    @Test
    open fun `generates data classes correctly`() {
        val input = j {
            obj(
                "string" to string("value"),
                "double" to number(1.0),
                "long" to number(10L),
                "boolean" to boolean(true),
                "bigDec" to number(BigDecimal(1.2)),
                "nullNode" to nullNode(),
                "int" to number(2),
                "empty" to obj(),
                "nonEmpty" to obj(
                    "double" to number(1.0),
                    "long" to number(10L)
                ),
                "array" to array(listOf(
                    string(""),
                    number(123),
                    obj(
                        "nullNode" to nullNode(),
                        "long" to number(10L)
                    )
                )),
                "singleTypeArray" to array(
                    listOf(obj(
                        "string" to string("someString"),
                        "list" to array(listOf(obj("id" to string("someValue"))))
                    ))
                )
            )
        }
        val os = ByteArrayOutputStream()

        val handler = GenerateDataClasses(j, PrintStream(os)) { 1 }.then { Response(OK).with(j.body().toLens() of input) }

        handler(Request(GET, "/bob"))
        val actual = String(os.toByteArray())
        assertThat(actual, equalTo("""// result generated from /bob

data class Array1(val nullNode: Any?, val long: Number?)

data class Base(val string: String?, val double: Number?, val long: Number?, val boolean: Boolean?, val bigDec: Number?, val nullNode: Any?, val int: Number?, val empty: Empty?, val nonEmpty: NonEmpty?, val array: List<Any>?, val singleTypeArray: List<SingleTypeArray1>?)

data class Empty()

data class List1(val id: String?)

data class NonEmpty(val double: Number?, val long: Number?)

data class SingleTypeArray1(val string: String?, val list: List<List1>?)
"""))
    }
}
