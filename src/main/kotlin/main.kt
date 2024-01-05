@file:OptIn(ExperimentalComposeUiApi::class)

import App.state
import MainData.guan
import MainData.heros
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.sun.jna.platform.win32.*
import data.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tasks.*
import tasks.hanbing.mengyan.ChuanZhangTest
import tasks.hezuo.zhannvsha.ZhanNvGameLaunch
import utils.LogUtil
import utils.MRobot
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import tasks.huodong.HuodongUtil
import utils.ImgUtil
import java.awt.event.KeyEvent


@OptIn(ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {

    var modelText by remember {
        mutableStateOf("选择模式")
    }
    val showChooseModel = remember { mutableStateOf(false) }
    val selectModel = remember { mutableStateOf(-1) }
    val timeInputDialog = remember { mutableStateOf(false) }
    val customScreen = remember { mutableStateOf(false) }

    val showHezuoZhannvMenu = remember { mutableStateOf(false) }
//    val testMenu = remember { mutableStateOf(false) }

    val showJieXiHeroUI = remember { mutableStateOf(false) }

    var testDialogTitle by remember { mutableStateOf("测试动态弹窗") }

    MaterialTheme {
        Row {
            carInfo()
            Column(Modifier.weight(1f).fillMaxHeight()) {
                Row(Modifier.fillMaxWidth().background(Color.LightGray).padding(12.dp)) {
                    MCheckBox("Home", Config.isHome4Setting)
                    MCheckBox("采集", App.caijing)
//                    HSpace(6)
//                    MRadioBUtton("模拟器", Config.platform_moniqi, Config.platform)
//                    HSpace(6)
//                    MRadioBUtton("WX", Config.platform_wx, Config.platform)
//                    HSpace(6)
//                    MRadioBUtton("QQ", Config.platform_qq, Config.platform)

//                    button((if (GameUtil.ShuaMoValue.value) "停魔" else "开魔")) {
//                        GameUtil.ShuaMoValue.value = !GameUtil.ShuaMoValue.value
//                        if (GameUtil.ShuaMoValue.value) {
//                            GameUtil.startShuaMo()
//                        } else {
//                            GameUtil.stopShuaMo()
//                        }
//                    }
//                    button((if (ChuanZhangTest.chuanZhangObeserver.value) "监听中" else "开启")) {
//                        if (ChuanZhangTest.chuanZhangObeserver.value) {
//                            ChuanZhangTest.chuanZhangObeserver.value = false
//                        } else {
//                            ChuanZhangTest.startChuanZhangOberserver(0)
//                        }
//                    }
//                    button((if (ChuanZhangTest.chuanZhangObeserver.value) "监听中" else "开启")) {
//                        if (ChuanZhangTest.chuanZhangObeserver.value) {
//                            ChuanZhangTest.chuanZhangObeserver.value = false
//                        } else {
//                            ChuanZhangTest.startChuanZhangOberserver(1)
//                        }
//                    }
                }
                Box(Modifier.fillMaxSize()) {
                    var state = rememberLazyListState()
                    LazyColumn(Modifier.fillMaxSize(), state) {
                        itemsIndexed(LogUtil.messages) { index, item ->
                            if (item is String) {
                                Text(item, Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp))
                            } else if (item is BufferedImage) {
                                var ww = item.width
                                var hh = item.height
                                if (item.width > item.height && item.width > 400) {
                                    ww = 400
                                    hh = ((item.height * 1f * ww) / item.width).toInt()
                                } else if (item.height > item.width && item.height > 400) {
                                    hh = 400
                                    ww = ((item.width * 1f / item.height) * hh).toInt()
                                }
                                Image(item.toPainter(), null, Modifier.width(ww.dp).height(hh.dp))
                            }
                        }
                    }
                    VerticalScrollbar(
                        rememberScrollbarAdapter(state),
                        Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                    )
                }
            }
            Column(Modifier.fillMaxHeight().background(Color.LightGray).padding(12.dp, 0.dp)) {
                button("初始化") {
                    App.init()
                }
                button(modelText) {
                    showChooseModel.value = true
                }
                Button(onClick = {
                    if (selectModel.value >= 0) {
                        if (state.value == 0) {
                            App.start()
                        } else {
                            App.stop()
                        }
                    }
                }) {
                    Text(if (state.value == 0) "开始" else "暂停")
                }

                button("output") {
                    Hero.aotuCaiji()
                }
                button("解析英雄") {
                    Hero.caijianIng = false
                    Hero.jiexiHeros()
                    showJieXiHeroUI.value = true
                }

                button(App.timerText) {
                    if (App.timerText.value == "定时关闭") {
                        timeInputDialog.value = true
                    }
                }
                button("活动") {
                    if (HuodongUtil.state.value) {
                        HuodongUtil.stop()
                    } else {
                        HuodongUtil.start(1)
                    }
                }
                button("天空") {
                    if (HuodongUtil.state.value) {
                        HuodongUtil.stop()
                    } else {
                        HuodongUtil.start(2)
                    }
                }
                button("测试") {
                    test()
                }

                if (selectModel.value == -2) {
                    button("自定义裁剪") {
                        customScreen.value = true
                    }
                    App.subButtons()

                }
                hezuocaiji()

            }
        }
        if (showChooseModel.value) {
            with(PopupAlertDialogProvider) {
                AlertDialog({
                    showChooseModel.value = false
                }) {
                    Box(Modifier.clickable { }) {
                        Column(
                            Modifier.heightIn(50.dp, 300.dp).background(Color.White)
                                .border(
                                    1.dp, Color.Blue,
                                    RoundedCornerShape(12.dp)
                                ).clip(RoundedCornerShape(12.dp))
                                .padding(20.dp)
                                .verticalScroll(
                                    state = rememberScrollState()
                                )
                        ) {
                            button("天空模式") {
                                showChooseModel.value = false
//                                App.setLaunchModel(App.modelCaiji)
//                                selectModel.value = App.modelCaiji
//                                modelText = "采集模式"
                                App.setLaunchModel(App.model_tiankong_5f)
                                selectModel.value = App.model_tiankong_5f
                                modelText = "天空5法"
                            }
                            button("对战-龙拳") {
                                showChooseModel.value = false
                                App.setLaunchModel(App.model_longquan)
                                selectModel.value = App.model_longquan
                                modelText = "对战-龙拳"
                            }
//                            button("对战-法枪") {
//                                showChooseModel.value = false
//                                App.setLaunchModel(App.model_faqiang)
//                                selectModel.value = App.model_faqiang
//                                modelText = "对战-法枪"
//                            }

                            button("合作-战女") {
                                showChooseModel.value = false
                                App.setLaunchModel(App.model_hezuo_zhannv)
                                selectModel.value = App.model_hezuo_zhannv
                                modelText = "合作-战女"
                                showHezuoZhannvMenu.value = true
                            }
                            button("寒冰-战女") {
                                showChooseModel.value = false
                                App.setLaunchModel(App.model_hanbing_zhannv)
                                selectModel.value = App.model_hanbing_zhannv
                                modelText = "寒冰-战女"
                            }
                            button("寒冰-梦魇") {
                                showChooseModel.value = false
                                App.setLaunchModel(App.model_hanbing_mengyan)
                                selectModel.value = App.model_hanbing_mengyan
                                modelText = "寒冰-梦魇"
                            }
                        }
                    }
                }
            }
        }

        if (customScreen.value) {
            showOtherWindow(customScreen)
        }

        if (CarDoing.showTouziRect.value) {
            showTouziRect()
        }

        showInputDialog("输入时长分钟数，可以浮点型", "例如：120", timeInputDialog) {
            try {
                App.startTimerDown(it.toFloat())
                true
            } catch (e: Exception) {
                log(e.message ?: "转换失败")
                false
            }
        }

//        if(testMenu.value){
////            MenuDialog(testMenu){
////                button("hahaha"){
////                    testMenu.value = false
////                }
////                button("lululu"){
////                    testMenu.value = false
////                }
////                button("siqunima"){
////                    testMenu.value = false
////                }
////            }
//            var ttttext = mutableStateOf("")
//            MCustomDialog(testMenu,{
//                OutlinedTextField(ttttext.value,{it->
//                    ttttext.value = it
//                })
//            }){
//                println("input text is $ttttext")
//            }
//        }

        addJiexiHeroResult(showJieXiHeroUI)

        addHezuoMenuDialog(showHezuoZhannvMenu)
    }
}


