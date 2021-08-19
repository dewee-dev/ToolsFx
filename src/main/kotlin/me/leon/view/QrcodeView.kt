package me.leon.view

import java.awt.Rectangle
import java.awt.Robot
import java.io.File
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.*
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlin.math.abs
import me.leon.ext.*
import tornadofx.*

class QrcodeView : View("Qrcode") {
    override val root = vbox {
        val bu = button("截屏") { action { this@QrcodeView.show() } }

        button("生成二维码") { action { iv!!.image = createQR() } }
        button("识别二维码") { action { File("E:/qr_tmp.png").qrReader() } }
        iv = imageview {}
        val keyCombination: KeyCombination = KeyCombination.valueOf("ctrl+alt+p")
        val mc = Mnemonic(bu, keyCombination)
        scene?.addMnemonic(mc)
    }

    private var startX // 切图区域的起始位置x
     =
        0.0
    private var startY // 切图区域的起始位置y
     =
        0.0
    var w // 切图区域宽
     =
        0.0
    var h // 切图区域高
     =
        0.0
    var hBox // 切图区域
    : HBox? =
        null
    var iv // 切成的图片展示区域
    : ImageView? =
        null

    private fun show() {
        // 将主舞台缩放到任务栏
        primaryStage.isIconified = true
        // 创建辅助舞台，并设置场景与布局
        val stage = Stage()
        // 锚点布局采用半透明
        val anchorPane = AnchorPane()
        anchorPane.style = "-fx-background-color: #85858522"
        // 场景设置白色全透明
        val scene = Scene(anchorPane)
        scene.fill = Paint.valueOf("#ffffff00")
        stage.scene = scene
        // 清楚全屏中间提示文字
        stage.fullScreenExitHint = ""
        stage.initStyle(StageStyle.TRANSPARENT)
        stage.isFullScreen = true
        stage.show()

        // 切图窗口绑定鼠标按下事件
        anchorPane.onMousePressed =
            EventHandler { event: MouseEvent ->
                // 清除锚点布局中所有子元素
                anchorPane.children.clear()
                // 创建切图区域
                hBox =
                    HBox().apply {
                        background = null
                        border =
                            Border(
                                BorderStroke(
                                    Paint.valueOf("#c03700"),
                                    BorderStrokeStyle.SOLID,
                                    null,
                                    BorderWidths(3.0)
                                )
                            )
                    }
                anchorPane.children.add(hBox)
                // 记录并设置起始位置
                startX = event.sceneX
                startY = event.sceneY
                AnchorPane.setLeftAnchor(hBox, startX)
                AnchorPane.setTopAnchor(hBox, startY)
            }
        // 绑定鼠标按下拖拽的事件
        addMouseDraggedEvent(anchorPane)

        // 绑定鼠标松开事件
        addMouseReleasedEvent(anchorPane, stage)
        scene.onKeyPressed =
            EventHandler { event: KeyEvent ->
                if (event.code == KeyCode.ESCAPE) {
                    stage.close()
                    primaryStage.isIconified = false
                }
            }
    }

    private fun addMouseReleasedEvent(anchorPane: AnchorPane, stage: Stage) {
        anchorPane.onMouseReleased =
            EventHandler { event: MouseEvent ->
                // 记录最终长宽
                w = abs(event.sceneX - startX)
                h = abs(event.sceneY - startY)
                anchorPane.style = "-fx-background-color: #00000000"
                // 添加剪切按钮，并显示在切图区域的底部
                val b = Button("剪切")
                hBox!!.border =
                    Border(
                        BorderStroke(
                            Paint.valueOf("#85858544"),
                            BorderStrokeStyle.SOLID,
                            null,
                            BorderWidths(3.0)
                        )
                    )
                hBox!!.children.add(b)
                hBox!!.alignment = Pos.BOTTOM_RIGHT
                // 为切图按钮绑定切图事件
                b.onAction =
                    EventHandler {
                        // 切图辅助舞台小时
                        stage.close()

                        try {
                            captureImg()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        // 主舞台还原
                        primaryStage.isIconified = false
                    }
            }
    }

    private fun addMouseDraggedEvent(anchorPane: AnchorPane) {
        anchorPane.onMouseDragged =
            EventHandler { event: MouseEvent ->
                // 用label记录切图区域的长宽
                val label =
                    Label().apply {
                        alignment = Pos.CENTER
                        prefHeight = 30.0
                        prefWidth = 170.0
                        textFill = Paint.valueOf("#ffffff") // 白色填充
                        style = "-fx-background-color: #000000" // 黑背景
                    }

                anchorPane.children.add(label)
                AnchorPane.setLeftAnchor(label, startX + 30)
                AnchorPane.setTopAnchor(label, startY)

                // 计算宽高并且完成切图区域的动态效果
                w = abs(event.sceneX - startX)
                h = abs(event.sceneY - startY)
                hBox!!.prefWidth = w
                hBox!!.prefHeight = h
                label.text = "宽：$w 高：$h"
            }
    }

    @Throws(Exception::class)
    fun captureImg() {
        val robot = Robot()
        val re = Rectangle(startX.toInt(), startY.toInt(), w.toInt(), h.toInt())
        val screenCapture = robot.createScreenCapture(re)
        val bufferedImage = screenCapture.toFxImg()
        iv!!.image = bufferedImage

        bufferedImage.copy()
        screenCapture.writeFile("E:/tmp.png")

        File("E:/tmp.png").qrReader()
    }

    private fun createQR(data: String = "E:/tmp.png"): Image {
        val bufferedImage = data.createQR()
        bufferedImage.writeFile("E:/qr_tmp.png")
        return bufferedImage.toFxImg()
    }
}
