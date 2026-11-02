package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.group.Group;
import seedu.address.model.student.Student;

/**
 * Shows the students in the active tutorial group, or explains why there are none to show.
 */
public class StudentListPanel extends UiPart<Region> {
    static final String NO_ACTIVE_GROUP = "No active group";
    static final String STUDENTS_TITLE = "Students";
    static final String NO_ACTIVE_GROUP_HINT = "Add a tutorial group and make it active to see its students here.";
    static final String SELECT_GROUP_HINT = "Select a group with group use g/GROUP_NAME to see its students here.";
    static final String DATA_NOT_READ = "Saved data could not be read";
    static final String DATA_NOT_READ_HINT =
            "Close TAssist, repair or move the data file, and restart. Any command other than help, view or exit "
                    + "shows why.";
    static final String EMPTY_GROUP_TITLE_FORMAT = "No students in %s yet";
    static final String EMPTY_GROUP_HINT_FORMAT = "Students you add to %s will appear here.";

    private static final String FXML = "StudentListPanel.fxml";
    private static final double ROW_NUMBER_WIDTH = 56;
    private static final double NAME_COLUMN_WIDTH = 280;
    private static final double ID_COLUMN_WIDTH = 260;
    /** Wide enough for a 20-character student ID, so that the ID the TA must type is shown in full. */
    private static final double ID_COLUMN_MAX_WIDTH = 260;
    private static final double TOOLTIP_MAX_WIDTH = 480;

    @FXML
    private TableView<Student> studentTable;
    @FXML
    private Label groupTitle;
    @FXML
    private Label studentCount;
    @FXML
    private VBox emptyState;
    @FXML
    private Label emptyTitle;
    @FXML
    private Label emptyHint;

    /**
     * Creates a table of the active group's students that follows {@code activeGroup} and {@code students}.
     * The empty state suggests selecting a group when {@code groups} is not empty but none is active.
     * If {@code isDataLoadFailed}, the empty state says the saved data could not be read.
     */
    public StudentListPanel(ObservableValue<Optional<Group>> activeGroup, ObservableList<Group> groups,
            ObservableList<Student> roster, ObservableList<Student> students, ObservableBooleanValue hasStudentFilter,
            Consumer<Student> studentClickHandler, boolean isDataLoadFailed) {
        super(FXML);
        requireAllNonNull(activeGroup, groups, roster, students, hasStudentFilter, studentClickHandler);

        studentTable.setItems(students);
        followStudentAdditions(activeGroup, roster, students, row -> {
            studentTable.requestLayout();
            studentTable.layout();
            // Scrolling just past the final row clamps to the bottom, including the entire new row.
            studentTable.scrollTo(row == students.size() - 1 ? students.size() : row);
        }, Platform::runLater);
        setUpStudentClicks(studentClickHandler);
        addColumn("#", ROW_NUMBER_WIDTH, student -> Integer.toString(students.indexOf(student) + 1))
                .setMaxWidth(ROW_NUMBER_WIDTH);
        addColumn("NAME", NAME_COLUMN_WIDTH, student -> student.getName().toString());
        addColumn("STUDENT ID", ID_COLUMN_WIDTH, student -> student.getStudentId().toString())
                .setMaxWidth(ID_COLUMN_MAX_WIDTH);
        // Keep the number and ID columns readable while giving the name all remaining table width.
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Row numbers depend on positions, which change when another student is removed.
        students.addListener((ListChangeListener<Student>) change -> studentTable.refresh());

        groupTitle.textProperty().bind(Bindings.createStringBinding(() -> describeTitle(activeGroup.getValue()),
                activeGroup));
        Tooltip fullTitle = new Tooltip();
        fullTitle.textProperty().bind(groupTitle.textProperty());
        groupTitle.setTooltip(fullTitle);
        BooleanBinding hasActiveGroup = Bindings.createBooleanBinding(() -> activeGroup.getValue().isPresent(),
                activeGroup);
        studentCount.visibleProperty().bind(hasActiveGroup);
        studentCount.managedProperty().bind(hasActiveGroup);
        studentCount.textProperty().bind(Bindings.createStringBinding(() ->
                hasStudentFilter.get() ? students.size() + " of " + describeCount(roster.size())
                        : describeCount(students.size()), students, roster, hasStudentFilter));
        emptyTitle.textProperty().bind(Bindings.createStringBinding(() ->
                describeEmptyTitle(activeGroup.getValue(), hasStudentFilter.get(),
                        isDataLoadFailed), activeGroup, hasStudentFilter));
        emptyHint.textProperty().bind(Bindings.createStringBinding(() ->
                describeEmptyHint(activeGroup.getValue(), !groups.isEmpty(), hasStudentFilter.get(),
                        isDataLoadFailed), activeGroup, groups, hasStudentFilter));
        showEmptyStateInsteadOfTable(students);
    }

