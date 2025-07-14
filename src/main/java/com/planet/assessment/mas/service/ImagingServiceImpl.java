package com.planet.assessment.mas.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planet.assessment.mas.dto.ImagingActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ImagingServiceImpl implements ImagingService {

    private static final Logger logger = LoggerFactory.getLogger(ImagingServiceImpl.class);

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Autowired
    public ImagingServiceImpl(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ImagingActivity> processChronologicalWindowFromFile() throws IOException {
        List<ImagingActivity> activities = loadActivitiesFromFile();
        return createChronologicalWindow(activities);
    }

    @Override
    public List<List<ImagingActivity>> processSplitWindowsByStateFromFile(String activityState) throws IOException {
        List<ImagingActivity> activities = loadActivitiesFromFile();
        List<List<ImagingActivity>> allWindows = splitWindowsByActivityState(activities);

        if (activityState == null || activityState.trim().isEmpty()) {
            return allWindows;
        }

        return allWindows.stream()
                .filter(window -> !window.isEmpty() &&
                        activityState.trim().equalsIgnoreCase(
                                window.get(0).getActivityState() != null ?
                                        window.get(0).getActivityState().trim() : ""))
                .collect(Collectors.toList());
    }

    private List<ImagingActivity> loadActivitiesFromFile() throws IOException {
        try {
            Resource resource = resourceLoader.getResource("classpath:/imaging_activities.json");
            if (!resource.exists()) {
                logger.warn("Imaging activities file not found, returning empty list");
                return new ArrayList<>();
            }

            try (InputStream inputStream = resource.getInputStream()) {
                List<ImagingActivity> activities = objectMapper.readValue(inputStream, new TypeReference<>() {});
                return activities != null ? activities : new ArrayList<>();
            }
        } catch (IOException e) {
            logger.error("Error loading imaging activities from file", e);
            throw e;
        }
    }

    private List<ImagingActivity> createChronologicalWindow(List<ImagingActivity> activities) {
        if (activities == null || activities.isEmpty()) {
            return new ArrayList<>();
        }

        List<ImagingActivity> validActivities = new ArrayList<>();
        int invalidCount = 0;

        for (ImagingActivity activity : activities) {
            if (activity == null) {
                invalidCount++;
                continue;
            }

            String startTime = activity.getStartTime();
            if (startTime == null || startTime.trim().isEmpty()) {
                invalidCount++;
                continue;
            }

            try {
                Instant.parse(startTime.trim());
                validActivities.add(activity);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid start time format for activity: {}", startTime, e);
                invalidCount++;
            }
        }

        if (invalidCount > 0) {
            logger.info("Filtered out {} invalid activities (null or invalid start_time)", invalidCount);
        }

        return validActivities.stream()
                .sorted(Comparator
                        .comparing((ImagingActivity activity) -> Instant.parse(activity.getStartTime().trim()))
                        .thenComparing(activity -> activity.getSatelliteHwId() != null ?
                                activity.getSatelliteHwId() : ""))
                .collect(Collectors.toList());
    }

    private List<List<ImagingActivity>> splitWindowsByActivityState(List<ImagingActivity> activities) {
        if (activities == null || activities.isEmpty()) {
            return new ArrayList<>();
        }

        List<ImagingActivity> sortedActivities = createChronologicalWindow(activities);

        if (sortedActivities.isEmpty()) {
            logger.warn("No valid activities to split into windows");
            return new ArrayList<>();
        }

        List<List<ImagingActivity>> allWindows = new ArrayList<>();
        List<ImagingActivity> currentWindow = new ArrayList<>();
        currentWindow.add(sortedActivities.get(0));

        for (int i = 1; i < sortedActivities.size(); i++) {
            ImagingActivity currentActivity = sortedActivities.get(i);
            ImagingActivity previousActivity = sortedActivities.get(i - 1);

            String currentState = normalizeActivityState(currentActivity.getActivityState());
            String previousState = normalizeActivityState(previousActivity.getActivityState());

            Instant currentStart = parseInstantSafely(currentActivity.getStartTime());
            Instant previousEnd = parseInstantSafely(previousActivity.getEndTime());

            boolean stateChanged = !Objects.equals(currentState, previousState);
            boolean breaksStreaming = previousEnd != null && currentStart != null && currentStart.isBefore(previousEnd);

            if (stateChanged || breaksStreaming) {
                allWindows.add(new ArrayList<>(currentWindow));
                currentWindow.clear();
            }

            currentWindow.add(currentActivity);
        }

        if (!currentWindow.isEmpty()) {
            allWindows.add(currentWindow);
        }

        logger.info("Split {} activities into {} windows", sortedActivities.size(), allWindows.size());
        return allWindows;
    }

    private String normalizeActivityState(String activityState) {
        return activityState != null ? activityState.trim().toLowerCase() : null;
    }

    private Instant parseInstantSafely(String time) {
        if (time == null || time.trim().isEmpty()) return null;
        try {
            return Instant.parse(time.trim());
        } catch (DateTimeParseException e) {
            logger.warn("Invalid time format: {}", time, e);
            return null;
        }
    }
}
