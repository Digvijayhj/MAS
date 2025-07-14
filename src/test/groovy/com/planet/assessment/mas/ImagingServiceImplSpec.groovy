package com.planet.assessment.mas;

import com.fasterxml.jackson.databind.ObjectMapper
import com.planet.assessment.mas.dto.ImagingActivity
import com.planet.assessment.mas.service.ImagingService // FIX: Added the missing import for the service interface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static org.mockito.Mockito.when

@SpringBootTest
class ImagingServiceSpec extends Specification {

    @Autowired
    private ImagingService imagingService

    @MockBean
    private ResourceLoader resourceLoader

    private final ObjectMapper objectMapper = new ObjectMapper()

    // Helper to mock the file loading process.
    private void mockFileContent(List<ImagingActivity> activities) {
        String jsonString = objectMapper.writeValueAsString(activities)
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))
        Resource mockResource = Mock(Resource)
        when(mockResource.exists()).thenReturn(true)
        when(mockResource.getInputStream()).thenReturn(inputStream)
        when(resourceLoader.getResource("classpath:/imaging_activities.json")).thenReturn(mockResource)
    }

    def "Task 1: should sort valid activities and filter out invalid ones"() {
        given: "a mix of valid and invalid activities are in the file"
        def activities = [
                new ImagingActivity(satelliteHwId: "s112", activityState: "scheduled", startTime: "2024-07-12T01:03:49Z"),
                new ImagingActivity(satelliteHwId: "s112", activityState: "scheduled", startTime: null), // Invalid
                new ImagingActivity(satelliteHwId: "s112", activityState: "executed", startTime: "2024-07-12T00:33:00Z")
        ]
        mockFileContent(activities)

        when: "the chronological window is processed"
        List<ImagingActivity> result = imagingService.processChronologicalWindowFromFile()

        then: "only valid activities are returned, and they are sorted"
        result.size() == 2
        result[0].startTime == "2024-07-12T00:33:00Z"
        result[1].startTime == "2024-07-12T01:03:49Z"
    }

    def "Task 2: should split windows correctly"() {
        given: "activities that will form multiple windows"
        def activities = [
                new ImagingActivity(activityState: "scheduled", startTime: "2024-07-12T01:00:00Z"),
                new ImagingActivity(activityState: "proposed", startTime: "2024-07-12T01:02:00Z"),
                new ImagingActivity(activityState: "proposed", startTime: "2024-07-12T01:03:00Z")
        ]
        mockFileContent(activities)

        when: "the windows are processed with no filter"
        List<List<ImagingActivity>> result = imagingService.processSplitWindowsByStateFromFile(null)

        then: "the activities are split into two distinct windows"
        result.size() == 2
        result[0].size() == 1
        result[0][0].activityState == "scheduled"
        result[1].size() == 2
        result[1][0].activityState == "proposed"
    }

    def "Task 2: should correctly filter windows by state"() {
        given: "a standard set of activities"
        def activities = [
                new ImagingActivity(activityState: "scheduled", startTime: "2024-07-12T01:02:00Z"),
                new ImagingActivity(activityState: "proposed", startTime: "2024-07-12T01:06:00Z")
        ]
        mockFileContent(activities)

        when: "the windows are processed and filtered by 'scheduled'"
        List<List<ImagingActivity>> result = imagingService.processSplitWindowsByStateFromFile("scheduled")

        then: "only the scheduled window is returned"
        result.size() == 1
        result[0][0].activityState == "scheduled"
    }

    def "should return empty list if the file does not exist"() {
        given: "the resource loader is mocked to say the file doesn't exist"
        Resource mockResource = Mock(Resource)
        when(mockResource.exists()).thenReturn(false)
        when(resourceLoader.getResource("classpath:/imaging_activities.json")).thenReturn(mockResource)

        when: "the service is called"
        def result = imagingService.processChronologicalWindowFromFile()

        then: "it returns an empty list"
        result.isEmpty()
    }
}
