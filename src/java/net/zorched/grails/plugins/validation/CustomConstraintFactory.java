/* Copyright 2006-2008 the original author or authors.
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

package net.zorched.grails.plugins.validation;

import groovy.lang.Closure;
import org.codehaus.groovy.grails.validation.AbstractConstraint;
import org.codehaus.groovy.grails.validation.Constraint;
import org.codehaus.groovy.grails.validation.ConstraintFactory;
import org.springframework.validation.Errors;

import java.util.ArrayList;

/**
 * Factory for creating wrapped custom Constraints
 */
public class CustomConstraintFactory implements ConstraintFactory {

    private final DefaultGrailsConstraintClass constraint;

    public CustomConstraintFactory(DefaultGrailsConstraintClass c) {
        this.constraint = c;
    }

    public Constraint newInstance() {
        return new CustomConstraint();
    }


    /**
     * Wrapper class that extends the AbstractConstraint so the user supplied constraint
     * conforms to the proper API.
     */
    class CustomConstraint extends AbstractConstraint {
        public boolean supports(Class aClass) {
            Closure c = constraint.getSupportsMethod();
            if (null == c) {
                return true;
            }
            return (Boolean) c.call(aClass);
        }

        public String getName() {
            return constraint.getName();
        }

        public void setParameter(Object constraintParameter) {
            constraint.validateParams(constraintParameter, constraintPropertyName, constraintOwningClass);

            constraint.getMetaClass().setProperty(constraint, "params", constraintParameter);
            super.setParameter(constraintParameter);
        }

        @Override
        protected String getDefaultMessage(String code) {
            System.out.println("xxx: " + code);
            
            String m = super.getDefaultMessage(code);
            if (null == m) {
                m = constraint.getDefaultMessage();
            }
            return m;
        }

        @Override
        protected void processValidate(Object target, Object propertyValue, Errors errors) {
            Closure c = constraint.getValidationMethod();
            int paramCount = c.getMaximumNumberOfParameters();

            ArrayList<Object> params = new ArrayList<Object>();
            if (paramCount > 0)
                params.add(propertyValue);
            if (paramCount > 1)
                params.add(target);
            if (paramCount > 2)
                params.add(errors);

            boolean isValid = (Boolean) c.call(params.toArray());
            if (! isValid) {
                Object[] args = new Object[] { constraintPropertyName, constraintOwningClass, propertyValue };
                super.rejectValue(target, errors, constraint.getDefaultMessageCode(), constraint.getFailureCode(), args);
            }
        }
    }
}
