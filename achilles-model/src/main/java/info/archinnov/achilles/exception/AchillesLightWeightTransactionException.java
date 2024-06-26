/*
 * Copyright (C) 2012-2014 DuyHai DOAN
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package info.archinnov.achilles.exception;

import static info.archinnov.achilles.listener.CASResultListener.CASResult;
import static info.archinnov.achilles.listener.CASResultListener.CASResult.Operation;
import info.archinnov.achilles.type.TypedMap;

public class AchillesLightWeightTransactionException extends AchillesException {
    private static final long serialVersionUID = 1L;
    private final CASResult casResult;

    public AchillesLightWeightTransactionException(CASResult casResult) {
        super(casResult.toString());
        this.casResult = casResult;
    }

    public Operation operation() {
        return casResult.operation();
    }

    public TypedMap currentValues() {
        return casResult.currentValues();
    }

    @Override
    public String toString() {
        return casResult.toString();
    }
}