object MainData {
    val heros = mutableStateOf(arrayListOf<HeroBean?>())
    val guan = mutableStateOf(0)
}

@Composable
fun carInfo() {
    Row {
        Column {
            Text("关卡",Modifier.height(20.dp))
            Text("${guan.value}", Modifier.height(20.dp))
            VSpace(12)
            carPosInfo(heros.value.getOrNull(5))
            carPosInfo(heros.value.getOrNull(3))
            carPosInfo(heros.value.getOrNull(1))
        }
        HSpace(12)
        Column {
            carPosInfo(heros.value.getOrNull(6))
            carPosInfo(heros.value.getOrNull(4))
            carPosInfo(heros.value.getOrNull(2))
            carPosInfo(heros.value.getOrNull(0))
        }

    }
}

@Composable
fun carPosInfo(hero: HeroBean?) {
    val name = hero?.heroName ?: "空位"
    val star = hero?.currentLevel?.toString() ?: ""
    Column {
        Text("$name", Modifier.background(Color.Gray).width(60.dp).height(20.dp))
        Text("$star",Modifier.width(60.dp).height(20.dp))
        VSpace(12)
    }
}

@Composable
fun dialogBtn(text: String, dialogContent: @Composable (MutableState<Boolean>) -> Unit) {
    var state = remember { mutableStateOf(false) }
    dialogContent(state)
    button(text) {
        state.value = true
    }
}


