package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;

public class TAssistArgumentParserTest {
    private static final Prefix PREFIX_NAME = new Prefix("n/");
    private static final Prefix PREFIX_ID = new Prefix("id/");
    private static final Prefix PREFIX_GROUP = new Prefix("g/");

    private final TAssistArgumentParser parser = new TAssistArgumentParser(
            List.of(PREFIX_NAME, PREFIX_ID), List.of(PREFIX_GROUP),
            List.of(PREFIX_ID));

    @Test
    public void parse_validParameters_trimsValuesAndAllowsAnyOrder() throws Exception {
        ArgumentMultimap values = parser.parse("  id/a123\t n/ Alice   Tan \n g/T01  ");
        assertEquals("a123", values.getValue(PREFIX_ID).orElseThrow());
        assertEquals("Alice   Tan", values.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("T01", values.getValue(PREFIX_GROUP).orElseThrow());
        assertEquals("", values.getPreamble());
    }

    @Test
    public void allowRepeated_repeatablePrefix_keepsEveryValueAndOthersStayUnique() throws Exception {
        TAssistArgumentParser repeatingParser = new TAssistArgumentParser(
                List.of(PREFIX_NAME, PREFIX_ID), List.of(PREFIX_GROUP), List.of(PREFIX_ID))
                .allowRepeated(PREFIX_ID);
        assertEquals(List.of("A1", "A2"), repeatingParser.parse("id/A1 n/Alice id/A2").getAllValues(PREFIX_ID));

        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME);
        assertThrows(ParseException.class, duplicateMessage, () -> repeatingParser.parse("id/A1 id/A2 n/A n/B"));
        String extraTextMessage = String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, "id/A2", "x");
        assertThrows(ParseException.class, extraTextMessage, () -> repeatingParser.parse("id/A1 id/A2 x n/Alice"));
    }

    @Test
    public void allowRepeated_undeclaredPrefix_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> parser.allowRepeated(new Prefix("x/")));
        assertThrows(NullPointerException.class, () -> parser.allowRepeated(null));
    }

    @Test
    public void parse_optionalParameterAbsent_success() throws Exception {
        assertTrue(parser.parse("n/Alice id/A1").getValue(PREFIX_GROUP).isEmpty());
    }

    @Test
    public void parse_blankRequiredValue_reportsMissingValue() {
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_BLANK_VALUE, "n/"), () ->
                parser.parse("n/ id/A1"));
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_BLANK_VALUE, "id/")
                + " Usage: cmd n/NAME id/ID", () -> parser.parse("n/Alice id/   ", "cmd n/NAME id/ID"));
    }

    @Test
    public void parse_slashesWithinValues_preservesValues() throws Exception {
        ArgumentMultimap values = parser.parse("n/PS1/PS2 https://example.com id/A1");
        assertEquals("PS1/PS2 https://example.com", values.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("/", parser.parse("n// id/A1").getValue(PREFIX_NAME).orElseThrow());
    }

    @Test
    public void parse_unknownOrUppercasePrefix_throwsSpecificError() {
        for (String prefix : new String[] {"x/", "N/", "unknown/", "1/", "bad-prefix/"}) {
            String message = String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, prefix, "n/ id/ g/");
            assertThrows(ParseException.class, message, () -> parser.parse("n/Alice " + prefix + "value id/A1"));
        }
    }

    @Test
    public void parse_missingParameters_namesMissingPrefixes() {
        assertMissingParameters("", "n/ id/");
        assertMissingParameters("g/T01", "n/ id/");
        assertMissingParameters("id/A1", "n/");
        assertMissingParameters("n/Alice", "id/");
        assertMissingParameters("n/Alice g/T01-id", "id/");
    }

    @Test
    public void parse_missingSpaceBeforePrefix_namesJoinedPrefix() {
        assertMissingSpace("n/Aliceid/A1", "id/", "n/Aliceid/A1");
        assertMissingSpace("id/A1n/Alice g/T01", "n/", "id/A1n/Alice");
        assertMissingSpace("n/Alice g/T01,id/A1", "id/", "g/T01,id/A1");
        assertMissingSpace("id/A1n/Alice Tan Wei Ming", "n/", "id/A1n/Alice");
    }

    @Test
    public void parse_unknownPrefixEndingWithMissingPrefix_hintsMissingSpace() {
        String unknownPrefix = String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "Tanid/", "n/ id/ g/");
        assertThrows(ParseException.class, unknownPrefix
                + String.format(TAssistArgumentParser.MESSAGE_SEPARATE_PREFIX, "id/"), () ->
                parser.parse("n/Alice Tanid/A1"));
        assertThrows(ParseException.class, unknownPrefix, () -> parser.parse("n/Alice Tanid/A1 id/A2"));
    }

    @Test
    public void parse_mistypedPrefixEndingWithAllowedPrefix_omitsHint() {
        String[][] cases = {
            {"n/Alice studentid/A1", "studentid/"},
            {"n/alice tanid/A1", "tanid/"},
            {"Tanid/A1 n/Alice", "Tanid/"}
        };
        for (String[] testCase : cases) {
            assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX,
                    testCase[1], "n/ id/ g/"), () -> parser.parse(testCase[0]));
        }
    }

    @Test
    public void parse_repeatedRequiredOrOptionalParameter_throwsSpecificError() {
        for (Prefix prefix : List.of(PREFIX_NAME, PREFIX_ID, PREFIX_GROUP)) {
            String input = "n/Alice id/A1 g/T01 " + prefix + "Other";
            assertThrows(ParseException.class, Messages.getErrorMessageForDuplicatePrefixes(prefix), () ->
                    parser.parse(input));
        }
    }

    @Test
    public void parse_unexpectedPreamble_throwsSpecificError() {
        String message = String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "extra", "n/ id/ g/");
        assertThrows(ParseException.class, message, () -> parser.parse("extra n/Alice id/A1"));
    }

    @Test
    public void parse_extraTextAfterSingleWordValue_namesParameterAndExtraText() {
        assertExtraText("n/Alice id/A1 now", "id/A1", "now");
        assertExtraText("n/Alice id/A1   now  please ", "id/A1", "now  please");
        assertExtraText("id/A1 now n/Alice", "id/A1", "now");
    }

    @Test
    public void parse_extraTextAndMissingParameter_reportsMissingParameterFirst() {
        String message = String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS, "n/");
        assertThrows(ParseException.class, message, () -> parser.parse("id/A1 now"));
    }

    @Test
    public void parse_withUsage_appendsFormatOnlyToMissingParameterError() throws Exception {
        String usage = "student add n/NAME id/STUDENT_ID";
        String missingMessage = "Missing required parameter(s): id/.";
        assertThrows(ParseException.class, missingMessage + " Usage: " + usage, () ->
                parser.parse("n/Alice", usage));
        assertThrows(ParseException.class, missingMessage, () -> parser.parse("n/Alice"));
        assertThrows(ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, "id/", "n/Aliceid/A1"), () ->
                parser.parse("n/Aliceid/A1", usage));
        assertThrows(ParseException.class,
                String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "x/", "n/ id/ g/"), () ->
                parser.parse("x/Alice", usage));
        assertThrows(ParseException.class, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME), () ->
                parser.parse("n/Alice n/Ben", usage));
        assertEquals("Alice", parser.parse("n/Alice id/A1", usage).getValue(PREFIX_NAME).orElseThrow());
    }

    @Test
    public void parse_spacesInFreeTextValue_preservesValue() throws Exception {
        assertEquals("Alice Tan", parser.parse("n/Alice Tan id/A1").getValue(PREFIX_NAME).orElseThrow());
    }

    @Test
    public void parse_noParametersAllowed_rejectsAllExtraInput() throws Exception {
        TAssistArgumentParser noParametersParser = new TAssistArgumentParser(List.of(), List.of());
        assertEquals("", noParametersParser.parse(" \t ").getPreamble());
        for (String input : new String[] {"extra", "n/Alice"}) {
            assertThrows(ParseException.class, TAssistArgumentParser.MESSAGE_NO_PARAMETERS, () ->
                    noParametersParser.parse(input));
        }
    }

    @Test
    public void parse_literalWords_keptInValueIgnoringCase() throws Exception {
        TAssistArgumentParser literalParser = new TAssistArgumentParser(
                List.of(PREFIX_NAME, PREFIX_ID), List.of(), List.of(), List.of("s/o", "D/O"));

        ArgumentMultimap values = literalParser.parse("n/Ravi s/o Kumar id/A1");
        assertEquals("Ravi s/o Kumar", values.getValue(PREFIX_NAME).orElseThrow());
        assertEquals("A1", values.getValue(PREFIX_ID).orElseThrow());
        assertEquals("SITI D/O AHMAD s/o X", literalParser.parse("id/A1 n/SITI D/O AHMAD s/o X")
                .getValue(PREFIX_NAME).orElseThrow());
        assertEquals("Ravi\ts/o\tKumar", literalParser.parse("n/Ravi\ts/o\tKumar id/A1")
                .getValue(PREFIX_NAME).orElseThrow());
    }

    @Test
    public void parse_literalWordNotStandingAlone_throwsUnknownPrefix() {
        TAssistArgumentParser literalParser = new TAssistArgumentParser(
                List.of(PREFIX_NAME, PREFIX_ID), List.of(), List.of(), List.of("s/o"));
        for (String input : new String[] {"n/Ravi s/ o Kumar id/A1", "n/Ravi x/o Kumar id/A1"}) {
            String prefix = input.contains("x/") ? "x/" : "s/";
            assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, prefix,
                    "n/ id/"), () -> literalParser.parse(input));
        }
        // A literal word joined to more text gets a hint to separate it, naming it as typed.
        String[][] joined = {{"n/Ravi s/oKumar id/A1", "s/", "s/o"}, {"n/Ravi S/O/ Kumar id/A1", "S/", "S/O"}};
        for (String[] testCase : joined) {
            assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, testCase[1],
                    "n/ id/") + String.format(TAssistArgumentParser.MESSAGE_SEPARATE_LITERAL_WORD, testCase[2]), () ->
                    literalParser.parse(testCase[0]));
        }
        // A joined literal word in any case is hinted, with the text as typed.
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "S/",
                "n/ id/") + String.format(TAssistArgumentParser.MESSAGE_SEPARATE_LITERAL_WORD, "S/O"), () ->
                literalParser.parse("n/Ravi S/OKumar id/A1"));
        // Without declared literal words, s/o is still an unknown prefix.
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, "s/",
                "n/ id/ g/"), () -> parser.parse("n/Ravi s/o Kumar id/A1"));
    }

    @Test
    public void parse_prefixOnlyStartsWithLiteralWord_noSeparateHint() {
        TAssistArgumentParser literalParser = new TAssistArgumentParser(
                List.of(PREFIX_NAME, PREFIX_ID), List.of(), List.of(), List.of("a/l", "a/p", "d/o"));
        for (String input : new String[] {"n/Ahmad a/lee id/A1", "n/Ahmad a/present id/A1", "n/Ahmad d/one id/A1",
            "n/Ahmad A/Lee id/A1"}) {
            String prefix = input.contains("A/") ? "A/" : input.substring(input.indexOf(" ") + 1).substring(0, 2);
            assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNKNOWN_PREFIX, prefix,
                    "n/ id/"), () -> literalParser.parse(input));
        }
    }

    @Test
    public void parse_literalWordBeforeFirstPrefix_throwsUnexpectedText() {
        TAssistArgumentParser literalParser = new TAssistArgumentParser(
                List.of(PREFIX_NAME), List.of(), List.of(), List.of("s/o"));
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_UNEXPECTED_TEXT, "s/o",
                "n/"), () -> literalParser.parse("s/o n/Kumar"));
    }

    @Test
    public void constructor_invalidLiteralWords_throwsIllegalArgumentException() {
        for (String literalWord : new String[] {"", "so", "s/", "/o", "s/o/x", "s o/x", "n/o", "N/o"}) {
            assertThrows(IllegalArgumentException.class, () ->
                    new TAssistArgumentParser(List.of(PREFIX_NAME), List.of(), List.of(), List.of(literalWord)));
        }
    }

    @Test
    public void constructor_invalidOrRepeatedPrefixes_throwsIllegalArgumentException() {
        for (String prefix : new String[] {"", "N/", "n", "n /", "1/"}) {
            assertThrows(IllegalArgumentException.class, () ->
                    new TAssistArgumentParser(List.of(new Prefix(prefix)), List.of()));
        }
        assertThrows(IllegalArgumentException.class, () ->
                new TAssistArgumentParser(List.of(PREFIX_NAME, PREFIX_NAME), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new TAssistArgumentParser(List.of(PREFIX_NAME), List.of(PREFIX_NAME)));
        assertThrows(IllegalArgumentException.class, () ->
                new TAssistArgumentParser(List.of(), List.of(PREFIX_NAME, PREFIX_NAME)));
        assertThrows(IllegalArgumentException.class, () ->
                new TAssistArgumentParser(List.of(PREFIX_NAME), List.of(), List.of(PREFIX_ID)));
    }

    @Test
    public void constructor_sourceModified_keepsDeclaredPrefixes() throws Exception {
        List<Prefix> requiredPrefixes = new ArrayList<>(List.of(PREFIX_NAME));
        TAssistArgumentParser copiedParser = new TAssistArgumentParser(requiredPrefixes, List.of());
        requiredPrefixes.clear();
        assertEquals("Alice", copiedParser.parse("n/Alice").getValue(PREFIX_NAME).orElseThrow());
        assertThrows(ParseException.class, () -> copiedParser.parse(""));
    }

    @Test
    public void nullInputs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TAssistArgumentParser(null, List.of()));
        assertThrows(NullPointerException.class, () -> new TAssistArgumentParser(List.of(), null));
        assertThrows(NullPointerException.class, () -> new TAssistArgumentParser(List.of(), List.of(), null));
        assertThrows(NullPointerException.class, () ->
                new TAssistArgumentParser(List.of(), List.of(), List.of(), null));
        assertThrows(NullPointerException.class, () -> parser.parse(null));
        assertThrows(NullPointerException.class, () -> parser.parse("n/Alice", null));
        assertThrows(NullPointerException.class, () -> parser.parse(null, "student add n/NAME id/STUDENT_ID"));
    }

    private void assertMissingParameters(String input, String missingPrefixes) {
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_MISSING_PARAMETERS,
                missingPrefixes), () -> parser.parse(input));
    }

    private void assertMissingSpace(String input, String missingPrefix, String joinedText) {
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_MISSING_SPACE, missingPrefix,
                joinedText), () -> parser.parse(input));
    }

    private void assertExtraText(String input, String precedingText, String extraText) {
        assertThrows(ParseException.class, String.format(TAssistArgumentParser.MESSAGE_EXTRA_TEXT, precedingText,
                extraText), () -> parser.parse(input));
    }
}
