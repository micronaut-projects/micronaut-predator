/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package io.micronaut.data.hibernate.jakarta_data.persistence;

import jakarta.persistence.Basic;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import java.util.Collections;
import java.util.Set;

@Entity
public class CatalogProduct {
    public enum Department {
        APPLIANCES, AUTOMOTIVE, CLOTHING, CRAFTS, ELECTRONICS, FURNITURE, GARDEN, GROCERY, OFFICE, PHARMACY, SPORTING_GOODS, TOOLS
    }

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Department> departments;

    @Basic(optional = false)
    private String name;

    private Double price;

    @Basic(optional = false)
    @Id
    private String productNum;

    @Transient
    private Double surgePrice;

    @Version
    private long versionNum;

    public static CatalogProduct of(String name, Double price, String productNum, Department... departments) {
        return new CatalogProduct(name, price, price, productNum, departments);
    }

    private CatalogProduct(String name, Double price, Double surgePrice, String productNum, Department... departments) {
        this.productNum = productNum;
        this.name = name;
        this.price = price;
        this.surgePrice = surgePrice;
        this.departments = departments == null ? Collections.emptySet() : Set.of(departments);
    }

    public CatalogProduct() {
        //do nothing
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getProductNum() {
        return productNum;
    }

    public void setProductNum(String productNum) {
        this.productNum = productNum;
    }

    public long getVersionNum() {
        return this.versionNum;
    }

    public Double getSurgePrice() {
        return surgePrice;
    }

    public void setSurgePrice(Double surgePrice) {
        this.surgePrice = surgePrice;
    }

    public void setVersionNum(long versionNum) {
        this.versionNum = versionNum;
    }

    @Override
    public String toString() {
        return "Product [departments=" + departments + ", name=" + name + ", price=" + price + ", productNum="
                + productNum + ", surgePrice=" + surgePrice + ", versionNum=" + versionNum + "]";
    }
}
