package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 * It grows to fit the message, up to {@link #MAX_VISIBLE_LINES} lines. A longer message scrolls.
 */
public class ResultDisplay extends UiPart<Region> {
    static final int MAX_VISIBLE_LINES = 4;

    private static final String FXML = "ResultDisplay.fxml";
    private static final String SUCCESS_ICON = "✓";
    private static final String FAILURE_ICON = "✕";
    /** Width of the text area that text cannot use: its padding and a vertical scroll bar. */
    private static final double NON_TEXT_WIDTH = 30;

    /** Measures how many lines a message wraps to. It is never shown. */
    private final Text measurement = new Text();

    @FXML
    private TextArea resultDisplay;
    @FXML
    private HBox feedbackPane;
    @FXML
    private Label feedbackIcon;

    /**
     * Creates the command feedback area.
     */
    public ResultDisplay() {
        super(FXML);
        resultDisplay.textProperty().addListener(observable -> fitToText());
        resultDisplay.widthProperty().addListener(observable -> fitToText());
        resultDisplay.fontProperty().addListener(observable -> fitToText());
        fitToText();
    }

    public void setFeedbackToUser(String feedbackToUser) {
        setFeedbackToUser(feedbackToUser, false);
    }

    /**
     * Displays command feedback with an explicit failure state.
     */
    public void setFeedbackToUser(String feedbackToUser, boolean isFailure) {
        requireNonNull(feedbackToUser);
        feedbackPane.pseudoClassStateChanged(PseudoClass.getPseudoClass("failure"), isFailure);
        feedbackIcon.setText(isFailure ? FAILURE_ICON : SUCCESS_ICON);
        resultDisplay.setText(feedbackToUser);
    }

    /**
     * Scrolls the feedback by one visible page, keeping one line of the previous page in view.
     *
     * @param isDown Whether to scroll towards the end of the message instead of its start.
     */
    public void scrollPage(boolean isDown) {
        double pageHeight = Math.max(measureLineHeight(), resultDisplay.getHeight() - measureLineHeight());
        PageScrolling.scrollPage(resultDisplay, pageHeight, isDown);
    }

    /**
     * Returns true if some of the message is hidden, so that it needs scrolling to read.
     */
    public boolean hasLongMessage() {
        return PageScrolling.getMaxScrollTop(resultDisplay) > 0;
    }

    /**
     * Shows as many lines as the message needs, up to {@link #MAX_VISIBLE_LINES}. Longer messages scroll.
     */
    private void fitToText() {
        resultDisplay.setPrefRowCount(Math.clamp(countLines(), 1, MAX_VISIBLE_LINES));
    }

    /**
     * Returns the number of lines the message takes up at the current width, including wrapped lines.
     */
    int countLines() {
        double lineHeight = measureLineHeight();
        measurement.setText(resultDisplay.getText());
        double textHeight = measurement.getLayoutBounds().getHeight();
        assert lineHeight > 0 : "A line of text must have a height";
        return (int) Math.round(textHeight / lineHeight);
    }

    private double measureLineHeight() {
        measurement.setFont(resultDisplay.getFont());
        measurement.setWrappingWidth(Math.max(0, resultDisplay.getWidth() - NON_TEXT_WIDTH));
        measurement.setText("X");
        return measurement.getLayoutBounds().getHeight();
    }
}
