package com.planet.assessment.mas

import com.fasterxml.jackson.databind.ObjectMapper
import com.planet.assessment.mas.dto.ActivityStateRequest
import com.planet.assessment.mas.dto.ImagingActivity
import com.planet.assessment.mas.service.ImagingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

import com.planet.assessment.mas.controller.ImagingController

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = ImagingController.class)
class ImagingControllerSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ObjectMapper objectMapper

    @MockBean
    private ImagingService imagingService

    def "GET /api/imaging/chronological-window should return 200 OK on success"() {
        given: "the service will successfully return a list of activities"
        def activities = [new ImagingActivity(satelliteHwId: "s112", activityState: "executed", startTime: "2024-07-12T01:00:00Z")]
        imagingService.processChronologicalWindowFromFile() >> activities

        when: "a GET request is made to the endpoint"
        def result = mockMvc.perform(get("/api/imaging/chronological-window"))

        then: "the response is 200 OK and contains the expected data"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.size()').value(1))
                .andExpect(jsonPath('$[0].satellite_hw_id').value("s112"))
    }

    def "POST /api/imaging/split-windows should return 200 OK on success"() {
        given: "a request to filter by a specific state"
        def request = new ActivityStateRequest(activityState: "proposed")
        def responseWindows = [[new ImagingActivity(satelliteHwId: "s112", activityState: "proposed", startTime: "2024-07-12T01:06:00Z")]]
        imagingService.processSplitWindowsByStateFromFile("proposed") >> responseWindows

        when: "a POST request is made to the endpoint"
        def result = mockMvc.perform(post("/api/imaging/split-windows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "the response is 200 OK and contains the filtered data"
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.size()').value(1))
                .andExpect(jsonPath('$[0][0].activity_state').value("proposed"))
    }

    def "GET endpoint should return 500 and cover catch block when service throws IOException"() {
        given: "the service is mocked to throw an IOException"
        imagingService.processChronologicalWindowFromFile() >> { throw new IOException("Test file read error!") }

        when: "a GET request is made to the endpoint"
        def result = mockMvc.perform(get("/api/imaging/chronological-window"))

        then: "the response is 500 Internal Server Error, confirming the catch block was executed"
        result.andExpect(status().isInternalServerError())
        // FIX: Add a check for the specific reason message from the ResponseStatusException
                .andExpect(MockMvcResultMatchers.status().reason("Could not read imaging data file."))
    }

    def "POST endpoint should return 500 and cover catch block when service throws IOException"() {
        given: "a request object"
        def request = new ActivityStateRequest(activityState: "scheduled")
        and: "the service is mocked to throw an IOException for that state"
        imagingService.processSplitWindowsByStateFromFile("scheduled") >> { throw new IOException("Test file read error!") }

        when: "a POST request is made to the endpoint"
        def result = mockMvc.perform(post("/api/imaging/split-windows")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then: "the response is 500 Internal Server Error, confirming the catch block was executed"
        result.andExpect(status().isInternalServerError())
        // FIX: Add a check for the specific reason message from the ResponseStatusException
                .andExpect(MockMvcResultMatchers.status().reason("Could not read imaging data file."))
    }
}
