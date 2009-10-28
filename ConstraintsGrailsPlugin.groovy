/*
* Copyright 2009 the original author or authors.
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

import net.zorched.grails.plugins.validation.ConstraintArtefactHandler
import net.zorched.grails.plugins.validation.GrailsConstraintClass
import org.codehaus.groovy.grails.validation.ConstrainedProperty
import net.zorched.grails.plugins.validation.CustomConstraintFactory
import net.zorched.constraints.*

class ConstraintsGrailsPlugin {
    // the plugin version
    def version = "0.1"

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.1.1 > *"

    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/*.gsp",
            "grails-app/views/**/*.gsp",
            "grails-app/domain/**/*.groovy",
            "grails-app/controllers/**/*.groovy",
            "grails-app/utils/net/zorched/test/**.groovy"
    ]

    // TODO Fill in these fields
    def author = "Geoff Lane"
    def authorEmail = "geoff@zorched.net"
    def title = "Custom domain constraints plugin"
    def description = '''\\
    This plugin allows you to create custom domain validations that are applied the same
    way as built-in domain constraints.
    '''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/GrailsConstraints+Plugin"

    def loadAfter = ['core', 'hibernate']
    def watchedResources = [
            "file:./grails-app/utils/**/*Constraint.groovy",
            "file:./plugins/*/grails-app/utils/**/*Constraint.groovy"
    ]

    // We can add provided constraints in this plugin using this
    def providedArtefacts = [
            ComparisonConstraint,
            SsnConstraint,
            UsPhoneConstraint,
            UsZipConstraint
    ]

    def artefacts = [new ConstraintArtefactHandler()]

    def onChange = {event ->
        // XXX: Not sure if this works?
        if (application.isArtefactOfType(ConstraintArtefactHandler.TYPE, event.source)) {
            application.addArtefact(ConstraintArtefactHandler.TYPE, event.source)
        }
    }

    def doWithDynamicMethods = {applicationContext ->
        println "Configuring custom constraints..."

        boolean usingHibernate = manager.hasGrailsPlugin("hibernate")
        for (GrailsConstraintClass c in application.constraintClasses) {
            registerConstraint(c, usingHibernate, applicationContext)
        }
    }
    
    private void registerConstraint(constraintClass, usingHibernate, applicationContext) {
        def constraintName = constraintClass.name
        // println "Loading constraint: $constraintClass.Name"

        if (usingHibernate) {
            // println "Hibernate plugin in use, allowing persistent constraints"
            ConstrainedProperty.registerNewConstraint(constraintName,
                    new CustomConstraintFactory(applicationContext, constraintClass))
        } else {
            // Don't allow persistent constraints if hibernate is not being used
            ConstrainedProperty.registerNewConstraint(constraintName,
                    new CustomConstraintFactory(constraintClass))
        }
    }
}
