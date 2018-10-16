package saffih.jsonstream

import org.testng.Assert.assertNotNull
import org.testng.annotations.Test

@Test(groups = ["failed"])
class KTFailedTestFail {
    fun wouldFail() {

        assertNotNull(null)
    }

}

@Test(groups = ["smoke"])
class KTPassTheSmokeTest {

    @Test
    fun passTest() {
        println("we pass - hello world.")
    }
}