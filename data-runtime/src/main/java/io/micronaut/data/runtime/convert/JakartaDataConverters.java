/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.data.runtime.convert;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.MutableConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.data.model.CursoredPageable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.PageRecord;

import java.util.Arrays;

/**
 * Jakarta Data converters.
 *
 * @author Denis Stepanov
 * @since 4.12
 */
@Requires(classes = Order.class)
@Prototype
@Internal
final class JakartaDataConverters implements TypeConverterRegistrar {

    @Override
    public void register(MutableConversionService conversionService) {
        conversionService.addConverter(Limit.class, io.micronaut.data.model.Limit.class,
            limit -> io.micronaut.data.model.Limit.of(limit.maxResults(), (int) limit.startAt() - 1));
        conversionService.addConverter(Order.class, Sort.class, order -> Sort.of(
            ((Order<?>) order).sorts().stream().map(sort -> new Sort.Order(
                sort.property(),
                sort.isAscending() ? Sort.Order.Direction.ASC : Sort.Order.Direction.DESC,
                sort.ignoreCase())
            ).toList()
        ));
        conversionService.addConverter(jakarta.data.Sort.class, Sort.class, sort -> Sort.of(
                new Sort.Order(
                    sort.property(),
                    sort.isAscending() ? Sort.Order.Direction.ASC : Sort.Order.Direction.DESC,
                    sort.ignoreCase())
            )
        );
        conversionService.addConverter(jakarta.data.Sort[].class, Sort.class, sort -> Sort.of(
                Arrays.stream(sort).map(s -> new Sort.Order(
                    s.property(),
                    s.isAscending() ? Sort.Order.Direction.ASC : Sort.Order.Direction.DESC,
                    s.ignoreCase())
                ).toList()
            )
        );
        conversionService.addConverter(jakarta.data.page.PageRequest.class, Pageable.class, pageRequest -> {
            if (pageRequest.mode() == PageRequest.Mode.CURSOR_NEXT || pageRequest.mode() == PageRequest.Mode.CURSOR_PREVIOUS) {
                return CursoredPageable.from(
                    (int) (pageRequest.page() - 1),
                    pageRequest.cursor().map(cursor -> Pageable.Cursor.of(cursor.elements())).orElse(null),
                    pageRequest.mode() == PageRequest.Mode.CURSOR_NEXT ? Pageable.Mode.CURSOR_NEXT : Pageable.Mode.CURSOR_PREVIOUS,
                    pageRequest.size(),
                    null,
                    pageRequest.requestTotal()
                );
            } else {
                Pageable pageable = Pageable.from((int) (pageRequest.page() - 1), pageRequest.size());
                if (pageRequest.requestTotal()) {
                    pageable = pageable.withTotal();
                } else {
                    pageable = pageable.withoutTotal();
                }
                return pageable;
            }
        });
        conversionService.addConverter(Pageable.class, jakarta.data.page.PageRequest.class, JakartaDataConverters::convert);
        conversionService.addConverter(Page.class, jakarta.data.page.Page.class, page ->
            new PageRecord<>(
                convert(page.getPageable()),
                page.getContent(),
                page.getPageable().requestTotal() ? page.getTotalSize() : -1
            )
        );
    }

    private static PageRequest convert(Pageable pageable) {
        return PageRequest.ofPage(pageable.getNumber() + 1, pageable.getSize() == -1 ? Integer.MAX_VALUE : pageable.getSize(), pageable.requestTotal());
    }

}
