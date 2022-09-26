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
package io.micronaut.data.model.naming;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.Embedded;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * A strategy interface for resolving the mapped name of an entity or property.
 *
 * @author graemerocher
 * @since 1.0
 */
@FunctionalInterface
@Introspected
public interface NamingStrategy {

    /**
     * Constant for the default under score separated lower case strategy.
     */
    NamingStrategy DEFAULT = new NamingStrategies.UnderScoreSeparatedLowerCase();

    /**
     * Return the mapped name for the given name.
     * @param name The name
     * @return The mapped name
     */
    @NonNull
    String mappedName(@NonNull String name);

    /**
     * Return the mapped name for the given entity.
     * @param entity The entity
     * @return The mapped name
     */
    default @NonNull String mappedName(@NonNull PersistentEntity entity) {
        ArgumentUtils.requireNonNull("entity", entity);
        return entity.getAnnotationMetadata().stringValue(MappedEntity.class)
                .filter(StringUtils::isNotEmpty)
                .orElseGet(() -> mappedName(entity.getSimpleName()));
    }

    /**
     * Return the mapped name given an {@link Embedded} association and the property of the association. The
     * default strategy takes the parent embedded property name and combines it underscore separated with the child parent property name.
     *
     * <p>For example given:</p>
     *
     * <pre><code>
     * {@literal @}Embedded Address address;
     * </code></pre>
     *
     * <p>Where the {@code Address} type has a property called {@code street} then a name of {@code address_street} will be returned</p>
     * @param embedded The embedded parent
     * @param property The embedded property
     * @return The mapped name
     */
    default @NonNull String mappedName(Embedded embedded, PersistentProperty property) {
        return mappedName(embedded.getName() + NameUtils.capitalize(property.getPersistedName()));
    }

    /**
     * Return the mapped name for the given property.
     * @param property The property
     * @return The mapped name
     */
    default @NonNull String mappedName(@NonNull PersistentProperty property) {
        return mappedName(property, true);
    }

    /**
     * Return the mapped name for the given property.
     * @param property The property
     * @param useGenerated an indicator telling whether generated name in the entity visitor should be used or else force mapped name calculation
     * @return The mapped name
     */
    default @NonNull String mappedName(@NonNull PersistentProperty property, boolean useGenerated) {
        ArgumentUtils.requireNonNull("property", property);
        if (property instanceof Association) {
            return mappedName((Association) property, useGenerated);
        } else {
            AnnotationMetadata propertyAnnotationMetadata = property.getAnnotationMetadata();
            boolean generated = propertyAnnotationMetadata.booleanValue(MappedProperty.class, "generated").orElse(false);
            return propertyAnnotationMetadata
                    .stringValue(MappedProperty.class)
                    .filter(n -> StringUtils.isNotEmpty(n) && generated ? useGenerated : true)
                    .orElseGet(() -> mappedName(property.getName()));
        }
    }

    /**
     * Return the mapped name for the given association.
     * @param association The association
     * @return The mapped name
     */
    default @NonNull String mappedName(Association association) {
        return mappedName(association, true);
    }

    /**
     * Return the mapped name for the given association.
     * @param association The association
     * @param useGenerated an indicator telling whether generated name in the entity visitor should be used or else force mapped name calculation
     * @return The mapped name
     */
    default @NonNull String mappedName(Association association, boolean useGenerated) {
        AnnotationMetadata assocationAnnotationMetadata = association.getAnnotationMetadata();
        boolean generated = assocationAnnotationMetadata.booleanValue(MappedProperty.class, "generated").orElse(false);
        boolean useExisting = generated ? useGenerated : true;
        String providedName = useExisting ? assocationAnnotationMetadata.stringValue(MappedProperty.class).orElse(null) : null;
        if (providedName != null) {
            return providedName;
        }
        if (association.isForeignKey()) {
            Optional<Association> inverseSide = association.getInverseSide().map(Function.identity());
            Association owningAssociation = inverseSide.orElse(association);
            return mappedName(owningAssociation.getOwner().getDecapitalizedName() + owningAssociation.getAssociatedEntity().getSimpleName());
        } else {
            switch (association.getKind()) {
                case ONE_TO_ONE:
                case MANY_TO_ONE:
                    return mappedName(association.getName() + getForeignKeySuffix());
                default:
                    return mappedName(association.getName());
            }
        }
    }

    default @NonNull String mappedName(@NonNull List<Association> associations, @NonNull PersistentProperty property) {
        return mappedName(associations, property, true);
    }

    /**
     * Gets the mapped name from for the list of associations and property.
     *
     * @param associations the list of associations
     * @param property the peristent property
     * @param useGenerated an indicator telling whether generated name in the entity visitor should be used or else force mapped name calculation
     * @return mapped name for the list of associations and property calculated using naming strategy
     */
    default @NonNull String mappedName(@NonNull List<Association> associations, @NonNull PersistentProperty property, boolean useGenerated) {
        if (associations.isEmpty()) {
            return mappedName(property, useGenerated);
        }
        StringBuilder sb = new StringBuilder();
        Association foreignAssociation = null;
        for (Association association : associations) {
            if (association.getKind() != Relation.Kind.EMBEDDED) {
                if (foreignAssociation == null) {
                    foreignAssociation = association;
                }
            }
            if (sb.length() > 0) {
                sb.append(NameUtils.capitalize(association.getName()));
            } else {
                sb.append(association.getName());
            }
        }
        if (foreignAssociation != null) {
            if (foreignAssociation.getAssociatedEntity() == property.getOwner()
                    && foreignAssociation.getAssociatedEntity().getIdentity() == property) {
                AnnotationMetadata foreignAssociationAnnotationMetadata = foreignAssociation.getAnnotationMetadata();
                boolean generated = foreignAssociationAnnotationMetadata.booleanValue(MappedProperty.class, "generated").orElse(false);
                boolean useExisting = generated ? useGenerated : true;
                String providedName = useExisting ? foreignAssociationAnnotationMetadata.stringValue(MappedProperty.class).orElse(null) : null;
                if (providedName != null) {
                    return providedName;
                }
                sb.append(getForeignKeySuffix());
                return mappedName(sb.toString());
            } else if (foreignAssociation.isForeignKey()) {
                throw new IllegalStateException("Foreign association cannot be mapped!");
            }
        } else {
            AnnotationMetadata propertyAnnotationMetadata = property.getAnnotationMetadata();
            boolean generated = propertyAnnotationMetadata.booleanValue(MappedProperty.class, "generated").orElse(false);
            boolean useExisting = generated ? useGenerated : true;
            String providedName = useExisting ? propertyAnnotationMetadata.stringValue(MappedProperty.class).orElse(null) : null;
            if (providedName != null) {
                return providedName;
            }
        }
        if (sb.length() > 0) {
            sb.append(NameUtils.capitalize(property.getName()));
        } else {
            sb.append(property.getName());
        }
        return mappedName(sb.toString());
    }

    default String mappedJoinTableColumn(PersistentEntity associated, List<Association> associations, PersistentProperty property) {
        StringBuilder sb = new StringBuilder();
        sb.append(associated.getDecapitalizedName());
        for (Association association : associations) {
            sb.append(NameUtils.capitalize(association.getName()));
        }
        if (associations.isEmpty()) {
            sb.append(getForeignKeySuffix());
        } else {
            sb.append(NameUtils.capitalize(property.getName()));
        }
        return mappedName(sb.toString());
    }

    /**
     * The default foreign key suffix for property names.
     * @return The suffix
     */
    default @NonNull String getForeignKeySuffix() {
        return "Id";
    }
}
