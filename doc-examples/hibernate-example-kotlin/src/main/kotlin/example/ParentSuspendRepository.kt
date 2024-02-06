package example

import io.micronaut.core.annotation.NonNull
import io.micronaut.data.annotation.Join
import io.micronaut.data.annotation.Repository
import io.micronaut.data.annotation.RepositoryConfiguration
import io.micronaut.data.model.query.builder.jpa.JpaQueryBuilder
import io.micronaut.data.repository.GenericRepository
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import jakarta.transaction.Transactional
import java.util.*

@Repository
@RepositoryConfiguration(queryBuilder = JpaQueryBuilder::class) // it was necessary to define a QueryBuilder for the findAll(): Flow scenario
interface ParentSuspendRepository : GenericRepository<Parent, Int>, CoroutineJpaSpecificationExecutor<Parent> {

    @Join(value = "children", type = Join.Type.FETCH)
    suspend fun findById(id: Int): Optional<Parent>

    @Transactional(Transactional.TxType.MANDATORY)
    suspend fun queryById(id: Int): Optional<Parent>

    suspend fun save(@NonNull entity: Parent): Parent

    suspend fun update(@NonNull entity: Parent): Parent

}
