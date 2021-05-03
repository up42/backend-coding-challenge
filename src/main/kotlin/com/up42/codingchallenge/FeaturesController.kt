package com.up42.codingchallenge

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
class FeaturesController {
    @GetMapping("/features")
    fun getFeatures(): List<FeatureCollection.Feature> =
        ClassPathResource("/static/source-data.json").file.readText()
            .let { jsonString ->
                jacksonObjectMapper().readValue<List<FeatureCollection>>(jsonString)
            }.flatMap {
                it.features
            }.map {
                it.apply {
                    id = properties?.id
                    timestamp = properties?.timestamp
                    beginViewingDate = properties?.acquisition?.beginViewingDate
                    endViewingDate = properties?.acquisition?.endViewingDate
                    missionName = properties?.acquisition?.missionName
                }
            }.ifEmpty {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No features found")
            }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class FeatureCollection(var features: List<Feature>) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Feature(
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        var properties: Properties? = null,
        var id: UUID?,
        var timestamp: Long?,
        var beginViewingDate: Long?,
        var endViewingDate: Long?,
        var missionName: String?
    ) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Properties(
            var id: UUID?,
            var timestamp: Long?,
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
            var acquisition: Acquisition? = null,
        ) {

            @JsonIgnoreProperties(ignoreUnknown = true)
            data class Acquisition(
                var beginViewingDate: Long?,
                var endViewingDate: Long?,
                var missionName: String?
            )
        }
    }
}
