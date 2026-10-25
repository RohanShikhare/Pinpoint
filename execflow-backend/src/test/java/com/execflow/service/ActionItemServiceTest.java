package com.execflow.service;

import com.execflow.dto.response.ActionItemResponse;
import com.execflow.entity.ActionItem;
import com.execflow.entity.ActionStatus;
import com.execflow.entity.Priority;
import com.execflow.mapper.ActionItemMapper;
import com.execflow.repository.ActionItemRepository;
import com.execflow.repository.AnalysisRepository;
import com.execflow.repository.InputRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionItemServiceTest {

    @Mock
    private ActionItemRepository actionItemRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private InputRepository inputRepository;

    private final ActionItemMapper actionItemMapper = new ActionItemMapper();

    private ActionItemService actionItemService;

    private UUID userId;
    private ActionItem overduePending;
    private ActionItem overdueInProgress;
    private ActionItem futureDeadlinePending;
    private ActionItem todayDeadlinePending;
    private ActionItem noDeadlinePending;
    private ActionItem completedWithPastDeadline;

    @BeforeEach
    void setUp() {
        actionItemService = new ActionItemService(
                actionItemRepository,
                analysisRepository,
                inputRepository,
                actionItemMapper
        );

        userId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        overduePending = ActionItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Overdue Pending Item")
                .deadline(today.minusDays(2))
                .status(ActionStatus.PENDING)
                .priority(Priority.HIGH)
                .build();

        overdueInProgress = ActionItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Overdue In Progress Item")
                .deadline(today.minusDays(1))
                .status(ActionStatus.IN_PROGRESS)
                .priority(Priority.URGENT)
                .build();

        futureDeadlinePending = ActionItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Future Deadline Item")
                .deadline(today.plusDays(3))
                .status(ActionStatus.PENDING)
                .priority(Priority.MEDIUM)
                .build();

        todayDeadlinePending = ActionItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Due Today Item")
                .deadline(today)
                .status(ActionStatus.PENDING)
                .priority(Priority.MEDIUM)
                .build();

        noDeadlinePending = ActionItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("No Deadline Item")
                .deadline(null)
                .status(ActionStatus.PENDING)
                .priority(Priority.LOW)
                .build();

        completedWithPastDeadline = ActionItem.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .title("Completed Past Deadline Item")
                .deadline(today.minusDays(5))
                .status(ActionStatus.COMPLETED)
                .priority(Priority.HIGH)
                .build();
    }

    @Test
    @DisplayName("overdue=true returns only overdue items (deadline passed and not completed)")
    void list_withOverdueTrue_returnsOnlyOverdueItems() {
        when(actionItemRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(
                        overduePending,
                        overdueInProgress,
                        futureDeadlinePending,
                        todayDeadlinePending,
                        noDeadlinePending,
                        completedWithPastDeadline
                ));

        List<ActionItemResponse> result = actionItemService.list(userId, null, null, true);

        assertEquals(2, result.size());
        List<UUID> returnedIds = result.stream().map(ActionItemResponse::id).toList();
        assertTrue(returnedIds.contains(overduePending.getId()));
        assertTrue(returnedIds.contains(overdueInProgress.getId()));
    }

    @Test
    @DisplayName("overdue=false returns only non-overdue items")
    void list_withOverdueFalse_returnsOnlyNonOverdueItems() {
        when(actionItemRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(
                        overduePending,
                        overdueInProgress,
                        futureDeadlinePending,
                        todayDeadlinePending,
                        noDeadlinePending,
                        completedWithPastDeadline
                ));

        List<ActionItemResponse> result = actionItemService.list(userId, null, null, false);

        assertEquals(4, result.size());
        List<UUID> returnedIds = result.stream().map(ActionItemResponse::id).toList();
        assertTrue(returnedIds.contains(futureDeadlinePending.getId()));
        assertTrue(returnedIds.contains(todayDeadlinePending.getId()));
        assertTrue(returnedIds.contains(noDeadlinePending.getId()));
        assertTrue(returnedIds.contains(completedWithPastDeadline.getId()));
    }

    @Test
    @DisplayName("overdue=null applies no overdue filter and returns all items")
    void list_withOverdueNull_returnsAllItems() {
        when(actionItemRepository.findAllByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(
                        overduePending,
                        overdueInProgress,
                        futureDeadlinePending,
                        todayDeadlinePending,
                        noDeadlinePending,
                        completedWithPastDeadline
                ));

        List<ActionItemResponse> result = actionItemService.list(userId, null, null, null);

        assertEquals(6, result.size());
    }
}
