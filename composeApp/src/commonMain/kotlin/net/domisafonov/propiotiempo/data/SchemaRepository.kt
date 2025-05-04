package net.domisafonov.propiotiempo.data

import net.domisafonov.propiotiempo.data.db.DatabaseSource

interface SchemaRepository {
}

class SchemaRepositoryImpl(
    database: DatabaseSource,
) : SchemaRepository {
}
