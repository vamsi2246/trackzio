package com.weathersnap.data.repository

import com.weathersnap.domain.model.CitySuggestion
import com.weathersnap.domain.model.Report
import com.weathersnap.domain.model.WeatherSnapshot
import com.weathersnap.domain.repository.ReportRepository
import com.weathersnap.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Test double for WeatherRepository — no mocking library needed. */
class FakeWeatherRepository : WeatherRepository {
    var cities: List<CitySuggestion> = emptyList()
    var snapshot: WeatherSnapshot? = null
    var shouldFail: Boolean = false

    override suspend fun searchCities(query: String): Result<List<CitySuggestion>> {
        return if (shouldFail) Result.failure(RuntimeException("Fake error"))
        else Result.success(cities)
    }

    override suspend fun getWeather(city: CitySuggestion): Result<WeatherSnapshot> {
        return if (shouldFail) Result.failure(RuntimeException("Fake error"))
        else snapshot?.let { Result.success(it) }
            ?: Result.failure(RuntimeException("No snapshot configured"))
    }
}

/** Test double for ReportRepository. */
class FakeReportRepository : ReportRepository {
    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    var shouldFail: Boolean = false
    val savedReports: List<Report> get() = _reports.value

    override fun getAllReports(): Flow<List<Report>> = _reports

    override suspend fun saveReport(report: Report): Result<Long> {
        return if (shouldFail) Result.failure(RuntimeException("Fake DB error"))
        else {
            val newList = _reports.value + report.copy(id = (_reports.value.size + 1).toLong())
            _reports.value = newList
            Result.success(newList.last().id)
        }
    }
}
