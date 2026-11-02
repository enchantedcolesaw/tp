package seedu.address.ui;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import seedu.address.model.group.Group;

/**
 * Shows every tutorial group as a chip in the top bar, with the active group highlighted.
 */
public class GroupStrip extends UiPart<ScrollPane> {
    static final String NO_GROUPS = "No tutorial groups yet";
    static final String DATA_NOT_READ = "Saved data could not be read";

    private static final String FXML = "GroupStrip.fxml";
    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private static final double CHIP_MAX_WIDTH = 200;

    private final ObservableList<Group> groups;
    private final ObservableValue<Optional<Group>> activeGroup;
    private final boolean isDataLoadFailed;

    @FXML
    private HBox groupChips;

    /**
     * Creates a strip that follows {@code groups} and highlights {@code activeGroup}. If {@code isDataLoadFailed},
     * an empty strip says the saved data could not be read instead of suggesting there are no groups yet.
     */
    public GroupStrip(ObservableList<Group> groups, ObservableValue<Optional<Group>> activeGroup,
            boolean isDataLoadFailed) {
        super(FXML);
        requireAllNonNull(groups, activeGroup);
        this.groups = groups;
        this.activeGroup = activeGroup;
        this.isDataLoadFailed = isDataLoadFailed;

        groups.addListener((ListChangeListener<Group>) change -> showGroups());
        activeGroup.addListener(observable -> showGroups());
        showGroups();
    }

    /**
     * Replaces the chips with one chip per group, or a note when there are no groups.
     */
    private void showGroups() {
        groupChips.getChildren().clear();
        if (groups.isEmpty()) {
            Label noGroups = new Label(isDataLoadFailed ? DATA_NOT_READ : NO_GROUPS);
            noGroups.getStyleClass().add("muted");
            groupChips.getChildren().add(noGroups);
            return;
        }
        for (Group group : groups) {
            Label chip = createChip(group);
            groupChips.getChildren().add(chip);
            if (isActive(group)) {
                Platform.runLater(() -> revealChip(chip));
            }
        }
    }

    /**
     * Reveals the active chip after the rebuilt strip has been laid out, moving only as far as necessary.
     */
    private void revealChip(Label chip) {
        if (!groupChips.getChildren().contains(chip)) {
            return; // A later rebuild has replaced this chip.
        }
        ScrollPane strip = getRoot();
        strip.applyCss();
        strip.layout();
        groupChips.layout();
        double viewportWidth = strip.getViewportBounds().getWidth();
        double overflow = groupChips.getLayoutBounds().getWidth() - viewportWidth;
        double scrollRange = strip.getHmax() - strip.getHmin();
        if (viewportWidth <= 0 || overflow <= 0 || scrollRange <= 0) {
            return;
        }
        double offset = (strip.getHvalue() - strip.getHmin()) / scrollRange * overflow;
        Bounds bounds = chip.getBoundsInParent();
        if (bounds.getMaxX() > offset + viewportWidth) {
            offset = bounds.getMaxX() - viewportWidth;
        } else if (bounds.getMinX() < offset) {
            offset = bounds.getMinX();
        } else {
            return;
        }
        strip.setHvalue(strip.getHmin() + Math.max(0, Math.min(offset / overflow, 1)) * scrollRange);
    }

    private Label createChip(Group group) {
        String name = group.getName().toString();
        Label chip = new Label(name);
        chip.getStyleClass().add("group-chip");
        chip.setMaxWidth(CHIP_MAX_WIDTH);
        // Long names that differ only at the end stay distinguishable.
        chip.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        chip.setTooltip(new Tooltip(name));
        chip.pseudoClassStateChanged(ACTIVE, isActive(group));
        return chip;
    }

    private boolean isActive(Group group) {
        return activeGroup.getValue().map(group::isSameGroup).orElse(false);
    }
}
