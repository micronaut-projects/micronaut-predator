package io.micronaut.data.jdbc

import io.micronaut.data.connection.jdbc.operations.DataSourceConnectionOperations
import io.micronaut.data.tck.tests.AbstractTransactionSpec
import io.micronaut.transaction.TransactionOperations
import io.micronaut.transaction.jdbc.DataSourceTransactionManager

import java.sql.Connection

abstract class AbstractJdbcTransactionSpec extends AbstractTransactionSpec {

    @Override
    protected TransactionOperations getTransactionOperations() {
        return context.getBean(DataSourceTransactionManager)
    }

    @Override
    protected Runnable getNoTxCheck() {
        DataSourceConnectionOperations connectionOperations = context.getBean(DataSourceConnectionOperations)
        return new Runnable() {
            @Override
            void run() {
                Connection connection = connectionOperations.getConnectionStatus().getConnection()
                // No transaction -> autoCommit == true
                assert connection.getAutoCommit()
            }
        }
    }

}