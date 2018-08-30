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
 * Created: 23.03.2016
*/
package com.comcast.xconf.shared.service;

import com.comcast.hesperius.dataaccess.core.ValidationException;
import com.comcast.hesperius.dataaccess.core.dao.ISimpleCachedDAO;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.validators.IValidator;

import java.util.List;
import java.util.Map;

public interface CrudService<T extends IPersistable & Comparable> {
    T create(T entity) throws ValidationException;

    T update(T entity) throws ValidationException;

    T getOne(String id);

    List<T> getAll();

    T delete(String id) throws ValidationException;

    ISimpleCachedDAO<String, T> getEntityDAO();

    IValidator<T> getValidator();

    List<T> findByContext(Map<String, String> context);
}
