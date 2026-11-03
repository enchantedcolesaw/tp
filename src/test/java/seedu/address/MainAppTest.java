package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import javafx.stage.Stage;
import seedu.address.commons.core.DataLoadStatus;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpEntry;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.TAssist;
import seedu.address.model.UserPrefs;
import seedu.address.model.group.GroupName;
import seedu.address.model.student.AttendanceStatus;
import seedu.address.model.student.StudentName;
import seedu.address.storage.JsonTAssistStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.TAssistBuilder;
import seedu.address.testutil.TypicalGroups;
import seedu.address.ui.Ui;

public class MainAppTest {

    @TempDir
    public Path temporaryFolder;

    private final MainApp mainApp = new MainApp();

    @Test
    public void init_groupCommandsAndHelp_areRegistered() throws Exception {
        MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"),
                temporaryFolder.resolve("tassist.json"));
        app.init();

        assertEquals("No tutorial groups exist. Create one using: group add g/GROUP_NAME",
                app.logic.execute("group list").getFeedbackToUser());
        assertEquals("Created tutorial group: T01.\nActive group is now T01.",
                app.logic.execute("group add g/T01").getFeedbackToUser());
        app.logic.execute("group add g/T02");
        assertEquals("Switched active group to T01.", app.logic.execute("group use g/t01").getFeedbackToUser());
        assertEquals("1. T01\n2. T02\nActive group: T01", app.logic.execute("group list").getFeedbackToUser());
        assertEquals(CommandResult.forHelp("Showing group commands.", "group"), app.logic.execute("help group"));
        assertEquals(List.of("group add g/GROUP_NAME", "group list",
                "group use g/GROUP_NAME", "group export", "group rename g/GROUP_NAME n/NEW_NAME",
                "group delete g/GROUP_NAME [f/yes]"),
                HelpCatalog.filterByTopic(app.logic.getHelpEntries(), "group").stream()
                        .map(HelpEntry::getCommandFormat).toList());
    }

    @Test
    public void init_afterGroupRenameAndDelete_savesChanges() throws Exception {
        Path dataFile = temporaryFolder.resolve("tassist.json");
        MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"), dataFile);
        app.init();
        app.logic.execute("group add g/T 01");
        app.logic.execute("group add g/T02");

        assertEquals("Renamed tutorial group T 01 to T01.", app.logic.execute("group rename g/T 01 n/T01")
                .getFeedbackToUser());
        assertEquals("T01", new JsonTAssistStorage(dataFile).readTAssist().orElseThrow()
                .getGroupList().get(0).getName().toString());

        app.logic.execute("group delete g/T02");
        assertEquals(1, new JsonTAssistStorage(dataFile).readTAssist().orElseThrow().getGroupList().size());
        assertEquals(Optional.empty(), new JsonTAssistStorage(dataFile).readTAssist().orElseThrow()
                .getActiveGroup());
    }

    @Test
    public void init_afterGroupCommandsAndRestart_restoresGroupsAndActiveSelection() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Path dataFile = temporaryFolder.resolve("tassist.json");
        MainApp app = new MainApp(prefsFile, dataFile);
        app.init();
        app.logic.execute("group add g/ Tutorial   1-Thu ");
        app.logic.execute("group add g/T02");
        assertEquals("T02", new JsonTAssistStorage(dataFile).readTAssist().orElseThrow()
                .getActiveGroup().orElseThrow().getName().toString());
        app.logic.execute("group use g/tutorial 1-thu");
        TAssist expected = new TAssist(app.model.getTAssist());
        app.stop();

        MainApp reopened = new MainApp(prefsFile, dataFile);
        reopened.init();

        assertEquals(expected, reopened.model.getTAssist());
        assertEquals("1. Tutorial 1-Thu\n2. T02\nActive group: Tutorial 1-Thu",
                reopened.logic.execute("group list").getFeedbackToUser());
        assertEquals("Tutorial 1-Thu", reopened.logic.activeGroupProperty().get().orElseThrow().getName().toString());
    }

    @Test
    public void init_corruptData_commandsAndRestartKeepFileUntouched() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        String weeklyRecordsData = """
                {"activeGroup":"T01","groups":[{"name":"T01","students":[{
                  "name":"Alice Tan","studentId":"A1",%s}]}]}
                """;
        String duplicateParticipation = weeklyRecordsData.formatted("\"participation\":{\"1\":3,\"01\":5}");
        String duplicateAttendance = weeklyRecordsData
                .formatted("\"attendance\":{\"1\":\"PRESENT\",\"01\":\"ABSENT\"}");
        new JsonTAssistStorage(dataFile()).saveTAssist(TypicalGroups.getTypicalTAssist());
        String validData = Files.readString(dataFile());
        for (String invalidData : List.of("{", "null", duplicateParticipation, duplicateAttendance,
                validData.replace("\"groups\"", "\"Groups\""),
                "{\"groups\":[],\"groups\":[]}",
                weeklyRecordsData.formatted("\"attendance\":{\"3\":\"PRESENT\",\"3\":\"ABSENT\"}"),
                validData + "\nTHIS IS INVALID JSON",
                validData + "\n" + validData, validData + "\nnull", validData + "\n[]")) {
            Files.writeString(dataFile(), invalidData);
            MainApp app = new MainApp(prefsFile, dataFile());
            app.init();

            assertTrue(app.getDataLoadStatus().isFailure());
            for (String command : List.of("help", "view storage")) {
                assertFalse(app.logic.execute(command).hasSavedChanges());
            }
            for (String command : List.of("group list", "student list", "group add g/T01")) {
                assertThrows(CommandException.class,
                        LogicManager.getDataProtectedMessage(app.getDataLoadStatus()), () ->
                        app.logic.execute(command));
            }
            assertEquals(new TAssist(), app.model.getTAssist());
            app.logic.execute("exit");
            app.stop();
            assertEquals(invalidData, Files.readString(dataFile()));

            MainApp reopened = new MainApp(prefsFile, dataFile());
            reopened.init();
            assertTrue(reopened.getDataLoadStatus().isFailure());
            assertEquals(invalidData, Files.readString(dataFile()));
        }
    }

    @Test
    public void init_invalidStudentValues_namesGroupAndStudentAndProtectsFile() throws Exception {
        String[][] cases = {
            {"Ben_Lim", "\"attendance\":{}", StudentName.MESSAGE_CONSTRAINTS},
            {"Ben Lim", "\"attendance\":{\"14\":\"PRESENT\"}", "Week must be a whole number from 1 to 13."},
            {"Ben Lim", "\"attendance\":{\"3\":\"UNKNOWN\"}", AttendanceStatus.MESSAGE_CONSTRAINTS},
            {"Ben Lim", "\"participation\":{\"14\":3}", "Week must be a whole number from 1 to 13."},
            {"Ben Lim", "\"participation\":{\"3\":7}", "Participation score must be a whole number from 0 to 5."}
        };
        for (String[] testCase : cases) {
            String invalidData = """
                    {"activeGroup":"T01","groups":[
                      {"name":"T01","students":[{"name":"Alice Tan","studentId":"A1"}]},
                      {"name":"T02","students":[{"name":"Dan Koh","studentId":"B1"},
                        {"name":"%s","studentId":"B2",%s}]}]}
                    """.formatted(testCase[0], testCase[1]);
            Files.writeString(dataFile(), invalidData);
            MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"), dataFile());
            app.init();
            assertEquals(DataLoadStatus.failed("Group T02, student B2: " + testCase[2]), app.getDataLoadStatus());
            assertEquals(new TAssist(), app.model.getTAssist());
            assertThrows(CommandException.class, () -> app.logic.execute("group add g/T03"));
            app.stop();
            assertEquals(invalidData, Files.readString(dataFile()));
        }
    }

    @Test
    public void init_distinctWeekAliases_loadsRecordsAndAllowsSaving() throws Exception {
        Files.writeString(dataFile(), """
                {"activeGroup":"T01","groups":[{"name":"T01","students":[{
                  "name":"Alice Tan","studentId":"A1",
                  "attendance":{"01":"PRESENT","13":"ABSENT"},
                  "participation":{"01":3,"02":5}}]}]}
                """);
        MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"), dataFile());
        app.init();

        assertEquals(DataLoadStatus.LOADED, app.getDataLoadStatus());
        assertEquals("Participation for T01, Week 1: 1 recorded, 0 not recorded\nAlice Tan | A1 | 3",
                app.logic.execute("participation list w/1").getFeedbackToUser());
        assertEquals("Participation for T01, Week 2: 1 recorded, 0 not recorded\nAlice Tan | A1 | 5",
                app.logic.execute("participation list w/2").getFeedbackToUser());
        assertEquals("Attendance for T01, Week 1: 1 present, 0 absent, "
                + "0 late, 0 excused, 0 not recorded\nAlice Tan | A1 | Present",
                app.logic.execute("attendance list w/1").getFeedbackToUser());
        assertTrue(app.logic.execute("group add g/T02").hasSavedChanges());
        app.stop();
        assertEquals(app.model.getTAssist(), new JsonTAssistStorage(dataFile()).readTAssist().orElseThrow());
    }

    @Test
    public void init_trailingWhitespace_loadsDataAndAllowsSaving() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        MainApp app = new MainApp(prefsFile, dataFile());
        app.init();
        app.logic.execute("group add g/T01");
        TAssist saved = new TAssist(app.model.getTAssist());
        app.stop();
        Files.writeString(dataFile(), Files.readString(dataFile()) + " \t\r\n");

        MainApp reopened = new MainApp(prefsFile, dataFile());
        reopened.init();

        assertEquals(DataLoadStatus.LOADED, reopened.getDataLoadStatus());
        assertEquals(saved, reopened.model.getTAssist());
        assertTrue(reopened.logic.execute("group add g/T02").hasSavedChanges());
        reopened.stop();
        assertEquals(reopened.model.getTAssist(), new JsonTAssistStorage(dataFile()).readTAssist().orElseThrow());
    }

    @Test
    public void init_unreadableDataPath_protectsExistingContents() throws Exception {
        Path directory = Files.createDirectory(dataFile());
        Path existingFile = directory.resolve("keep.txt");
        Files.writeString(existingFile, "Keep existing contents.");
        MainApp app = new MainApp(temporaryFolder.resolve("preferences.json"), directory);
        app.init();

        assertEquals(DataLoadStatus.failed("The file could not be opened."), app.getDataLoadStatus());
        assertThrows(CommandException.class,
                LogicManager.getDataProtectedMessage(app.getDataLoadStatus()), () ->
                app.logic.execute("group add g/T01"));
        app.stop();
        assertEquals("Keep existing contents.", Files.readString(existingFile));
        assertTrue(Files.isDirectory(directory));
    }

    @Test
    public void constructor_nullPath_throwsNullPointerException() {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Path dataFile = temporaryFolder.resolve("tassist.json");
        assertThrows(NullPointerException.class, () -> new MainApp(null, dataFile));
        assertThrows(NullPointerException.class, () -> new MainApp(prefsFile, null));
    }

    @Test
    public void init_missingDataFile_startsEmptyAndSavesPrefs() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        MainApp app = new MainApp(prefsFile, temporaryFolder.resolve("tassist.json"));

        app.init();

        assertEquals(new TAssist(), app.model.getTAssist());
        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(Optional.of(new UserPrefs()), new JsonUserPrefsStorage(prefsFile).readUserPrefs());
        assertFalse(Files.exists(temporaryFolder.resolve("tassist.json")));
        assertEquals(DataLoadStatus.MISSING, app.getDataLoadStatus());
    }

    @Test
    public void init_nullGuiSettings_usesAndSavesDefaultsAcrossRestart() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Files.writeString(prefsFile, "{\"guiSettings\":null}");
        MainApp app = new MainApp(prefsFile, dataFile());
        app.init();

        assertEquals(new UserPrefs(), app.model.getUserPrefs());
        assertEquals(Optional.of(new UserPrefs()), new JsonUserPrefsStorage(prefsFile).readUserPrefs());
        app.logic.execute("group add g/T01");
        app.stop();

        MainApp reopened = new MainApp(prefsFile, dataFile());
        reopened.init();
        assertEquals(new UserPrefs(), reopened.model.getUserPrefs());
        assertTrue(reopened.model.hasGroup(TypicalGroups.NAME_T01));
        reopened.stop();
    }

    @Test
    public void init_savedFiles_restoresDataAndPrefs() throws Exception {
        Path prefsFile = temporaryFolder.resolve("preferences.json");
        Path dataFile = temporaryFolder.resolve("tassist.json");
        TAssist saved = new TAssistBuilder(TypicalGroups.getTypicalTAssist())
                .withActiveGroup(TypicalGroups.NAME_T02).build();
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(800, 600, 1, 2));
        StorageManager storage = new StorageManager(new JsonTAssistStorage(dataFile),
                new JsonUserPrefsStorage(prefsFile));
        storage.saveTAssist(saved);
        storage.saveUserPrefs(userPrefs);

        MainApp app = new MainApp(prefsFile, dataFile);
        app.init();

        assertEquals(saved, app.model.getTAssist());
        assertEquals(DataLoadStatus.LOADED, app.getDataLoadStatus());
        assertEquals(userPrefs, app.model.getUserPrefs());
        assertEquals(TypicalGroups.getT02().getStudentList(), app.model.getActiveGroupStudentList());
    }

    @Test
    public void start_delegatesToUi() {
        RecordingUi recordingUi = new RecordingUi();
        mainApp.ui = recordingUi;

        mainApp.start(null);

        assertTrue(recordingUi.isStarted());
        assertNull(recordingUi.getStartedStage());
    }

    @Test
    public void stop_savesUserPrefs() throws Exception {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(800, 600, 1, 2));
        mainApp.model = new ModelManager(new TAssist(), userPrefs);
        mainApp.storage = createStorage();

        mainApp.stop();

        assertEquals(Optional.of(userPrefs), mainApp.storage.readUserPrefs());
    }

    @Test
    public void initModelManager_missingDataFile_startsEmpty() {
        Model model = mainApp.initModelManager(createStorage(), new UserPrefs());
        assertEquals(new TAssist(), model.getTAssist());
    }

    @Test
    public void initModelManager_validDataFile_restoresData() throws Exception {
        TAssist saved = new TAssistBuilder(TypicalGroups.getTypicalTAssist())
                .withActiveGroup(TypicalGroups.NAME_T02).build();
        StorageManager storage = createStorage();
        storage.saveTAssist(saved);

        Model model = mainApp.initModelManager(storage, new UserPrefs());

        assertEquals(saved, model.getTAssist());
        assertEquals(TypicalGroups.getT02().getStudentList(), model.getActiveGroupStudentList());
    }

    @Test
    public void initModelManager_invalidDataFile_startsEmptyAndKeepsFile() throws Exception {
        String missingBrackets = "{ \"groups\" : [ { \"name\" : \"T01\" }";
        for (String invalidData : List.of(missingBrackets, "null")) {
            Files.writeString(dataFile(), invalidData);

            Model model = mainApp.initModelManager(createStorage(), new UserPrefs());

            assertEquals(new TAssist(), model.getTAssist(), invalidData);
            assertEquals(invalidData, Files.readString(dataFile()));
            assertTrue(mainApp.getDataLoadStatus().isFailure());
        }
    }

    @Test
    public void initModelManager_invalidDataFile_statusNamesReason() throws Exception {
        String duplicateIds = "{ \"groups\" : [ { \"name\" : \"T01\", \"students\" : ["
                + " { \"name\" : \"Alice Tan\", \"studentId\" : \"A1\" },"
                + " { \"name\" : \"Ben Lim\", \"studentId\" : \"A1\" } ] } ] }";
        assertLoadFailureReason(duplicateIds, "Group T01 has more than one student with ID A1.");
        assertLoadFailureReason("{\n  \"groups\" : [ {",
                "The file has a JSON syntax error or a value of the wrong type near line 2.");
        assertLoadFailureReason("{ \"groups\" : \"T01\" }",
                "The file has a JSON syntax error or a value of the wrong type near line 1.");
        assertLoadFailureReason("{ \"groups\" : [] }\n\nextra",
                "The file has a JSON syntax error or a value of the wrong type near line 3.");
        assertLoadFailureReason("null", JsonUtil.MESSAGE_NULL_CONTENT);
    }

    @Test
    public void initModelManager_duplicateWeekAlias_showsSpecificReason() throws Exception {
        String data = """
                {"groups":[{"name":"T01","students":[{"name":"Alice Tan","studentId":"A1",%s}]}]}
                """;
        String reason = "Group T01, student A1: Duplicate weekly record for Week 1. Use only one entry per week.";
        assertLoadFailureReason(data.formatted("\"participation\":{\"1\":3,\"01\":5}"), reason);
        assertLoadFailureReason(data.formatted("\"attendance\":{\"1\":\"PRESENT\",\"01\":\"ABSENT\"}"), reason);
        assertLoadFailureReason(data.formatted("\"attendance\":{\"x\":\"PRESENT\"}"),
                "Group T01, student A1: Week must be a whole number from 1 to 13.");
    }

    @Test
    public void initModelManager_unknownOrRepeatedFields_showsFieldAndLine() throws Exception {
        assertLoadFailureReason("{\n  \"Groups\": []}",
                "The file has an unknown field \"Groups\" near line 2.");
        assertLoadFailureReason("{\n  \"groups\": [],\n  \"groups\": []}",
                "The file has a repeated field 'groups' near line 3.");
        assertLoadFailureReason("""
                {"groups":[{"name":"T01","students":[{"name":"Alice","studentId":"A1",
                "attendance":{"3":"PRESENT","3":"ABSENT"}}]}]}
                """, "The file has a repeated field '3' near line 2.");
    }

    @Test
    public void describeLoadFailure_noJsonLocation_omitsLine() {
        String reason = "The file has a JSON syntax error or a value of the wrong type.";
        assertEquals(reason, MainApp.describeLoadFailure(
                new DataLoadingException(new JsonParseException((JsonParser) null, "Unexpected end."))));
        assertEquals(reason, MainApp.describeLoadFailure(
                new DataLoadingException(new JsonParseException(null, "Unexpected end.", JsonLocation.NA))));
    }

    private void assertLoadFailureReason(String invalidData, String reason) throws Exception {
        Files.writeString(dataFile(), invalidData);
        mainApp.initModelManager(createStorage(), new UserPrefs());
        assertEquals(DataLoadStatus.failed(reason).getMessage(), mainApp.getDataLoadStatus().getMessage());
        assertEquals(DataLoadStatus.MESSAGE_FAILED_PREFIX + " " + reason + " "
                + DataLoadStatus.MESSAGE_FILE_UNCHANGED, mainApp.getDataLoadStatus().getMessage());
    }

    @Test
    public void initModelManager_unknownActiveGroup_loadsDataWithNoActiveGroup() throws Exception {
        Files.writeString(dataFile(), "{ \"activeGroup\" : \"T09\", \"groups\" : [ { \"name\" : \"T01\" } ] }");

        Model model = mainApp.initModelManager(createStorage(), new UserPrefs());

        assertTrue(model.hasGroup(new GroupName("T01")));
        assertEquals(Optional.empty(), model.getActiveGroup());
    }

    private Path dataFile() {
        return temporaryFolder.resolve("tassist.json");
    }

    private StorageManager createStorage() {
        return new StorageManager(new JsonTAssistStorage(dataFile()),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
    }

    /**
     * Records the stage passed to {@link Ui#start(Stage)}.
     */
    private static class RecordingUi implements Ui {
        private boolean hasStarted;
        private Stage startedStage;

        @Override
        public void start(Stage primaryStage) {
            hasStarted = true;
            startedStage = primaryStage;
        }

        /**
         * Returns true if {@link #start(Stage)} has been called.
         */
        public boolean isStarted() {
            return hasStarted;
        }

        /**
         * Returns the stage passed to {@link #start(Stage)}.
         */
        public Stage getStartedStage() {
            return startedStage;
        }
    }
}
