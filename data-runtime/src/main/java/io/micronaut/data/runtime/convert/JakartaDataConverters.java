package io.micronaut.data.runtime.convert;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.MutableConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.data.model.Sort;
import jakarta.data.Order;

/**
 * Jakarta Data converters.
 *
 * @author Denis Stepanov
 * @since 4.12
 */
@Requires(classes = Order.class)
@Prototype
@Internal
public final class JakartaDataConverters implements TypeConverterRegistrar {

    @Override
    public void register(MutableConversionService conversionService) {
        conversionService.addConverter(Order.class, Sort.class, order -> Sort.of(
            ((Order<?>) order).sorts().stream().map(sort -> new Sort.Order(
                sort.property(),
                sort.isAscending() ? Sort.Order.Direction.ASC : Sort.Order.Direction.DESC,
                sort.ignoreCase())
            ).toList()
        ));
    }
}
