/*
 * Copyright 2003-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.stat.univariate.moment;

import org.apache.commons.math.stat.univariate.StorelessUnivariateStatisticAbstractTest;
import org.apache.commons.math.stat.univariate.UnivariateStatistic;

/**
 * Test cases for the {@link UnivariateStatistic} class.
 * @version $Revision: 1.2 $ $Date: 2004/06/17 21:37:05 $
 */
public class FirstMomentTest extends StorelessUnivariateStatisticAbstractTest{

    /** descriptive statistic. */
    protected FirstMoment stat;
    
    /**
     * @param name
     */
    public FirstMomentTest(String name) {
        super(name);
    }
    
    /**
     * @see org.apache.commons.math.stat.univariate.UnivariateStatisticAbstractTest#getUnivariateStatistic()
     */
    public UnivariateStatistic getUnivariateStatistic() {
        return new FirstMoment();
    }

    /**
     * @see org.apache.commons.math.stat.univariate.UnivariateStatisticAbstractTest#expectedValue()
     */
    public double expectedValue() {
        return this.mean;
    }

}
