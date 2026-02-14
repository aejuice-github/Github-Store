import org.gradle.api.Plugin
import org.gradle.api.Project
import zed.rainxch.githubstore.convention.configureJvmTarget

class CmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            configureJvmTarget()
        }
    }
}
