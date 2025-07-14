package com.planet.assessment.mas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single imaging activity.
 * Using Lombok @Data to generate getters, setters, equals, hashCode, and toString.
 * @NoArgsConstructor and @AllArgsConstructor generate the respective constructors.
 */

@Data
public class ImagingActivity {

    @JsonProperty("satellite_hw_id")
    private String satelliteHwId;

    @JsonProperty("activity_state")
    private String activityState;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

}
