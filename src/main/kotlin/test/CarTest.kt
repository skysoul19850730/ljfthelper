package test

import logOnly
import model.CarDoing

object CarTest {

    fun testStar() {
        var starttime = System.currentTimeMillis()
        doTestStart()
        var end = System.currentTimeMillis()
        logOnly("time :${(end - starttime)}")
    }

    private fun doTestStart() {
//         var car1 = CarDoing().apply {
//             initPositions(true,0)
//             img = getImageFromRes("cartest1.png")
//         }
        var car2 = CarDoing(1).apply {
            initPositions()
//            img = getImageFromRes("tftest.png")


            carps.forEachIndexed { index, carPosition ->
                var level = carPosition.getStarLevelDirect()
                logOnly("position:$index level:$level")
            }

        }
    }
}