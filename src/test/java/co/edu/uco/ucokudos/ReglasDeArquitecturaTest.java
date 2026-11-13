package co.edu.uco.ucokudos;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.List;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Hace cumplir ADR-002: las dependencias apuntan hacia adentro y los módulos solo se hablan por sus
 * puertos de entrada.
 *
 * <p>Las reglas permiten estar vacías porque el esqueleto todavía no tiene clases en los módulos; en
 * cuanto aparezca la primera, la regla empieza a vigilarla.
 */
@AnalyzeClasses(packages = ReglasDeArquitecturaTest.RAIZ, importOptions = ImportOption.DoNotIncludeTests.class)
class ReglasDeArquitecturaTest {

	static final String RAIZ = "co.edu.uco.ucokudos";

	private static final List<String> MODULOS = List.of("kudos", "avisos", "identidad", "auditoria", "moderacion");

	@ArchTest
	static final ArchRule el_dominio_no_depende_de_frameworks = noClasses()
		.that().resideInAPackage("..dominio..")
		.should().dependOnClassesThat().resideInAnyPackage(
				"org.springframework..", "jakarta.persistence..", "jakarta.servlet..",
				"com.fasterxml.jackson..", "tools.jackson..", "org.hibernate..")
		.because("el dominio son reglas del negocio puras, sin framework ni base de datos (ADR-002)")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule el_dominio_no_depende_de_las_otras_capas = noClasses()
		.that().resideInAPackage("..dominio..")
		.should().dependOnClassesThat().resideInAnyPackage("..aplicacion..", "..infraestructura..")
		.because("las dependencias apuntan hacia adentro (ADR-002)")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule la_aplicacion_no_depende_de_la_infraestructura = noClasses()
		.that().resideInAPackage("..aplicacion..")
		.should().dependOnClassesThat().resideInAPackage("..infraestructura..")
		.because("los casos de uso definen puertos y la infraestructura los implementa (ADR-002)")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule los_modulos_no_tienen_ciclos = slices()
		.matching(RAIZ + ".(*)..")
		.should().beFreeOfCycles()
		.allowEmptyShould(true);

	@ArchTest
	static void los_modulos_solo_se_hablan_por_puertos_de_entrada(JavaClasses clases) {
		for (String modulo : MODULOS) {
			for (String otroModulo : MODULOS) {
				if (modulo.equals(otroModulo)) {
					continue;
				}
				noClasses()
					.that().resideInAPackage(paqueteDe(modulo))
					.should().dependOnClassesThat(interiorDe(otroModulo))
					.because("un módulo solo usa los puertos de entrada de otro (ADR-002)")
					.allowEmptyShould(true)
					.check(clases);
			}
		}
	}

	private static String paqueteDe(String modulo) {
		return RAIZ + "." + modulo + "..";
	}

	private static DescribedPredicate<JavaClass> interiorDe(String modulo) {
		String puertosDeEntrada = RAIZ + "." + modulo + ".aplicacion.puerto.entrada..";
		return resideInAPackage(paqueteDe(modulo))
			.and(not(resideInAPackage(puertosDeEntrada)))
			.as("pertenecen al interior del módulo " + modulo);
	}

}
