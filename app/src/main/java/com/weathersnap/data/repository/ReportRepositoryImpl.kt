package com.weathersnap.data.repository

import com.weathersnap.data.local.ReportDao
import com.weathersnap.data.local.toDomain
import com.weathersnap.data.local.toEntity
import com.weathersnap.domain.model.Report
import com.weathersnap.domain.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ReportRepositoryImpl(
    private val reportDao: ReportDao
) : ReportRepository {

    override fun getAllReports(): Flow<List<Report>> {
        return reportDao.getAllReports().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveReport(report: Report): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val id = reportDao.insertReport(report.toEntity())
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