@Composable
fun addJiexiHeroResult(state: MutableState<Boolean>) {

    MCustomDialog(state, content = {
        LazyColumn {
            itemsIndexed(Hero.mHeroParse) { index, bean ->

                Row(Modifier.fillMaxWidth()) {

//                    Image(getImageFromRes("test/bingnv.png").toPainter(), null, Modifier.width(60.dp).height(60.dp))

                    val imgs = bean.imgs
                    LazyRow(Modifier.weight(1f)) {
//                        val imgs = remember { mutableStateListOf<File>().apply { addAll(bean.imgs) } }
                        itemsIndexed(imgs) { s2, f ->
                            Box(Modifier.size(60.dp)) {
                                Image(
                                    getImageFromFile(f).toPainter(),
                                    null,
                                    Modifier.width(40.dp).height(40.dp).align(Alignment.Center).border(1.dp, Color.Blue)
                                )
                                Icon(Icons.Default.Close, null, Modifier.size(20.dp).align(Alignment.TopEnd).clickable {
                                    imgs.removeAt(s2)
                                    f.delete()
                                }, tint = Color.Red)
                            }

                        }
                    }

                    button("删除") {
                        var bean = Hero.mHeroParse.removeAt(index)
                        bean.imgs.forEach {
                            it.delete()
                        }
                        bean.file?.delete()
                    }

                    dialogBtn("修改") {
                        showInputDialog("英雄名", "ex:zhanjiang", it) { name ->

                            bean.imgs.forEachIndexed { findex, fileImg ->
                                fileImg.rename("$name${findex}.png")
                            }
                            bean.file = bean.file!!.rename("$name")
                            var newList = bean.file!!.listFiles()
                            bean.imgs.clear()
                            bean.imgs.addAll(newList)
                            true
                        }
                    }
                }


            }
        }

    }, onSuc = {

    })

}

