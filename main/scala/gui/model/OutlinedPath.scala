package gui.model

import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.util._
import edu.umd.cs.piccolo.nodes._

trait OutlinedPath extends PPath with Outlined {
  override def paint(pc: PPaintContext) {

    //
    // reimplement the PPath.paint since setStrokePaint invokes too many
    // invalidPaints and kills performance
    //

    val p = getPaint;
    val g2 = pc.getGraphics();

    val path = getPathReference
    val stroke = getStroke
    val strokePaint = getStrokePaint

    if (p != null) {
      g2.setPaint(p);
      g2.fill(path);
    }

    if (stroke != null && strokePaint != null) {
      g2.setPaint(outlineColor);
      g2.setStroke(stroke);
      g2.draw(path);
    }
  }
}

// vim: set ts=2 sw=2 et:

