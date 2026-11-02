package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalAssignments.PS1;
import static seedu.address.testutil.TypicalAssignments.PS2;
import static seedu.address.testutil.TypicalStudents.ALICE;
import static seedu.address.testutil.TypicalStudents.FIONA;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyTAssist;
import seedu.address.model.TAssist;
import seedu.address.model.assignment.Assignment;
import seedu.address.model.assignment.AssignmentRecord;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.Student;
import seedu.address.testutil.GroupBuilder;
import seedu.address.testutil.TypicalGroups;

public class JsonTAssistStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonTAssistStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void constructor_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new JsonTAssistStorage(null));
    }

    @Test
    public void readTAssist_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readTAssist(null));
    }

    private Optional<ReadOnlyTAssist> readTAssist(String filePath) throws Exception {
        return new JsonTAssistStorage(Paths.get("unused.json")).readTAssist(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String fileInTestDataFolder) {
        return fileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(fileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readTAssist("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readTAssist("notJsonFormatTAssist.json"));
    }

    @Test
    public void read_wrongJsonStructure_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readTAssist("wrongStructureTAssist.json"));
    }

    @Test
    public void read_emptyFile_exceptionThrown() throws Exception {
        Path emptyFile = Files.createFile(testFolder.resolve("empty.json"));
        assertThrows(DataLoadingException.class, () -> new JsonTAssistStorage(emptyFile).readTAssist());
    }

    @Test
    public void read_invalidAndValidGroups_exceptionThrown() {
        assertReadFailsWithReason("invalidAndValidGroupTAssist.json",
                String.format(JsonAdaptedGroup.MESSAGE_INVALID_GROUP_NAME, "T0@", GroupName.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void read_unknownActiveGroup_loadsGroupsWithNoActiveGroup() throws Exception {
        ReadOnlyTAssist readBack = readTAssist("unknownActiveGroupTAssist.json").orElseThrow();
        assertEquals(1, readBack.getGroupList().size());
        assertEquals(Optional.empty(), readBack.getActiveGroup());
    }

    /**
     * Asserts that reading {@code fileInTestDataFolder} fails because of {@code expectedReason}.
     */
    private void assertReadFailsWithReason(String fileInTestDataFolder, String expectedReason) {
        DataLoadingException e = Assertions.assertThrows(DataLoadingException.class, () ->
                readTAssist(fileInTestDataFolder));
        assertEquals(expectedReason, e.getCause().getMessage());
    }

    @Test
    public void readAndSaveTAssist_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("data").resolve("TempTAssist.json");
        TAssist original = TypicalGroups.getTypicalTAssist();
        Student alice = original.getGroup(TypicalGroups.NAME_T01).getStudentList().getFirst();
        original.setStudent(TypicalGroups.NAME_T01,
                alice.withAttendanceRecord(1, "PRESENT").withParticipationRecord(1, 4));
        JsonTAssistStorage jsonTAssistStorage = new JsonTAssistStorage(filePath);

        // Save in a new file and folder, and read back
        jsonTAssistStorage.saveTAssist(original, filePath);
        ReadOnlyTAssist readBack = jsonTAssistStorage.readTAssist(filePath).orElseThrow();
        assertEquals(original, new TAssist(readBack));

        // Modify data, overwrite existing file, and read back
        original.addGroup(new GroupBuilder().withName(TypicalGroups.NAME_T04).build());
        original.setActiveGroup(TypicalGroups.NAME_T04);
        jsonTAssistStorage.saveTAssist(original, filePath);
        readBack = jsonTAssistStorage.readTAssist(filePath).orElseThrow();
        assertEquals(original, new TAssist(readBack));

        // Save and read without specifying file path
        original.addStudent(TypicalGroups.NAME_T04, FIONA);
        jsonTAssistStorage.saveTAssist(original);
        readBack = jsonTAssistStorage.readTAssist().orElseThrow();
        assertEquals(original, new TAssist(readBack));
    }

    @Test
    public void read_fileWithByteOrderMark_loadsData() throws Exception {
        Path filePath = testFolder.resolve("bom.json");
        TAssist original = TypicalGroups.getTypicalTAssist();
        JsonTAssistStorage jsonTAssistStorage = new JsonTAssistStorage(filePath);
        jsonTAssistStorage.saveTAssist(original);
        Files.writeString(filePath, "\uFEFF" + Files.readString(filePath));

        assertEquals(original, new TAssist(jsonTAssistStorage.readTAssist().orElseThrow()));
    }

    @Test
    public void read_unknownOrRepeatedFields_exceptionThrown() throws Exception {
        Path filePath = testFolder.resolve("invalidKeys.json");
        JsonTAssistStorage storage = new JsonTAssistStorage(filePath);
        storage.saveTAssist(TypicalGroups.getTypicalTAssist());
        String validData = Files.readString(filePath);
        for (String field : List.of("groups", "students", "attendance", "participation", "assignmentRecords")) {
            String misspelledField = Character.toUpperCase(field.charAt(0)) + field.substring(1);
            Files.writeString(filePath, validData.replace("\"" + field + "\"", "\"" + misspelledField + "\""));
            assertThrows(DataLoadingException.class, storage::readTAssist);
        }
        for (String invalidData : List.of("{\"groups\":[],\"groups\":[]}",
                "{\"groups\":[{\"name\":\"T01\",\"name\":\"T02\"}]}",
                """
                {"groups":[{"name":"T01","students":[{"name":"Alice","studentId":"A1",
                "attendance":{"3":"PRESENT","3":"ABSENT"}}]}]}
                """)) {
            Files.writeString(filePath, invalidData);
            assertThrows(DataLoadingException.class, storage::readTAssist);
        }
    }

    @Test
    public void read_legacyDataWithoutWeeklyRecords_defaultsToEmptyRecords() throws Exception {
        Path filePath = testFolder.resolve("legacy.json");
        String legacyData = """
                {"activeGroup":"T01","groups":[{"name":"T01","students":[
                  {"name":"Alice Tan","studentId":"A0123456X"}]}]}
                """;
        Files.writeString(filePath, legacyData);

        Student loaded = new TAssist(new JsonTAssistStorage(filePath).readTAssist().orElseThrow())
                .getGroup(TypicalGroups.NAME_T01).getStudentList().getFirst();

        assertTrue(loaded.getAttendanceRecords().getRecords().isEmpty());
        assertTrue(loaded.getParticipationRecords().getRecords().isEmpty());
    }

    @Test
    public void readAndSaveTAssist_withAssignments_assignmentsSurviveReload() throws Exception {
        Path filePath = testFolder.resolve("assignments.json");
        TAssist original = TypicalGroups.getTypicalTAssist();
        original.addAssignment(TypicalGroups.NAME_T01, PS2);
        original.addAssignment(TypicalGroups.NAME_T01, PS1);
        original.addAssignment(TypicalGroups.NAME_T02, PS1);
        Student alice = original.getGroup(TypicalGroups.NAME_T01).getStudentList().getFirst();
        original.setStudent(TypicalGroups.NAME_T01,
                alice.withAttendanceRecord(1, "PRESENT").withParticipationRecord(1, 4));
        Assignment gradedPs1 = PS1.withSubmissionStatus(ALICE.getStudentId(), true)
                .withGrade(ALICE.getStudentId(), 87);
        original.setAssignment(TypicalGroups.NAME_T01, gradedPs1);
        JsonTAssistStorage jsonTAssistStorage = new JsonTAssistStorage(filePath);

        jsonTAssistStorage.saveTAssist(original, filePath);
        TAssist readBack = new TAssist(jsonTAssistStorage.readTAssist(filePath).orElseThrow());

        assertEquals(original, readBack);
        assertEquals(List.of(PS2, gradedPs1), readBack.getGroup(TypicalGroups.NAME_T01).getAssignmentList());
        assertTrue(readBack.getGroup(TypicalGroups.NAME_T03).getAssignmentList().isEmpty());
    }

    @Test
    public void read_handEditedAssignments_loadsAssignmentsInFileOrder() throws Exception {
        Path filePath = testFolder.resolve("handEdited.json");
        Files.writeString(filePath, """
                {"activeGroup":"T01","groups":[{"name":"T01","assignments":["PS1", "  PS2 "]}]}
                """);

        TAssist loaded = new TAssist(new JsonTAssistStorage(filePath).readTAssist().orElseThrow());

        assertEquals(List.of(PS1, PS2), loaded.getGroup(TypicalGroups.NAME_T01).getAssignmentList());
    }

    @Test
    public void read_duplicateAssignmentsInFile_exceptionThrown() throws Exception {
        Path filePath = testFolder.resolve("duplicateAssignments.json");
        Files.writeString(filePath, """
                {"groups":[{"name":"T01","assignments":["PS1","Ps1"]}]}
                """);

        DataLoadingException e = Assertions.assertThrows(DataLoadingException.class, () ->
                new JsonTAssistStorage(filePath).readTAssist());

        assertEquals(String.format(JsonAdaptedGroup.MESSAGE_DUPLICATE_ASSIGNMENT, "T01", "Ps1"),
                e.getCause().getMessage());
    }

    @Test
    public void read_fractionalParticipationScore_exceptionThrown() throws Exception {
        assertHandEditedFileRejected("""
                {"groups":[{"name":"T01","students":[
                  {"name":"Alice Tan","studentId":"A1","participation":{"1":5.9}}]}]}
                """, "Group T01, student A1: " + JsonAdaptedStudent.MESSAGE_INVALID_PARTICIPATION_SCORE);
    }

    @Test
    public void read_fractionalGrade_exceptionThrown() throws Exception {
        assertHandEditedFileRejected("""
                {"groups":[{"name":"T01","students":[{"name":"Alice Tan","studentId":"A1"}],
                  "assignments":["PS1"],
                  "assignmentRecords":[
                    {"assignmentName":"PS1","studentId":"A1","submitted":true,"grade":100.9}]}]}
                """, String.format(JsonAdaptedGroup.MESSAGE_INVALID_RECORD, "T01", 1,
                AssignmentRecord.MESSAGE_INVALID_GRADE));
    }

    /**
     * Asserts that reading a data file containing {@code json} fails because of {@code expectedMessage}.
     */
    private void assertHandEditedFileRejected(String json, String expectedMessage) throws IOException {
        Path filePath = testFolder.resolve("handEdited.json");
        Files.writeString(filePath, json);
        DataLoadingException e = Assertions.assertThrows(DataLoadingException.class, () ->
                new JsonTAssistStorage(filePath).readTAssist());
        assertEquals(expectedMessage, e.getCause().getMessage());
    }

    @Test
    public void saveTAssist_partialWriteFailure_preservesSavedFile() throws Exception {
        JsonTAssistStorage storage = new JsonTAssistStorage(testFolder.resolve("tassist.json")) {
            @Override
            void writeTemporaryFile(Path temporaryFile, String json) throws IOException {
                Files.writeString(temporaryFile, "partial JSON");
                throw new IOException("Writing was interrupted.");
            }
        };
        assertFailedSavePreservesFile(storage);
    }

    @Test
    public void saveTAssist_atomicMoveUnsupported_preservesSavedFile() throws Exception {
        JsonTAssistStorage storage = new JsonTAssistStorage(testFolder.resolve("tassist.json")) {
            @Override
            void replaceDataFile(Path temporaryFile, Path target) throws IOException {
                throw new AtomicMoveNotSupportedException(temporaryFile.toString(), target.toString(),
                        "Atomic replacement is unavailable.");
            }
        };
        assertFailedSavePreservesFile(storage);
    }

    private void assertFailedSavePreservesFile(JsonTAssistStorage storage) throws Exception {
        Path filePath = storage.getTAssistFilePath();
        assertThrows(IOException.class, () -> storage.saveTAssist(TypicalGroups.getTypicalTAssist()));
        assertFalse(Files.exists(filePath), "A failed first save must not create the data file.");

        JsonTAssistStorage workingStorage = new JsonTAssistStorage(filePath);
        workingStorage.saveTAssist(TypicalGroups.getTypicalTAssist());
        String previousContents = Files.readString(filePath);
        assertThrows(IOException.class, () -> storage.saveTAssist(new TAssist()));
        assertEquals(previousContents, Files.readString(filePath));
        assertEquals(TypicalGroups.getTypicalTAssist(), workingStorage.readTAssist().orElseThrow());
        try (Stream<Path> files = Files.list(testFolder)) {
            assertEquals(List.of(filePath), files.toList(), "Temporary files must be removed.");
        }
    }

    @Test
    public void saveTAssist_emptyTAssist_readsBackEmpty() throws Exception {
        JsonTAssistStorage jsonTAssistStorage = new JsonTAssistStorage(testFolder.resolve("Empty.json"));
        jsonTAssistStorage.saveTAssist(new TAssist());
        assertEquals(new TAssist(), new TAssist(jsonTAssistStorage.readTAssist().orElseThrow()));
    }

    @Test
    public void saveTAssist_nullTAssist_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveTAssist(null, "SomeFile.json"));
    }

    @Test
    public void saveTAssist_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveTAssist(new TAssist(), null));
    }

    /**
     * Saves {@code tAssist} at the specified {@code filePath}.
     */
    private void saveTAssist(ReadOnlyTAssist tAssist, String filePath) {
        try {
            new JsonTAssistStorage(Paths.get("unused.json"))
                    .saveTAssist(tAssist, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }
}