@Composable
fun addHezuoMenuDialog(state: MutableState<Boolean>) {
    var wxMenu = remember { mutableStateOf(false) }
    MenuDialog(state) {
        button("手动模式") {
            ZhanNvGameLaunch.model = 0
            state.value = false
        }
        button("微信自动车") {
            ZhanNvGameLaunch.model = 1
            wxMenu.value = true
        }
        button("好友自动车") {
            ZhanNvGameLaunch.model = 2
            state.value = false
        }
        button("世界自动车") {
            ZhanNvGameLaunch.model = 3
            state.value = false
        }
    }

    if (wxMenu.value) {
        MenuDialog(wxMenu) {
            button("群合作") {
                ZhanNvGameLaunch.parterner = 0
                wxMenu.value = false
                state.value = false
            }
            button("小号") {
                ZhanNvGameLaunch.parterner = 1
                wxMenu.value = false
                state.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MenuDialog(showState: MutableState<Boolean>, content: @Composable ColumnScope.() -> Unit) {
    if (!showState.value) return
    with(PopupAlertDialogProvider) {
        AlertDialog({
            showState.value = false
        }) {
            Box(Modifier.clickable { }) {
                Column(
                    Modifier.heightIn(50.dp, 300.dp).background(Color.White)
                        .border(
                            1.dp, Color.Blue,
                            RoundedCornerShape(12.dp)
                        ).clip(RoundedCornerShape(12.dp))
                        .padding(20.dp)
                        .verticalScroll(
                            state = rememberScrollState()
                        )
                ) {
                    content.invoke(this)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MCustomDialog(showState: MutableState<Boolean>, content: @Composable () -> Unit, onSuc: () -> Unit) {
    if (!showState.value) return
    with(PopupAlertDialogProvider) {
        AlertDialog({
            showState.value = false
        }) {
            Box(Modifier.clickable { }) {
                Column(
                    Modifier.heightIn(50.dp, 300.dp).background(Color.White)
                        .border(
                            1.dp, Color.Blue,
                            RoundedCornerShape(12.dp)
                        ).clip(RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    content.invoke()

                    Row {

                        Spacer(Modifier.weight(1f, true))
                        button("取消") {
                            showState.value = false
                        }
                        HSpace(12)
                        button("确定") {
                            onSuc.invoke()
                            showState.value = false
                        }

                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun showInputDialog(
    title: String,
    hint: String,
    showState: MutableState<Boolean>,
    onSuc: RowScope.(String) -> Boolean
) {
    if (showState.value) {
        var text = remember { mutableStateOf("") }
        with(PopupAlertDialogProvider) {
            AlertDialog({
                showState.value = false
            }) {
                Box(Modifier.clickable { }.width(IntrinsicSize.Min)) {
                    Column(
                        Modifier.heightIn(50.dp, 200.dp).background(Color.White)
                            .border(
                                1.dp, Color.Blue,
                                RoundedCornerShape(12.dp)
                            ).clip(RoundedCornerShape(12.dp))
                            .padding(20.dp)
                            .verticalScroll(
                                state = rememberScrollState()
                            )
                    ) {
                        Text(title)
                        VSpace(12)
                        OutlinedTextField(text.value, {
                            text.value = it
                        }, placeholder = { Text(hint) })
                        VSpace(12)
                        Row {

                            Spacer(Modifier.weight(1f, true))
                            button("取消") {
                                showState.value = false
                            }
                            HSpace(12)
                            button("确定") {
                                if (onSuc.invoke(this, text.value)) {
                                    showState.value = false
                                }

                            }

                        }

                    }
                }
            }
        }
    }
}

@Composable
private fun showTouziRect() {
    Window({
        CarDoing.showTouziRect.value = false
    }, transparent = true, undecorated = true, resizable = false, alwaysOnTop = true) {
        MaterialTheme {
            Box(
                Modifier.width(CarDoing.touziRect.width.dp).height(CarDoing.touziRect.height.dp)
                    .border(1.dp, Color.Green)
            ) {
            }

            this.window.setLocation(CarDoing.touziLeft.value, CarDoing.touziRect.top)
            this.window.setBounds(
                CarDoing.touziLeft.value,
                CarDoing.touziRect.top,
                CarDoing.touziRect.width,
                CarDoing.touziRect.height
            )
        }

    }
}

@Composable
private fun showOtherWindow(customScreen: MutableState<Boolean>) {
    Window({
        customScreen.value = false
    }, undecorated = true, resizable = false) {
        customScreenDialog(this.window, customScreen)
        this.window.setLocation(0, 0)
    }
}

@ExperimentalComposeUiApi
@Composable
private fun customScreenDialog(window: ComposeWindow, customScreen: MutableState<Boolean>) {
    val img = getImage(App.rectWindow)
    window.setBounds(0, 0, img.width, img.height)
    var startPoint: Point? = null
    val movePoint = remember { mutableStateOf<Point?>(null) }

    MaterialTheme {
        Box(Modifier.width(img.width.dp).height(img.height.dp).border(1.dp, Color.Green)) {

            Image(img.toPainter(), null, Modifier.width(img.width.dp)
                .height(img.height.dp)
                .onPointerEvent(PointerEventType.Press) {
                    if (it.buttons.isSecondaryPressed) {//按右键
                        movePoint.value = null
                        if (startPoint != null) {
                            startPoint = null
                        } else {
                            window.dispose()
                            customScreen.value = false
                        }
                    } else if (it.buttons.isPrimaryPressed) {
                        startPoint = it.awtEvent.point!!
                        log("start x ${startPoint!!.x}")
                    }
                }.onPointerEvent(PointerEventType.Release) {
                    if (startPoint != null) {
                        val end = it.awtEvent.point!!
                        val left = min(end.x, startPoint!!.x)
                        val top = min(end.y, startPoint!!.y)
                        val right = max(end.x, startPoint!!.x)
                        val bottom = max(end.y, startPoint!!.y)
                        if (left >= 0 && top >= 0) {
                            if (right > left && bottom > top) {//矩形
                                var imgname = "custom_${left}_${top}_${right}_${bottom}.png"
                                img.saveSubTo(
                                    MRect.create4P(left, top, right, bottom), File(
                                        Config.caiji_main_path + "\\custom",
                                        imgname
                                    )
                                )
                                MRobot.copyText("newrect(\"$imgname\", MRect.create4P($left,$top,$right,$bottom)),")
                            } else if (right == left && bottom == top) {//生成point
                                MRobot.copyText(
                                    "val newPoint = MPoint($left,$top,Color(${
                                        MRobot.robot.getPixelColor(
                                            left,
                                            top
                                        )
                                    }))"
                                )
                            }
                        }
                        startPoint = null
                        movePoint.value = null
                    }
                }.onPointerEvent(PointerEventType.Move) {
                    movePoint.value = it.awtEvent.point!!
//                    text = "x:${curPoint.value.x} y:${curPoint.value.y}"
                }, alignment = Alignment.TopStart
            )
//            if(movePoint.value!=null) {
//                Text(
//                    "x:${movePoint.value!!.x} y:${movePoint.value!!.y}", Modifier
//                        .layout { measurable, constraints ->
//                            val place = measurable.measure(constraints)
//                            layout(place.width, place.height) {
//                                place.placeRelative(
//                                    movePoint.value!!.x - place.width / 2,
//                                    movePoint.value!!.y - place.height
//                                )
//                            }
//                        }, color = Color.White
//                )
//            }
            movePoint.value?.run {
                Text(
                    "x:${x} y:${y}", Modifier
                        .layout { measurable, constraints ->
                            val place = measurable.measure(constraints)
                            layout(place.width, place.height) {
                                place.placeRelative(
                                    x - place.width / 2,
                                    y - place.height
                                )
                            }
                        }, color = Color.White
                )
            }

            if (movePoint.value != null && startPoint != null) {
                Box(
                    Modifier.size(
                        abs(movePoint.value!!.x - startPoint!!.x).dp,
                        abs(movePoint.value!!.y - startPoint!!.y).dp
                    )
                        .absoluteOffset(
                            min(movePoint.value!!.x, startPoint!!.x).dp,
                            min(movePoint.value!!.y, startPoint!!.y).dp
                        )
                        .border(1.dp, Color.Red)
                )
            }

        }
    }
}

@Composable
private fun getSubButtons(model: Int) {
    if (model == App.model_faqiang) {

    }
}


@Composable
private fun hezuocaiji() {
    button("管卡采集") {
        if (!Guanka.caijiing) {
            Guanka.startCaiji()
        } else {
            Guanka.stopCaiji()
        }
    }
    button("保存boss") {
        Boss.save()
    }

//    button("识别车"){
//        GlobalScope.launch {
//            var chePosition = -1
//            while (chePosition == -1) {
//                if (Recognize.QianChe.isFit()) {
//                    chePosition = 1
//                } else if (Recognize.Houche.isFit()) {
//                    chePosition = 0
//                }
//            }
//            log("识别车辆：${chePosition}")
//        }
//    }

    button("保存装备") {
        Zhuangbei.save()
    }
//    button("保存管卡") {
//        Guanka.save()
//    }
//    button("保存英雄") {
//        Hero.save()
//    }
    button("保存窗口") {
        App.save()
    }
//    button("下1") {
//        caijiRes()
//    }
}


@Composable
fun button(text: String, onClick: () -> Unit) {
    Button(onClick = {
        onClick.invoke()
    }) {
        Text(text)
    }
}

@Composable
fun button(text: State<String>, onClick: () -> Unit) {
    Button(onClick = {
        onClick.invoke()
    }) {
        Text(text.value)
    }
}

var testing = false
fun resPrepare() {
    //管卡rename
    var file = File(Guanka.caijiPath + File.separator + "1699083434974")
    var files = file.listFiles().sortedBy {
        it.lastModified()
    }
    files.forEachIndexed { index, file ->
        file.rename("g${index + 1}.png")
    }
}

fun autoMoveMouse() {
    testing = !testing
    GlobalScope.launch {
        while (testing) {
            MRobot.robot.mouseMove(1100, 800)
            delay(150000)
            MRobot.robot.mouseMove(1200, 850)
            delay(150000)
        }
    }
}

//fun testKaiJi() {
//    GlobalScope.launch {
//        delay(3000)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(1000)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(300)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(300)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(300)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(300)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(300)
//        MRobot.singleClickPc(MPoint(1000, 620), null)
//        delay(300)
//        MRobot.inputOneKey(KeyEvent.VK_NUMPAD8)
//        MRobot.inputOneKey(KeyEvent.VK_5)
//        MRobot.inputOneKey(KeyEvent.VK_0)
//        MRobot.inputOneKey(KeyEvent.VK_7)
//        MRobot.inputOneKey(KeyEvent.VK_3)
//        MRobot.inputOneKey(KeyEvent.VK_0)
//        MRobot.inputKeys(KeyEvent.VK_SHIFT, KeyEvent.VK_MINUS)
//        MRobot.inputOneKey(KeyEvent.VK_S)
//        MRobot.inputOneKey(KeyEvent.VK_H)
//        MRobot.inputOneKey(KeyEvent.VK_E)
//        MRobot.inputOneKey(KeyEvent.VK_N)
//        MRobot.singleClickPc(MPoint(1100, 620), null)
//    }
//
//}

fun testSim() {
    val zhanjiang = HeroBean("zhanjiang", 100)
    val nvwang = HeroBean("nvwang", 90)
    val saman = HeroBean("saman", 80)
    val jiaonv = HeroBean("jiaonv", 70)
    val shahuang = HeroBean("shahuang", 60)
    val xiaoye = HeroBean("xiaoye", 50)
    val muqiu = HeroBean("muqiu", 40, needCar = false, compareRate = 0.95)
    val shexian = HeroBean("shexian", 30, needCar = false)
    val guangqiu = HeroBean("guangqiu", 0, needCar = false)

    var heros = arrayListOf<HeroBean>()
    heros.add(zhanjiang)
    heros.add(nvwang)
    heros.add(saman)
    heros.add(jiaonv)
    heros.add(shahuang)
    heros.add(xiaoye)
    heros.add(muqiu)
    heros.add(shexian)
    heros.add(guangqiu)
    val huanqiu = HeroBean("huanqiu", 20, needCar = false, compareRate = 0.95)

    heros.forEach {
        testSim(it, huanqiu)
    }
}

fun deleteSimPic() {
    var list = arrayListOf<File>()
    var imgs = arrayListOf<BufferedImage>()
    var files = File(App.caijiPath).listFiles()
    var toDelete = arrayListOf<File>()
    files.forEach {
        if (it.name.endsWith("png")) {

            var img = getImageFromFile(it)
            var has = false
            imgs.forEach { i ->
                if (ImgUtil.isImageSim(img, i, 0.95)) {
                    has = true
                }
            }
            if (has) {
                toDelete.add(it)
            } else {
                imgs.add(img)
            }
        }
    }
    var fileTo = File(App.caijiPath, "temp")
    fileTo.mkdirs()
    toDelete.forEach {
//        org.apache.commons.io.FileUtils.moveFile(it,fileTo)
        org.apache.commons.io.FileUtils.moveFileToDirectory(it, fileTo, false)
    }
}

fun saveImgTest(file: File, rect: MRect) {
    getImageFromFile(file).saveSubTo(rect, File(App.caijiPath, System.currentTimeMillis().toString() + ".png"))
}

fun test() {
//WxUtil.findWindowAndMove()
//    WxUtil.getText().apply {
//        log(this)
//    }

    ChuanZhangTest.startChuanZhangOberserver()
//    saveImgTest(File(App.caijiPath,"1701233714591.png"),Recognize.IcAdv4Hezuo.rectFinal)
//    autoMoveMouse()
//    testKaiJi()
//    GlobalScope.launch {
//        delay(2000)
//        MRobot.niantie("1234")
//
//    }
//    resPrepare()

//    LongQuanHeroDoing(false).apply {
//        init()
//        start()
//    }

//    var carDoing = CarDoing(0,CarDoing.CheType_MaChe)
//    carDoing.initPositions()
//    var  imgs = arrayListOf<BufferedImage>().apply {
//        add(getImageFromRes("test1.png"))
//        add(getImageFromRes("test2.png"))
//        add(getImageFromRes("test3.png"))
//    }
//    imgs.forEachIndexed { index, bufferedImage ->
//        "".log("第${index}张图片")
//        "".log(bufferedImage)
//        carDoing.testStarAndChuanzhang(bufferedImage)
//    }


//    var img = getImageFromFile(File("C:\\Users\\Administrator\\IdeaProjects\\intellij-sdk-code-samples\\untitled1\\tfres\\boss\\1694773514240name.png"))
//    Boss.nvwangche.testFitImg(img)


}

private fun testSim(hero: HeroBean, hero2: HeroBean) {
    for (i in 0..hero.imgList.size - 1) {
        var img = hero.imgList[i]

        if (hero2.fitImage(img)) {
            hero.log(img)
            hero.log("图相似")
        } else {
            hero.log("图不同")
        }

    }
}


fun Any.log(msg: Any, onlyPrint: Boolean = false) {
    println(msg.toString())
    if (!onlyPrint) {
        LogUtil.messages.add(0, msg)
    }
}

fun Any.logOnly(msg: Any) {
    log(msg, true)
}

fun logWin(win: WinDef.HWND) {
    var img = TTTT.getScreenshot(win)
    img!!.log(img!!)
//    User32.INSTANCE.EnumChildWindows(win, object : WinUser.WNDENUMPROC {
//        override fun callback(p0: WinDef.HWND, p1: Pointer?): Boolean {
//            logWin(p0)
//            return true
//        }
//
//    }, Pointer.createConstant(0))
}


fun main() = application {
    App.initPath()
    if (App.windowClose.value > 0) {
        App.closeApp()
        testing = false
        GlobalScope.launch {
            delay(3000)
            exitApplication()
        }
    }
    Window(onCloseRequest = {
        App.closeApp()
        exitApplication()
    }, title = "塔防助手") {
        App()
        tttt(this)
//        this.window.setLocation(1920 - this.window.width, 0)
    }
}

fun tttt(fw: FrameWindowScope) {
    GlobalScope.launch {
        delay(300)
        fw.window.setLocation(1920 - fw.window.width, 0)

    }
}


//fun main() {
//    var over = false
////    Window.findWindowWithName("承澄")
////    println("fit:${Recognize.CanreJujue.isFit()}")
////    ADBUtil.jietu()
////    println("${Recognize.CanreJujue.isFit()}")
////    GlobalScope.launch {
////        inputRoom("1234")
////    }
////    ADBUtil.tap(MPoint(300,300))
////    ADBUtil.inputText("1234")
////    ADBUtil.tap(MPoint(300,1200))
//
//    var img = getImageFromRes(Recognize.CanreJujue.resNameFinal)
//    var img2 = getImageFromRes(Recognize.CanreJujueFail.resNameFinal)
//    println(ImgUtil.isImageSim(img,img2))
//    while (!over){
//
//    }
//}

//private suspend fun inputRoom(text: String) {
////    while (!Recognize.Duizhan.isFit()) {//等回到“对战” 首页
////        delay(Config.delayNor)
////    }
//    Config.hezuo_startPoint.click()
//    delay(Config.delayNor)
////        Config.hezuo_friend.click()
////        delay(Config.delayNor)
////        Config.hezuo_Join.click()
////        delay(Config.delayNor)
//    Config.hezuo_room_input_game.click()
//    delay(Config.delayNor)
//
////        Config.hezuo_room_input_wx.click()
////    MRobot.niantie(text)
////    MRobot.adbInput(text)
//    delay(Config.delayNor)
////        Config.hezuo_room_input_wx_over.click()
////        delay(Config.delayNor)
//    Config.hezuo_Join_Sure.click()
//}