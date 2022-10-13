package io.micronaut.data.azure

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.core.annotation.AnnotationMetadataProvider
import io.micronaut.core.naming.NameUtils
import io.micronaut.data.annotation.Query
import io.micronaut.data.azure.entities.Family
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.writer.BeanDefinitionVisitor

class BuildCosmosQuerySpec extends AbstractTypeElementSpec {

    void "test cosmos repo"() {
        given:
        def repository = buildRepository('test.CosmosBookRepository', """
import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.document.tck.entities.Book;
import java.util.Optional;
@CosmosRepository
interface CosmosBookRepository extends GenericRepository<Book, String> {
    Optional<Book> findById(String id);

    @Query("SELECT * FROM c WHERE c.title = :title")
    List<Book> findByTitle(String title);
}
"""
        )

        when:
        String findByIdQuery = getQuery(repository.getRequiredMethod("findById", String))
        def findByTitleMethod = repository.getRequiredMethod("findByTitle", String)
        String findByTitleQuery = getQuery(findByTitleMethod)
        def findByTitleRawQuery = findByTitleMethod.stringValue(Query.class, "rawQuery").orElse(null)
        then:
        findByIdQuery == "SELECT book_.id,book_.authorId,book_.title,book_.totalPages,book_.publisherId,book_.lastUpdated,book_.created FROM book book_ WHERE (book_.id = @p1)"
        findByTitleQuery == "SELECT * FROM c WHERE c.title = :title"
        findByTitleRawQuery == "SELECT * FROM c WHERE c.title = @p1"
    }

    void "test object properties and arrays"() {
        given:
        def repository = buildRepository('test.FamilyRepository', """
import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.azure.entities.Family;
import java.util.Optional;
@CosmosRepository
interface FamilyRepository extends GenericRepository<Family, String> {

    Optional<Family> findById(String id);
}
"""
        )

        when:
        def queryById = getQuery(repository.getRequiredMethod("findById", String))
        then:
        queryById == "SELECT family_.id,family_.lastName,family_.address,family_.children,family_.registered,family_.registeredDate FROM family family_ WHERE (family_.id = @p1)"
    }

    void "test build delete query"() {
        given:
        def repository = buildRepository('test.FamilyRepository', """
import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.azure.entities.Family;
import java.util.Optional;
import java.util.List;
@CosmosRepository
interface FamilyRepository extends GenericRepository<Family, String> {

    void deleteById(String id);

    void deleteByIds(List<String> ids);

    void deleteAll();

    void delete(Family family);
}
"""
        )

        when:
        def deleteByIdQuery = getQuery(repository.getRequiredMethod("deleteById", String))
        def deleteByIdsQuery = getQuery(repository.getRequiredMethod("deleteByIds", List<String>))
        def deleteAllQuery = getQuery(repository.getRequiredMethod("deleteAll"))
        def deleteQueryMethod = repository.getRequiredMethod("delete", Family)
        then:
        deleteByIdQuery == "SELECT *  FROM family  family_ WHERE (family_.id = @p1)"
        deleteByIdsQuery == "SELECT *  FROM family  family_ WHERE (family_.id IN (@p1))"
        deleteAllQuery == "SELECT *  FROM family  family_"
        !deleteQueryMethod.getAnnotation(Query)
    }

    BeanDefinition<?> buildRepository(String name, String source) {
        def pkg = NameUtils.getPackageName(name)
        return buildBeanDefinition(name + BeanDefinitionVisitor.PROXY_SUFFIX, """
package $pkg;
import io.micronaut.data.model.*;
import io.micronaut.data.repository.*;
import io.micronaut.data.annotation.*;
import java.util.*;
$source
""")

    }

    void "test build update query"() {
        given:
        def repository = buildRepository('test.FamilyRepository', """
import io.micronaut.data.cosmos.annotation.CosmosRepository;
import io.micronaut.data.azure.entities.Family;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.data.annotation.Id;
import java.util.Optional;
import java.util.List;
@CosmosRepository
interface FamilyRepository extends GenericRepository<Family, String> {
    long updateRegistered(@Parameter("id") @Id String id, @Parameter("registered") boolean registered);
}
"""
        )

        when:
        def updateRegisteredMethod = repository.getRequiredMethod("updateRegistered", String, boolean)
        def updateRegisteredQuery = getQuery(updateRegisteredMethod)
        def updateQuery = updateRegisteredMethod.stringValue(Query.class, "update").orElse(null)
        then:
        updateRegisteredQuery == "SELECT * FROM family family_ WHERE (family_.id = @p2)"
        updateQuery == "registered"
    }

    static String getQuery(AnnotationMetadataProvider metadata) {
        return metadata.getAnnotation(Query).stringValue().get()
    }
}
