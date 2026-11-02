package seedu.address.ui;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

/**
 * Scrolls text areas by a page at a time without moving past the start or end of their text.
 */
final class PageScrolling {
    private PageScrolling() {}

    /**
     * Returns how far {@code area} can scroll down, which is 0 if all of its text is visible.
     * Before {@code area} is laid out, there is no limit.
     */
    static double getMaxScrollTop(TextArea area) {
        if (area.lookup(".scroll-pane") instanceof ScrollPane pane && pane.getContent() != null) {
            double textHeight = pane.getContent().prefHeight(pane.getViewportBounds().getWidth());
            return Math.max(0, textHeight - pane.getViewportBounds().getHeight());
        }
        return Double.MAX_VALUE;
    }

    /**
     * Scrolls {@code area} by {@code pageHeight} towards the end of its text if {@code isDown}, or its start otherwise.
     */
    static void scrollPage(TextArea area, double pageHeight, boolean isDown) {
        double scrollTop = area.getScrollTop() + (isDown ? pageHeight : -pageHeight);
        area.setScrollTop(Math.clamp(scrollTop, 0, getMaxScrollTop(area)));
    }
}
