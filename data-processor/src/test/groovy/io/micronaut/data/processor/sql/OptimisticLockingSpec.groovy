/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.processor.sql

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.TypeRole
import io.micronaut.data.intercept.annotation.DataMethod
import io.micronaut.data.processor.visitors.AbstractDataSpec

class OptimisticLockingSpec extends AbstractDataSpec {

    void "test optimistic locking methods"() {
        given:
            def repository = buildRepository('test.StudentRepository', """
  import io.micronaut.data.jdbc.annotation.JdbcRepository;
  import io.micronaut.data.model.query.builder.sql.Dialect;
  import io.micronaut.data.tck.entities.Student;

  @JdbcRepository(dialect= Dialect.MYSQL)
  @io.micronaut.context.annotation.Executable
  interface StudentRepository extends CrudRepository<Student, Long> {

    void updateByIdAndVersion(Long id, Long version, String name);
    
    void updateStudent1(@Id Long id, @Version Long zzz, String name);

    void updateStudent2(@Id Long id, @Version Long version, String name);
    
    void updateById(@Id Long id, @Version Long version, String name);

    void update(@Id Long id, @Version Long version, String name);

    void delete(@Id Long id, @Version Long version, String name);
    
    void deleteByIdAndVersionAndName(Long id, Long version, String name);

  }
  """)

        when:
            def updateByIdAndVersionMethod = repository.findPossibleMethods("updateByIdAndVersion").findFirst().get()
            def updateByIdAndVersionQuery = updateByIdAndVersionMethod.stringValue(Query).get()

        then:
            updateByIdAndVersionQuery == 'UPDATE `student` SET `version`=?,`name`=?,`last_updated_time`=? WHERE (`id` = ? AND `version` = ?)'
            updateByIdAndVersionMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ['$versionUpdateParameter', "", "lastUpdatedTime", "", 'version'] as String[]
            updateByIdAndVersionMethod.stringValue(DataMethod, TypeRole.LAST_UPDATED_PROPERTY).get() == 'lastUpdatedTime'
            updateByIdAndVersionMethod.stringValue(DataMethod, TypeRole.VERSION_UPDATE).get() == '$versionUpdateParameter'
            updateByIdAndVersionMethod.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'version'
            updateByIdAndVersionMethod.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def updateMethod = repository.findPossibleMethods("update").filter({ it -> it.getArguments().size() == 1 }).findFirst().get()
            def updateQuery = updateMethod.stringValue(Query).get()

        then:
            updateQuery == 'UPDATE `student` SET `version`=?,`name`=?,`last_updated_time`=? WHERE (`id` = ? AND `version` = ?)'
            updateMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ['version', "name", "lastUpdatedTime", "id", '$versionMatchParameter'] as String[]
            updateMethod.stringValue(DataMethod, TypeRole.LAST_UPDATED_PROPERTY).get() == 'lastUpdatedTime'
            updateMethod.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == '$versionMatchParameter'
            updateMethod.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def updateStudentMethod1 = repository.findPossibleMethods("updateStudent1").findFirst().get()
            def updateStudentQuery1 = updateStudentMethod1.stringValue(Query).get()

        then:
            updateStudentQuery1 == 'UPDATE `student` SET `version`=?,`name`=?,`last_updated_time`=? WHERE (`id` = ? AND `version` = ?)'
            updateStudentMethod1.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ['$versionUpdateParameter', "", "lastUpdatedTime", "id", "zzz"] as String[]
            updateStudentMethod1.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING) == ['-1', '2', '-1', '0', "1"] as String[]
            updateStudentMethod1.stringValue(DataMethod, TypeRole.LAST_UPDATED_PROPERTY).get() == 'lastUpdatedTime'
            updateStudentMethod1.stringValue(DataMethod, TypeRole.VERSION_UPDATE).get() == '$versionUpdateParameter'
            updateStudentMethod1.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'zzz'
            updateStudentMethod1.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def updateStudentMethod2 = repository.findPossibleMethods("updateStudent2").findFirst().get()
            def updateStudentQuery2 = updateStudentMethod2.stringValue(Query).get()

