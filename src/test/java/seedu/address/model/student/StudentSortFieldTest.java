package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.StudentBuilder;

public class StudentSortFieldTest {
    @Test
    public void nameOrder_accentsAndCase_sortsAlphabeticallyThenById() {
        Student zoe = new StudentBuilder().withName("Zoe").withStudentId("A1").build();
        Student emile = new StudentBuilder().withName("Émile").withStudentId("A2").build();
        Student upperBen = new StudentBuilder().withName("BEN LIM").withStudentId("A3").build();
        Student lowerBen = new StudentBuilder().withName("ben lim").withStudentId("A4").build();
        List<Student> students = new ArrayList<>(List.of(zoe, lowerBen, emile, upperBen));

        students.sort(StudentSortField.NAME.getComparator());

        assertEquals(List.of(upperBen, lowerBen, emile, zoe), students);
    }

    @Test
    public void nameOrder_sharedFirstWord_keepsWholeWordsTogether() {
        Student tanAh = new StudentBuilder().withName("Tan Ah Kow").withStudentId("A1").build();
        Student tanaka = new StudentBuilder().withName("Tanaka Yuki").withStudentId("A2").build();
        Student tanBee = new StudentBuilder().withName("Tan Bee Lin").withStudentId("A3").build();
        Student tan = new StudentBuilder().withName("Tan").withStudentId("A4").build();
        List<Student> students = new ArrayList<>(List.of(tanaka, tanBee, tanAh, tan));

        students.sort(StudentSortField.NAME.getComparator());

        assertEquals(List.of(tan, tanAh, tanBee, tanaka), students);
    }
}
