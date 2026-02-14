package zed.rainxch.core.data.install

import zed.rainxch.core.domain.model.Component
import zed.rainxch.core.domain.model.ComponentDependency
import zed.rainxch.core.domain.repository.ComponentRepository
import zed.rainxch.core.domain.repository.InstalledAppsRepository

class DependencyResolver(
    private val componentRepository: ComponentRepository,
    private val installedAppsRepository: InstalledAppsRepository
) {
    suspend fun resolve(component: Component): DependencyResult {
        if (component.dependencies.isEmpty()) {
            return DependencyResult.Satisfied(listOf(component))
        }

        val missing = mutableListOf<ComponentDependency>()
        val toInstall = mutableListOf<Component>()
        val visited = mutableSetOf<String>()

        collectDependencies(component, missing, toInstall, visited)

        if (missing.isNotEmpty()) {
            return DependencyResult.Missing(missing)
        }

        if (detectCircularDependency(component.id, visited = mutableSetOf())) {
            return DependencyResult.CircularDependency(component.id)
        }

        val sorted = topologicalSort(toInstall)
        return DependencyResult.Satisfied(sorted + component)
    }

    private suspend fun collectDependencies(
        component: Component,
        missing: MutableList<ComponentDependency>,
        toInstall: MutableList<Component>,
        visited: MutableSet<String>
    ) {
        if (visited.contains(component.id)) return
        visited.add(component.id)

        for (dep in component.dependencies) {
            val installed = installedAppsRepository.getByComponentId(dep.id)
            if (installed != null) {
                if (dep.version.isNotBlank() && !matchesVersion(installed.installedVersion, dep.version)) {
                    missing.add(dep)
                }
                continue
            }

            val depComponent = componentRepository.getComponentById(dep.id)
            if (depComponent == null) {
                missing.add(dep)
                continue
            }

            toInstall.add(depComponent)
            collectDependencies(depComponent, missing, toInstall, visited)
        }
    }

    private suspend fun detectCircularDependency(
        componentId: String,
        visited: MutableSet<String>
    ): Boolean {
        if (visited.contains(componentId)) return true
        visited.add(componentId)

        val component = componentRepository.getComponentById(componentId) ?: return false
        for (dep in component.dependencies) {
            if (detectCircularDependency(dep.id, visited)) return true
        }

        visited.remove(componentId)
        return false
    }

    private fun topologicalSort(components: List<Component>): List<Component> {
        val result = mutableListOf<Component>()
        val visited = mutableSetOf<String>()

        fun visit(component: Component) {
            if (visited.contains(component.id)) return
            visited.add(component.id)
            for (dep in component.dependencies) {
                val depComponent = components.find { it.id == dep.id }
                if (depComponent != null) visit(depComponent)
            }
            result.add(component)
        }

        components.forEach { visit(it) }
        return result
    }

    private fun matchesVersion(installed: String, required: String): Boolean {
        if (required.isBlank()) return true
        val cleanRequired = required.removePrefix(">=").removePrefix("<=").removePrefix("=").trim()
        val normalizedInstalled = installed.removePrefix("v")
        val normalizedRequired = cleanRequired.removePrefix("v")

        if (required.startsWith(">=")) {
            return compareVersions(normalizedInstalled, normalizedRequired) >= 0
        }
        return normalizedInstalled == normalizedRequired
    }

    private fun compareVersions(a: String, b: String): Int {
        val partsA = a.split(".")
        val partsB = b.split(".")
        val maxLength = maxOf(partsA.size, partsB.size)
        for (i in 0 until maxLength) {
            val numA = partsA.getOrNull(i)?.toIntOrNull() ?: 0
            val numB = partsB.getOrNull(i)?.toIntOrNull() ?: 0
            if (numA != numB) return numA.compareTo(numB)
        }
        return 0
    }
}

sealed class DependencyResult {
    data class Satisfied(val installOrder: List<Component>) : DependencyResult()
    data class Missing(val dependencies: List<ComponentDependency>) : DependencyResult()
    data class CircularDependency(val componentId: String) : DependencyResult()
}
