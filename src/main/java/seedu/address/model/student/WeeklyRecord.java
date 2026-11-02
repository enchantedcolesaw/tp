package seedu.address.model.student;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import seedu.address.commons.util.ToStringBuilder;

/** Stores one value per teaching week for a student. */
public final class WeeklyRecord<T> {
    public static final int MIN_WEEK = 1;
    public static final int MAX_WEEK = 13;
    public static final String MESSAGE_INVALID_WEEK = "Week must be a whole number from 1 to 13.";

    private final Map<Integer, T> records;

    /** Creates an empty weekly record. */
    public WeeklyRecord() {
        this(Map.of());
    }

    /** Creates a weekly record containing a defensive copy of {@code records}. */
    public WeeklyRecord(Map<Integer, T> records) {
        requireNonNull(records);
        Map<Integer, T> copiedRecords = new TreeMap<>();
        records.forEach((week, value) -> {
            checkArgument(week != null, "Week must not be null.");
            validateWeek(week);
            checkArgument(value != null, "Weekly record value must not be null.");
            copiedRecords.put(week, value);
        });
        this.records = Collections.unmodifiableMap(copiedRecords);
    }

    /** Returns the record for {@code week}, or an empty value if none has been entered. */
    public Optional<T> getRecord(int week) {
        validateWeek(week);
        return Optional.ofNullable(records.get(week));
    }

    /** Returns a new weekly record with {@code value} replacing any existing record for {@code week}. */
    public WeeklyRecord<T> withRecord(int week, T value) {
        validateWeek(week);
        requireNonNull(value);
        Map<Integer, T> updatedRecords = new TreeMap<>(records);
        updatedRecords.put(week, value);
        return new WeeklyRecord<>(updatedRecords);
    }

    /**
     * Returns a new weekly record without the record for {@code week}, so that the week is no longer recorded.
     * Returns an equal weekly record if {@code week} has no record.
     */
    public WeeklyRecord<T> withoutRecord(int week) {
        validateWeek(week);
        Map<Integer, T> updatedRecords = new TreeMap<>(records);
        updatedRecords.remove(week);
        return new WeeklyRecord<>(updatedRecords);
    }

    /** Returns an unmodifiable, week-ordered view of all records. */
    public Map<Integer, T> getRecords() {
        return records;
    }

    /** Checks that {@code week} is within the teaching term. */
    public static void validateWeek(int week) {
        checkArgument(week >= MIN_WEEK && week <= MAX_WEEK, MESSAGE_INVALID_WEEK);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeeklyRecord<?> otherWeeklyRecord)) {
            return false;
        }
        return records.equals(otherWeeklyRecord.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(records);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("records", records).toString();
    }
}
