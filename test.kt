class Test {

    private var value: Int = 1
    private var a: Int = 10 // intConst
    private var d: Int = 0
    val c : Char = 'c'

    /**
     * Comments Block yyy-lya-lya
     */
    fun main(args: Array<Char>): Int {
        val array : Array<Int> = Array(5)
        array[4] = 9
        value = 4
        while ((a + d) * d < 200) {
            if (d == 5) {
                a = a + 5
            } else {
                a = a - 5
            }
        }
        // create object
        val t1: Int = 1
        val t3: Int = 3
        t1 = 3
        // call methods
        val b: Int = myFunc2(3, 4)
        main1()
        return a
    }

    val res: Int = 111

    private fun myFunc2(b: Int, c: Int): Int {
        if (true) {
            b = b + (d - a)
        } else {
            var s: Int = 5
        }
        if (c < 2) {
            d = d * 2
        } else {
            c = 0
        }
        return d * b
    }


    fun main1() {
        val eps: Int = 2
        val pi: Int = 3
        {
            val x: Int = 5
            val y: Int = 5
            {
                val x1: Char = 'x'
                val y2: Char = 'y'
            }
        }
    }
}


