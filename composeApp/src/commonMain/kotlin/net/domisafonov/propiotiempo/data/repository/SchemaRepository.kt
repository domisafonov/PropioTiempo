package net.domisafonov.propiotiempo.data.repository

import net.domisafonov.propiotiempo.data.db.DatabaseSource

interface SchemaRepository {
}

class SchemaRepositoryImpl(
    database: DatabaseSource,
) : SchemaRepository {
}