    /**
     * Scrolls to a sole new roster member after the displayed list has caught up with the model.
     * ActiveGroupTracker replaces the roster on every change, so displayed-list add events alone cannot
     * distinguish additions from searches or group switches. A deferred group check excludes switches.
     */
    static void followStudentAdditions(ObservableValue<Optional<Group>> activeGroup,
            ObservableList<Student> roster, ObservableList<Student> displayedStudents,
            Consumer<Integer> scrollTo, Consumer<Runnable> runLater) {
        List<Student> previousStudents = new ArrayList<>(roster);
        roster.addListener((ListChangeListener<Student>) change -> {
            Optional<Group> group = activeGroup.getValue();
            Student added = null;
            if (roster.size() == previousStudents.size() + 1 && roster.containsAll(previousStudents)) {
                added = roster.stream().filter(student -> !previousStudents.contains(student)).findFirst().orElse(null);
            }
            previousStudents.clear();
            previousStudents.addAll(roster);
            if (added == null || group.isEmpty()) {
                return;
            }
            Student newStudent = added;
            runLater.accept(() -> {
                if (activeGroup.getValue().orElse(null) != group.get()) {
                    return;
                }
                int row = displayedStudents.indexOf(newStudent);
                if (row >= 0) {
                    scrollTo.accept(row);
                }
            });
        });
    }

    /**
     * Handles explicit primary-button clicks on populated rows, including an already selected student.
     */
    private void setUpStudentClicks(Consumer<Student> studentClickHandler) {
        studentTable.setRowFactory(table -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && !row.isEmpty()) {
                    studentClickHandler.accept(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * Shows the explanation in place of the table, without its column headings, while there are no students.
     */
    private void showEmptyStateInsteadOfTable(ObservableList<Student> students) {
        BooleanBinding hasNoStudents = Bindings.isEmpty(students);
        emptyState.visibleProperty().bind(hasNoStudents);
        emptyState.managedProperty().bind(hasNoStudents);
        studentTable.visibleProperty().bind(hasNoStudents.not());
    }

    /**
     * Adds a column that shows {@code value} for each student, and returns the column.
     */
    private TableColumn<Student, String> addColumn(String title, double minWidth, Function<Student, String> value) {
        TableColumn<Student, String> column = new TableColumn<>(title);
        column.setMinWidth(minWidth);
        column.setPrefWidth(minWidth);
        column.setSortable(false);
        column.setReorderable(false);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        column.setCellFactory(unused -> new StudentCell());
        studentTable.getColumns().add(column);
        return column;
    }

    /**
     * Returns the student count shown in the heading, such as "1 student" or "3 students".
     */
    static String describeCount(int count) {
        assert count >= 0 : "A group cannot have a negative number of students";
        return StringUtil.pluralize(count, "student");
    }

    /**
     * Returns the card title, which is the name of the active group, or a general title when no group is active.
     */
    static String describeTitle(Optional<Group> activeGroup) {
        return activeGroup.map(group -> group.getName().toString()).orElse(STUDENTS_TITLE);
    }

    /**
     * Returns the title shown in place of the table when there are no students to show.
     */
    static String describeEmptyTitle(Optional<Group> activeGroup, boolean hasStudentFilter,
            boolean isDataLoadFailed) {
        if (isDataLoadFailed) {
            return DATA_NOT_READ;
        }
        if (activeGroup.isPresent() && hasStudentFilter) {
            return "No matching students";
        }
        return activeGroup.map(group -> String.format(EMPTY_GROUP_TITLE_FORMAT, group.getName()))
                .orElse(NO_ACTIVE_GROUP);
    }

    /**
     * Returns the hint shown in place of the table when there are no students to show.
     */
    static String describeEmptyHint(Optional<Group> activeGroup, boolean hasGroups, boolean hasStudentFilter,
            boolean isDataLoadFailed) {
        if (isDataLoadFailed) {
            return DATA_NOT_READ_HINT;
        }
        if (activeGroup.isPresent() && hasStudentFilter) {
            return "Use student list to show all students.";
        }
        return activeGroup.map(group -> String.format(EMPTY_GROUP_HINT_FORMAT, group.getName()))
                .orElse(hasGroups ? SELECT_GROUP_HINT : NO_ACTIVE_GROUP_HINT);
    }

    /**
     * Shows a value in a table cell, with a tooltip that keeps the full value readable when the cell is too narrow.
     */
    private static class StudentCell extends TableCell<Student, String> {
        @Override
        protected void updateItem(String value, boolean isEmpty) {
            super.updateItem(value, isEmpty);
            boolean hasValue = !isEmpty && value != null;
            setText(hasValue ? value : null);
            setTooltip(hasValue ? createTooltip(value) : null);
        }

        private static Tooltip createTooltip(String value) {
            Tooltip tooltip = new Tooltip(value);
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
            return tooltip;
        }
    }
}
