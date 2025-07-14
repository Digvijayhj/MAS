package com.planet.assessment.mas.service;

import com.planet.assessment.mas.dto.ImagingActivity;
import java.io.IOException;
import java.util.List;

/**
 * Interface for the Imaging Service.
 * Defines the contract for processing imaging activities from the local data file.
 */
public interface ImagingService {

    /**
     * Processes Task 1: Reads from the local JSON file and returns a sorted list.
     * @return A sorted list of all imaging activities.
     * @throws IOException if the data file cannot be read.
     */
    List<ImagingActivity> processChronologicalWindowFromFile() throws IOException;

    /**
     * Processes Task 2: Reads from the local JSON file, creates all imaging windows,
     * and then filters them based on the provided activity state.
     *
     * @param activityState The state to filter windows by (e.g., "scheduled", "proposed").
     * If null or blank, all windows are returned.
     * @return A filtered list of imaging windows.
     * @throws IOException if the data file cannot be read.
     */
    List<List<ImagingActivity>> processSplitWindowsByStateFromFile(String activityState) throws IOException;
}
