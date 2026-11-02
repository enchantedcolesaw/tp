package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentName;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/**
 * Represents the in-memory model of the TAssist data and the user prefs.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TAssist tAssist;
    private final UserPrefs userPrefs;
    private final ActiveGroupTracker activeGroupTracker = new ActiveGroupTracker();
    private final FilteredList<Student> filteredStudents =
            new FilteredList<>(activeGroupTracker.getStudentList());
    private final SortedList<Student> displayedStudents = new SortedList<>(filteredStudents);
    private final ReadOnlyBooleanWrapper hasStudentFilter = new ReadOnlyBooleanWrapper();
    private final ObservableList<Student> unmodifiableDisplayedStudents =
            FXCollections.unmodifiableObservableList(displayedStudents);
    private TAssistHistory history = new TAssistHistory();

    /**
     * Initializes a ModelManager with copies of the given tAssist and userPrefs.
     */
    public ModelManager(ReadOnlyTAssist tAssist, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(tAssist, userPrefs);

        logger.fine("Initializing with TAssist data: " + tAssist + " and user prefs " + userPrefs);

        this.tAssist = new TAssist(tAssist);
        this.userPrefs = new UserPrefs(userPrefs);
        hasStudentFilter.bind(filteredStudents.predicateProperty().isNotNull());
        trackActiveGroup();
    }

    /**
     * Initializes a ModelManager with empty TAssist data and default user prefs.
     */
    public ModelManager() {
        this(new TAssist(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== TAssist ===================================================================================

    @Override
    public void setTAssist(ReadOnlyTAssist tAssist) {
        requireNonNull(tAssist);
        boolean isActiveGroupRenamed = isActiveGroupRenamed(this.tAssist, tAssist);
        this.tAssist.resetData(tAssist);
        if (isActiveGroupRenamed) {
            // As with renameGroup, the same roster stays active under its other name, so the display settings stay.
            followActiveGroup();
        } else {
            trackActiveGroup();
        }
    }

    /**
     * Returns true if the active group of {@code next} is that of {@code current} under another name, as when a
     * rename of the active group is undone or redone: neither name is a group in the other data.
     */
    private static boolean isActiveGroupRenamed(ReadOnlyTAssist current, ReadOnlyTAssist next) {
        Optional<GroupName> currentName = current.getActiveGroup().map(Group::getName);
        Optional<GroupName> nextName = next.getActiveGroup().map(Group::getName);
        return currentName.isPresent() && nextName.isPresent()
                && !hasGroupNamed(next, currentName.get()) && !hasGroupNamed(current, nextName.get());
    }

    private static boolean hasGroupNamed(ReadOnlyTAssist data, GroupName name) {
        return data.getGroupList().stream().anyMatch(group -> group.getName().equals(name));
    }

    @Override
    public ReadOnlyTAssist getTAssist() {
        return tAssist;
    }

    @Override
    public boolean hasGroup(GroupName name) {
        requireNonNull(name);
        return tAssist.hasGroup(name);
    }

    @Override
    public void addGroup(Group group) {
        requireNonNull(group);
        tAssist.addGroup(group);
    }

    @Override
    public Group getGroup(GroupName name) {
        requireNonNull(name);
        return tAssist.getGroup(name);
    }

    @Override
    public Group removeGroup(GroupName name) {
        requireNonNull(name);
        Group removed = tAssist.removeGroup(name);
        logger.info("Removed group " + removed.getName());
        trackActiveGroup();
        return removed;
    }

    @Override
    public void renameGroup(GroupName name, GroupName newName) {
        requireAllNonNull(name, newName);
        tAssist.renameGroup(name, newName);
        logger.info("Renamed group " + name + " to " + newName);
        // A rename replaces the group object, but keeps the same roster, so the display settings stay.
        followActiveGroup();
    }

    @Override
    public void setActiveGroup(GroupName name) {
        requireNonNull(name);
        tAssist.setActiveGroup(name);
        logger.info("Active group is now " + name);
        trackActiveGroup();
    }

    @Override
    public Optional<Group> getActiveGroup() {
        return tAssist.getActiveGroup();
    }

    @Override
    public void addStudent(GroupName groupName, Student student) {
        requireAllNonNull(groupName, student);
        tAssist.addStudent(groupName, student);
    }

    @Override
    public void setStudent(GroupName groupName, Student student) {
        requireAllNonNull(groupName, student);
        tAssist.setStudent(groupName, student);
    }

    @Override
    public Student removeStudent(GroupName groupName, StudentId studentId) {
        requireAllNonNull(groupName, studentId);
        return tAssist.removeStudent(groupName, studentId);
    }

    @Override
    public void replaceStudent(GroupName groupName, StudentId studentId, Student replacement) {
        requireAllNonNull(groupName, studentId, replacement);
        tAssist.replaceStudent(groupName, studentId, replacement);
        logger.fine("Replaced a student in " + groupName);
    }

    @Override
    public void addAssignment(GroupName groupName, Assignment assignment) {
        requireAllNonNull(groupName, assignment);
        tAssist.addAssignment(groupName, assignment);
    }

    @Override
    public void setAssignment(GroupName groupName, Assignment assignment) {
        requireAllNonNull(groupName, assignment);
        tAssist.setAssignment(groupName, assignment);
    }

    @Override
    public Assignment removeAssignment(GroupName groupName, AssignmentName assignmentName) {
        requireAllNonNull(groupName, assignmentName);
        return tAssist.removeAssignment(groupName, assignmentName);
    }

    @Override
    public void renameAssignment(GroupName groupName, AssignmentName assignmentName, AssignmentName newName) {
        requireAllNonNull(groupName, assignmentName, newName);
        tAssist.renameAssignment(groupName, assignmentName, newName);
    }

    //=========== Undo and redo =============================================================================

    @Override
    public void commitTAssist(ReadOnlyTAssist previousTAssist, String description) {
        requireAllNonNull(previousTAssist, description);
        history.record(previousTAssist, description);
    }

    @Override
    public boolean canUndoTAssist() {
        return history.canUndo();
    }

    @Override
    public boolean canRedoTAssist() {
        return history.canRedo();
    }

    @Override
    public String undoTAssist() {
        TAssistHistory.Version undone = history.undo(tAssist);
        setTAssist(undone.data());
        logger.info("Undid: " + undone.description());
        return undone.description();
    }

    @Override
    public String redoTAssist() {
        TAssistHistory.Version redone = history.redo(tAssist);
        setTAssist(redone.data());
        logger.info("Redid: " + redone.description());
        return redone.description();
    }

    @Override
    public TAssistHistory getTAssistHistory() {
        return new TAssistHistory(history);
    }

    @Override
    public void setTAssistHistory(TAssistHistory history) {
        requireNonNull(history);
        this.history = new TAssistHistory(history);
    }

    //=========== Active Group Accessors =====================================================================

    @Override
    public ReadOnlyObjectProperty<Optional<Group>> activeGroupProperty() {
        return activeGroupTracker.activeGroupProperty();
    }

    @Override
    public ObservableList<Student> getActiveGroupStudentList() {
        return activeGroupTracker.getStudentList();
    }

    @Override
    public ObservableList<Student> getDisplayedStudentList() {
        return unmodifiableDisplayedStudents;
    }

    @Override
    public ReadOnlyBooleanProperty studentFilterActiveProperty() {
        return hasStudentFilter.getReadOnlyProperty();
    }

    @Override
    public void filterStudents(Predicate<Student> predicate) {
        filteredStudents.setPredicate(requireNonNull(predicate));
        logger.fine("Updated the student display filter");
    }

    @Override
    public void sortStudents(Comparator<Student> comparator) {
        displayedStudents.setComparator(requireNonNull(comparator));
        logger.fine("Updated the student display order");
    }

    @Override
    public void resetStudentDisplay() {
        filteredStudents.setPredicate(null);
        displayedStudents.setComparator(null);
    }

    @Override
    public StudentDisplayState getStudentDisplayState() {
        return new StudentDisplayState(filteredStudents.getPredicate(), displayedStudents.getComparator());
    }

    @Override
    public void setStudentDisplayState(StudentDisplayState state) {
        requireNonNull(state);
        filteredStudents.setPredicate(state.predicate());
        displayedStudents.setComparator(state.comparator());
    }

    /**
     * Points the active group views at the group that is active in {@code tAssist}.
     */
    private void trackActiveGroup() {
        if (!activeGroupTracker.activeGroupProperty().get().map(Group::getName)
                .equals(tAssist.getActiveGroup().map(Group::getName))) {
            resetStudentDisplay();
        }
        followActiveGroup();
    }

    /**
     * Points the active group views at the group that is active in {@code tAssist}, keeping the display settings.
     */
    private void followActiveGroup() {
        activeGroupTracker.track(tAssist.getActiveGroup());
        assert activeGroupTracker.activeGroupProperty().get().equals(tAssist.getActiveGroup())
                : "The active group views must show the active group";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return tAssist.equals(otherModelManager.tAssist)
                && userPrefs.equals(otherModelManager.userPrefs);
    }

}
