class Test {

    private var value : Double = 0.943 - 0.3239
    private var a : Int = 10 // intConst
    private var d : Double = 0.1

    /**
     * Ira is a monkey
     * asasx */
    fun myFunc1(args: String): Int {
        value = 0.0001
        Test.myFunc2(5.0, 10)
        while ((a + d) * d < 200) {
            if (d == 0.1) {
                a = a + 5
            } else {
                a = a - 5
            }
        }

        // create object
        val t1 : Int = 1
        val t3 : Int = 3
        t1 = 3

        // call methods
        val b : Double = myFunc2(3.0, 4, t1)

        return a
    }

    private fun myFunc2(b: Double, c: Int): Double {
        //var b : Double = b
        var c : Int = c
        if (true) {
            b = b + (d - a)
        } else {
            var s : String = g
        }
        if (c < 2) {
            d = d * 2
        } else {
            c = 0
        }
        return d * b
    }
}