package com.wassimbh.cogistest.utilities

interface Node

data class Edge(var node1: Node, var node2: Node, val distance: Double)


fun findShortestPath(edges: List<Edge>, source: Node, target: Node): ShortestPathResult {

    // Note: this implementation uses similar variable names as the algorithm given do.
    // We found it more important to align with the algorithm than to use possibly more sensible naming.

    val dist = mutableMapOf<Node, Double>()
    val prev = mutableMapOf<Node, Node?>()
    val q = findDistinctNodes(edges)

    q.forEach { v ->
        dist[v] = Double.MAX_VALUE
        prev[v] = null
    }
    dist[source] = 0.0

    while (q.isNotEmpty()) {
        val u = q.minByOrNull { dist[it] ?: 0.0 }
        q.remove(u)

        if (u == target) {
            break // Found shortest path to target
        }
        val list =  edges.filter { it.node1 == u || it.node2 == u }
        list.forEach { edge ->
                val v = if(edge.node1 == u) edge.node2 else edge.node1
                val alt = (dist[u] ?: 0.0) + edge.distance
                if (alt < (dist[v] ?: 0.0)) {
                    dist[v] = alt
                    prev[v] = u
                }
            }
    }

    return ShortestPathResult(prev, dist, source, target)
}

private fun findDistinctNodes(edges: List<Edge>): MutableSet<Node> {
    val nodes = mutableSetOf<Node>()
    edges.forEach {
        nodes.add(it.node1)
        nodes.add(it.node2)
    }
    return nodes
}

/**
 * Traverse result
 */
class ShortestPathResult(private val prev: Map<Node, Node?>, private val dist: Map<Node, Double>, private val source: Node, private val target: Node) {

    fun shortestPath(from: Node = source, to: Node = target, list: List<Node> = emptyList()): List<Node> {
        val last = prev[to] ?: return if (from == to) {
            list + to
        } else {
            emptyList()
        }
        return shortestPath(from, last, list) + to
    }

    fun shortestDistance(): Double? {
        val shortest = dist[target]
        if (shortest == Double.MAX_VALUE) {
            return null
        }
        return shortest
    }
}