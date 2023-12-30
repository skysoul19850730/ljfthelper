package tasks.normal

import data.HeroBean

data class NormalConfigBean(
    val heros: List<HeroBean>,
)

data class TaskStep(
    var condition: Condition,
    var taskType: Int,//上英雄或下英雄，（幻，光等都算是上英雄)
    var heros: List<HeroBean>//需要用到的英雄
)

//触发条件
data class Condition(
    var guankaImg: Int = 0,
    var bossImg: String? = null,
)
