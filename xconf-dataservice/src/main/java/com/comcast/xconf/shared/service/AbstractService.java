/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Igor Kostrov
 * Created: 3/16/2016
*/
package com.comcast.xconf.shared.service;

import com.comcast.hesperius.dataaccess.core.ValidationException;
import com.comcast.hesperius.dataaccess.core.exception.EntityExistsException;
import com.comcast.hesperius.dataaccess.core.exception.EntityNotFoundException;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractService<T extends IPersistable & Comparable> implements CrudService<T> {

    @Override
    public T create(T entity) throws ValidationException {
        beforeCreating(entity);
        return save(entity);
    }

    @Override
    public T update(T entity) throws ValidationException {
        beforeUpdating(entity);
        return save(entity);
    }

    @Override
    public T getOne(String id) {
        T one = getEntityDAO().getOne(id);
        if (one == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
        return one;
    }

    @Override
    public List<T> getAll() {
        List<T> all = getEntityDAO().getAll();
        Collections.sort(all);
        return all;
    }

    @Override
    public T delete(String id) throws ValidationException {
        T entity = getEntityDAO().getOne(id);
        if (entity == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
        validateUsage(id);
        getEntityDAO().deleteOne(id);

        return entity;
    }

    protected void beforeCreating(T entity) {
        String id = entity.getId();
        if (StringUtils.isBlank(id)) {
            entity.setId(UUID.randomUUID().toString());
        } else if (getEntityDAO().getOne(id, false) != null) {
            throw new EntityExistsException("Entity with id: " + id + " already exists");
        }
    }

    protected void beforeUpdating(T entity) throws ValidationException {
        String id = entity.getId();
        if (StringUtils.isBlank(id)) {
            throw new ValidationException("Entity id is empty");
        }
        T existingEntity = getEntityDAO().getOne(id, false);
        if (existingEntity == null) {
            throw new EntityNotFoundException("Entity with id: " + id + " does not exist");
        }
    }

    protected void beforeSaving(T entity) {
        normalizeOnSave(entity);
        validateOnSave(entity);
    }

    protected T save(T entity) throws ValidationException {
        beforeSaving(entity);
        getEntityDAO().setOne(entity.getId(), entity);
        return entity;
    }

    public void validateOnSave(T entity) {
        getValidator().validate(entity);

        Iterable<T> all = getAll();
        getValidator().validateAll(entity, all);
    }

    public List<T> findByContext(final Map<String, String> searchContext) {
        List<Predicate<T>> predicates = getPredicatesByContext(searchContext);

        return Lists.newArrayList(Iterables.filter(getAll(), Predicates.and(predicates)));
    }

    protected abstract List<Predicate<T>> getPredicatesByContext(Map<String, String> context);

    protected void normalizeOnSave(T entity) {}

    protected void validateUsage(String id) {}
}
