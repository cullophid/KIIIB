package gui

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util.PPaintContext

object DrawsGrid {
  trait Properties {
    def pixelsPerUnit: Double
    def numCells: Int

    /** The x offset in "Unit" coordinates */
    def offsetX: Double = 0.0

    /** The y offset in "Unit" coordinates */
    def offsetY: Double = 0.0
  }
}

trait DrawsGrid extends PNode {
  val properties: DrawsGrid.Properties

  def drawGrid(pc: PPaintContext, cellSize: Double) {
    val r = pc.getLocalClip
    val g = pc.getGraphics
    val c = pc.getCamera

    val (clipX, clipY, clipWidth, clipHeight) = (r.getX toInt, r.getY toInt, r.getWidth toInt, r.getHeight toInt)

    val (viewX, viewY, viewWidth, viewHeight) = (c.getViewBounds.getX,
                                                 c.getViewBounds.getY,
                                                 c.getViewBounds.getWidth,
                                                 c.getViewBounds.getHeight)
    val scale = c.getViewScale

    val offsetX = (properties.offsetX + viewX) % cellSize
    val offsetY = (properties.offsetY + viewY) % cellSize

    // draw vertical lines
    var i: Double = -offsetX + Math.signum(offsetX) * cellSize
    while (i < viewWidth + offsetX) {
      val x = (i * scale).toInt
      g.drawLine(x, clipY,
                 x, clipY + clipHeight)
      i += cellSize
    }

    // draw vertical lines
    var j: Double = -offsetY + Math.signum(offsetY) * cellSize
    while (j < viewHeight + offsetY) {
      val y = (j * scale).toInt
      g.drawLine(clipX, y,
                 clipX + clipWidth, y)
      j += cellSize
    }

  }

  override protected
  def paint(pc: PPaintContext) {
    val r = pc.getLocalClip
    val g = pc.getGraphics
    val c = pc.getCamera

    val renderQuality = pc.getRenderQuality
    pc.setRenderQuality(PPaintContext.LOW_QUALITY_RENDERING)

    val cellSize = properties.pixelsPerUnit / properties.numCells

    if (cellSize * c.getViewScale > 4.0) {
      // draw fine grid
      g.setColor(new java.awt.Color(0xd3d7cf))
      drawGrid(pc, cellSize)

      g.setColor(new java.awt.Color(0x888a85))
    }
    else {
      // lighten the larger grid colors when not showing the smaller one
      g.setColor(new java.awt.Color(0xd3d7cf))
    }

    // draw coarse grid
    drawGrid(pc, properties.pixelsPerUnit)

    pc.setRenderQuality(renderQuality)

    super.paint(pc)
  }
}

// vim: set ts=2 sw=2 et:

