package gui

import scala.swing._
import scala.swing.event._

abstract class ViewPage(title: String, content: Component, tip: String) extends TabbedPane.Page(title, content, tip) {
  def viewEnter
  def viewLeave
}

class ViewPane(viewPages: Seq[ViewPage]) extends TabbedPane {
  private var _currentView: Option[ViewPage] = None

  tabLayoutPolicy = TabbedPane.Layout.Scroll

  listenTo(selection)

  viewPages foreach ( viewPage => pages += viewPage)

  reactions += {
    case SelectionChanged(_) if selection.index != -1 => {
      val view = viewPages.find (_.content == selection.page.content)

      view map { v =>
        _currentView map (_.viewLeave)
        _currentView = Some(v)
        _currentView map (_.viewEnter)
      }
    }
  }

  selection publish SelectionChanged(this)

}


// vim: set ts=2 sw=2 et:
