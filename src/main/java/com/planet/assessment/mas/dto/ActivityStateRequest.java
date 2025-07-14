package com.planet.assessment.mas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for receiving the activity state in a POST request.
 */
@Data
@NoArgsConstructor
public class ActivityStateRequest {
    private String activityState;
}
