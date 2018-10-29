package com.clicktale.pipeline.regression.tutorial

import scala.swing._

object GUIYOT {
  def main(args: Array[String]): Unit = {
    val frame = new MainFrame {
      title = "Asia Rules"
      contents = new Button {
        text = "Click Me!"
      }
      size = new Dimension(500,500)
      centerOnScreen()

    }

    val frame2 = new MainFrame {
      title = "Asia Rules 2"
      //contents = Button("xdfgfzdgf")(println("orint"))
      contents = new TextArea()
      size = new Dimension(500,500)
      centerOnScreen()

    }

    frame.visible = true
    frame2.visible = true
  }




}
