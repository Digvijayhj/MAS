package com.planet.assessment.mas


import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class MissionAwarenessServiceApplicationSpec extends Specification {

    def "application context loads successfully"() {
        expect:
        true // Context loading is tested by @SpringBootTest; if it fails, this test fails
    }
}
