plugins {
    alias(libs.plugins.convention.cmp.feature)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(projects.core.domain)
                implementation(projects.core.data)
                implementation(projects.core.presentation)
                implementation(projects.feature.apps.domain)

                implementation(compose.components.uiToolingPreview)
                implementation(compose.components.resources)

                implementation(libs.bundles.landscapist)
                implementation(libs.liquid)
            }
        }

        jvmMain {
            dependencies {

            }
        }
    }

}