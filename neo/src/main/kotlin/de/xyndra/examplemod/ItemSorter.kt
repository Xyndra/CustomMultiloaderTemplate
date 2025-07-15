package de.xyndra.examplemod

// WARNING: AI generated code (but tested and works)

fun sortItemsByDependencies(): List<Pair<String, ItemInfo>> {
    val itemInfos = Globals.itemInfos.toList()
    val visited = mutableSetOf<String>()
    val visiting = mutableSetOf<String>()
    val result = mutableListOf<Pair<String, ItemInfo>>()

    fun visit(itemName: String) {
        if (itemName in visiting) {
            throw IllegalStateException("Found cyclical reference for item: $itemName")
        }
        if (itemName in visited) {
            return
        }

        visiting.add(itemName)

        val itemInfo = Globals.itemInfos[itemName]
        if (itemInfo != null) {
            for ((_, itemRef) in itemInfo.tabs) {
                when (itemRef) {
                    is ItemReference.ItemName -> {
                        if (Globals.itemInfos.containsKey(itemRef.name)) {
                            visit(itemRef.name)
                        }
                    }
                    else -> {}
                }
            }

            visiting.remove(itemName)
            visited.add(itemName)
            result.add(itemName to itemInfo)
        } else {
            visiting.remove(itemName)
        }
    }

    for ((itemName, _) in itemInfos) {
        visit(itemName)
    }

    return result
}