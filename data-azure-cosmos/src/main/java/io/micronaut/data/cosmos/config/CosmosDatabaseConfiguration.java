/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.data.cosmos.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static io.micronaut.data.cosmos.config.CosmosDatabaseConfiguration.PREFIX;

/**
 * The Azure Cosmos database configuration.
 *
 * @author radovanradic
 * @since 4.0.0
 */
@ConfigurationProperties(PREFIX)
public final class CosmosDatabaseConfiguration {

    public static final String PREFIX = "azure.cosmos.database";

    public static final String UPDATE_POLICY = PREFIX + ".update-policy";

    private ThroughputSettings throughput;

    private List<CosmosContainerSettings> containers;

    private String databaseName;

    private StorageUpdatePolicy updatePolicy = StorageUpdatePolicy.NONE;

    private List<String> packages = new ArrayList<>();

    public CosmosDatabaseConfiguration.ThroughputSettings getThroughput() {
        return throughput;
    }

    @Inject
    public void setThroughput(CosmosDatabaseConfiguration.ThroughputSettings throughput) {
        this.throughput = throughput;
    }

    public List<CosmosContainerSettings> getContainers() {
        return containers;
    }

    @Inject
    public void setContainers(List<CosmosContainerSettings> containers) {
        this.containers = containers;
    }

    /**
     * @return the database name
     */
    @NonNull
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Sets the database name.
     *
     * @param databaseName the database name
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * @return the update policy for the database to be used during startup.
     */
    public StorageUpdatePolicy getUpdatePolicy() {
        return updatePolicy;
    }

    /**
     * Sets the update policy for the database to be used during startup.
     *
     * @param updatePolicy the update policy for the database
     */
    public void setUpdatePolicy(StorageUpdatePolicy updatePolicy) {
        this.updatePolicy = updatePolicy;
    }

    /**
     * @return the list of package names to filter entities during init database and containers
     */
    public List<String> getPackages() {
        return packages;
    }

    /**
     * @param packages the package names to be considered during init
     */
    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    /**
     * Throughput settings for database and container.
     */
    @ConfigurationProperties("throughput-settings")
    public static final class ThroughputSettings {

        private Integer requestUnits;

        private boolean autoScale;

        public Integer getRequestUnits() {
            return requestUnits;
        }

        public void setRequestUnits(Integer requestUnits) {
            this.requestUnits = requestUnits;
        }

        public boolean isAutoScale() {
            return autoScale;
        }

        public void setAutoScale(boolean autoScale) {
            this.autoScale = autoScale;
        }
    }

    /**
     * The settings for Cosmos container.
     */
    @EachProperty(value = "container-settings", list = true)
    public static class CosmosContainerSettings {

        private String containerName;

        private String partitionKeyPath;

        private CosmosDatabaseConfiguration.ThroughputSettings throughput;

        /**
         * @return the container name
         */
        public String getContainerName() {
            return containerName;
        }

        /**
         * Sets the container name.
         *
         * @param containerName the container name
         */
        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }

        /**
         * @return the partition key path for the container
         */
        public String getPartitionKeyPath() {
            return partitionKeyPath;
        }

        /**
         * Sets the container partition key path.
         *
         * @param partitionKeyPath the partition key path
         */
        public void setPartitionKeyPath(String partitionKeyPath) {
            this.partitionKeyPath = partitionKeyPath;
        }

        /**
         * @return container throughput settings
         */
        public CosmosDatabaseConfiguration.ThroughputSettings getThroughput() {
            return throughput;
        }

        /**
         * Sets the container throughput settings.
         *
         * @param throughput the throughput settings
         */
        @Inject
        public void setThroughput(CosmosDatabaseConfiguration.ThroughputSettings throughput) {
            this.throughput = throughput;
        }
    }
}
