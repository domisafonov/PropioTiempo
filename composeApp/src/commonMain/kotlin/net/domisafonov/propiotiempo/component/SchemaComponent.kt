package net.domisafonov.propiotiempo.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import net.domisafonov.propiotiempo.data.SchemaRepository

interface SchemaComponent : ComponentContext

fun makeSchemaComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    schemaRepositoryProvider: Lazy<SchemaRepository>,
) : SchemaComponent = SchemaComponentImpl(
    componentContext = componentContext,
    storeFactory = storeFactory,
    schemaRepositoryProvider = schemaRepositoryProvider,
)

private class SchemaComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    schemaRepositoryProvider: Lazy<SchemaRepository>,
) : SchemaComponent, ComponentContext by componentContext
