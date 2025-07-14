package com.planet.assessment.mas.controller;

import com.planet.assessment.mas.dto.ActivityStateRequest;
import com.planet.assessment.mas.dto.ImagingActivity;
import com.planet.assessment.mas.service.ImagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/imaging")
public class ImagingController {

    private final ImagingService imagingService;

    @Autowired
    public ImagingController(ImagingService imagingService) {
        this.imagingService = imagingService;
    }

    /**
     * Task 1 Endpoint: Returns the complete, chronologically sorted imaging window.
     * Maps to GET /api/imaging/chronological-window
     */
    @GetMapping("/chronological-window")
    public ResponseEntity<List<ImagingActivity>> getChronologicalWindowFromFile() {
        try {
            List<ImagingActivity> sortedWindow = imagingService.processChronologicalWindowFromFile();
            return ResponseEntity.ok(sortedWindow);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read imaging data file.", e);
        }
    }

    /**
     * Task 2 Endpoint: Returns imaging windows filtered by a specific activity state.
     * Maps to POST /api/imaging/split-windows
     *
     * @param request A JSON object containing the "activityState" to filter by.
     * Example: { "activityState": "proposed" }
     */
    @PostMapping("/split-windows")
    public ResponseEntity<List<List<ImagingActivity>>> getSplitWindowsByState(@RequestBody ActivityStateRequest request) {
        try {
            List<List<ImagingActivity>> splitWindows = imagingService.processSplitWindowsByStateFromFile(request.getActivityState());
            return ResponseEntity.ok(splitWindows);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read imaging data file.", e);
        }
    }
}