        then:
            updateStudentQuery2 == 'UPDATE `student` SET `version`=?,`name`=?,`last_updated_time`=? WHERE (`id` = ? AND `version` = ?)'
            updateStudentMethod2.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ['$versionUpdateParameter', "", "lastUpdatedTime", "id", "version"] as String[]
            updateStudentMethod2.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING) == ['-1', '2', '-1', '0', "1"] as String[]
            updateStudentMethod2.stringValue(DataMethod, TypeRole.LAST_UPDATED_PROPERTY).get() == 'lastUpdatedTime'
            updateStudentMethod2.stringValue(DataMethod, TypeRole.VERSION_UPDATE).get() == '$versionUpdateParameter'
            updateStudentMethod2.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'version'
            updateStudentMethod2.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def updateByStudentMethod = repository.findPossibleMethods("updateById").findFirst().get()
            def updateByStudentQuery = updateByStudentMethod.stringValue(Query).get()

        then:
            updateByStudentQuery == 'UPDATE `student` SET `version`=?,`name`=?,`last_updated_time`=? WHERE (`id` = ? AND `version` = ?)'
            updateByStudentMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ['$versionUpdateParameter', "", "lastUpdatedTime", "id", "version"] as String[]
            updateByStudentMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING) == ['-1', '2', '-1', '0', "1"] as String[]
            updateByStudentMethod.stringValue(DataMethod, TypeRole.LAST_UPDATED_PROPERTY).get() == 'lastUpdatedTime'
            updateByStudentMethod.stringValue(DataMethod, TypeRole.VERSION_UPDATE).get() == '$versionUpdateParameter'
            updateByStudentMethod.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'version'
            updateByStudentMethod.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def deleteMethod = repository.findPossibleMethods("delete").filter({ it -> it.getArguments().size() == 1 }).findFirst().get()
            def deleteQuery = deleteMethod.stringValue(Query).get()

        then:
            deleteQuery == 'DELETE  FROM `student`  WHERE (`id` = ? AND `version` = ?)'
            deleteMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ["", ""] as String[]
            deleteMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING) == ['0', '0'] as String[]
            deleteMethod.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'version'
            deleteMethod.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def deleteStudentMethod = repository.findPossibleMethods("delete").filter({ it -> it.getArguments().size() > 1 }).findFirst().get()
            def deleteStudentQuery = deleteStudentMethod.stringValue(Query).get()

        then:
            deleteStudentQuery == 'DELETE  FROM `student`  WHERE (`id` = ? AND `version` = ? AND `name` = ?)'
            deleteStudentMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ["", "version", ""] as String[]
            deleteStudentMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING) == ['0', '1', '2'] as String[]
            deleteStudentMethod.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'version'
            deleteStudentMethod.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)

        when:
            def deleteByIdAndVersionAndNameMethod = repository.findPossibleMethods("deleteByIdAndVersionAndName").findFirst().get()
            def deleteByIdAndVersionAndNameQuery = deleteStudentMethod.stringValue(Query).get()

        then:
            deleteByIdAndVersionAndNameQuery == 'DELETE  FROM `student`  WHERE (`id` = ? AND `version` = ? AND `name` = ?)'
            deleteByIdAndVersionAndNameMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING_PATHS) == ["", "version", ""] as String[]
            deleteByIdAndVersionAndNameMethod.stringValues(DataMethod, DataMethod.META_MEMBER_PARAMETER_BINDING) == ['0', '1', '2'] as String[]
            deleteByIdAndVersionAndNameMethod.stringValue(DataMethod, TypeRole.VERSION_MATCH).get() == 'version'
            deleteByIdAndVersionAndNameMethod.booleanValue(DataMethod, DataMethod.META_MEMBER_OPTIMISTIC_LOCK)
    }
}
