package net.domisafonov.propiotiempo.data

import net.domisafonov.propiotiempo.data.db.DatabaseSource

interface ReportRepository {
}

class ReportRepositoryImpl(
    database: DatabaseSource,
) : ReportRepository {
}
