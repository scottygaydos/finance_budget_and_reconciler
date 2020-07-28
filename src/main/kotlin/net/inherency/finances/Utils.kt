package net.inherency.finances

fun <O, T> objectIsUnique(objects: List<O>, objectToValidate: (O) -> T): Boolean {
    return objects.map { objectToValidate(it) }.toSet().size == objects.size
}