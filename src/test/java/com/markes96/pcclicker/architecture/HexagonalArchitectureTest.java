package com.markes96.pcclicker.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Keeps the hexagon honest: the domain knows nothing, the application only knows the domain, and
 * adapters only meet each other in the composition root.
 */
@AnalyzeClasses(packages = "com.markes96.pcclicker", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    private static final String ROOT = "com.markes96.pcclicker";

    @ArchTest
    static final ArchRule layers_follow_the_hexagonal_architecture = onionArchitecture()
            .domainModels(ROOT + ".domain.configuration..", ROOT + ".domain.geometry..",
                    ROOT + ".domain.session..", ROOT + ".domain.control..")
            .domainServices(ROOT + ".domain.movement..")
            .applicationServices(ROOT + ".application..")
            .adapter("ui", ROOT + ".adapter.in.ui..")
            .adapter("hotkeys", ROOT + ".adapter.in.hotkey..")
            .adapter("system", ROOT + ".adapter.out.system..")
            .adapter("scheduling", ROOT + ".adapter.out.scheduling..")
            // The composition root wires everything together by definition.
            .ignoreDependency(resideInAPackage(ROOT + ".bootstrap.."), alwaysTrue());

    @ArchTest
    static final ArchRule the_domain_only_depends_on_the_jdk = classes()
            .that().resideInAPackage(ROOT + ".domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(ROOT + ".domain..", "java..");

    @ArchTest
    static final ArchRule the_application_only_depends_on_the_domain_and_the_jdk = classes()
            .that().resideInAPackage(ROOT + ".application..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(ROOT + ".domain..", ROOT + ".application..", "java..");

    @ArchTest
    static final ArchRule the_ui_reaches_the_application_only_through_ports = noClasses()
            .that().resideInAPackage(ROOT + ".adapter.in.ui..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + ".application.service..");

    @ArchTest
    static final ArchRule nothing_depends_on_the_composition_root = noClasses()
            .that().resideOutsideOfPackage(ROOT + ".bootstrap..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + ".bootstrap..");

    @ArchTest
    static final ArchRule packages_are_free_of_cycles = slices()
            .matching(ROOT + ".(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule technology_stays_out_of_the_core = noClasses()
            .that().resideInAnyPackage(ROOT + ".domain..", ROOT + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage("javafx..", "java.awt..", "lc.kra..", "atlantafx..");
}
